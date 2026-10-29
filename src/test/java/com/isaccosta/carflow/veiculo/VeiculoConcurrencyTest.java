package com.isaccosta.carflow.veiculo;

import com.isaccosta.carflow.tenant.Loja;
import com.isaccosta.carflow.tenant.LojaRepository;
import com.isaccosta.carflow.tenant.TenantContext;
import com.isaccosta.carflow.tenant.TenantIdentifierResolver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TenantIdentifierResolver.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class VeiculoConcurrencyTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private VeiculoRepository veiculoRepository;

    @Autowired
    private LojaRepository lojaRepository;

    @Autowired
    private JdbcClient jdbcClient;

    private Loja loja;

    @BeforeEach
    void setUp() {
        limparBanco();

        loja = lojaRepository.save(Loja.builder()
                .razaoSocial("Auto Concorrência Ltda")
                .nomeFantasia("Auto Concorrência")
                .cnpj("99999999000199")
                .telefone("11999999999")
                .slug("auto-concorrencia")
                .build());
    }

    @AfterEach
    void tearDown() {
        limparBanco();
    }

    private void limparBanco() {
        TenantContext.clear();
        jdbcClient.sql("DELETE FROM veiculo_opcionais").update();
        jdbcClient.sql("DELETE FROM veiculos").update();
        jdbcClient.sql("DELETE FROM opcionais").update();
        jdbcClient.sql("DELETE FROM lojas").update();
    }

    private Veiculo buildVeiculo() {
        return Veiculo.builder()
                .lojaId(loja.getId())
                .marca("Toyota")
                .modelo("Corolla")
                .versao("Altis")
                .anoFabricacao(2023)
                .anoModelo(2024)
                .placa("COR2024")
                .chassi("9BRBL42E0P9999999")
                .cor("Branco")
                .kmAtual(10000)
                .combustivel(Combustivel.FLEX)
                .cambio(Cambio.AUTOMATICO)
                .precoCompra(new BigDecimal("130000.00"))
                .precoAnuncio(new BigDecimal("145000.00"))
                .status(StatusVeiculo.DISPONIVEL)
                .build();
    }

    @Test
    @DisplayName("Deve impedir atualização concorrente disparando OptimisticLockingFailureException")
    void shouldPreventConcurrentModificationWithOptimisticLocking() {
        TenantContext.setTenantId(loja.getId());

        // 1. Veículo é cadastrado com version = 0
        Veiculo veiculoSalvo = veiculoRepository.save(buildVeiculo());
        UUID veiculoId = veiculoSalvo.getId();
        assertThat(veiculoSalvo.getVersion()).isEqualTo(0L);

        // 2. Vendedor 1 e Vendedor 2 carregam o mesmo veículo simultaneamente (ambos
        // com version = 0)
        Veiculo sessaoVendedor1 = veiculoRepository.findById(veiculoId).orElseThrow();
        Veiculo sessaoVendedor2 = veiculoRepository.findById(veiculoId).orElseThrow();

        assertThat(sessaoVendedor1.getVersion()).isEqualTo(0L);
        assertThat(sessaoVendedor2.getVersion()).isEqualTo(0L);

        // 3. Vendedor 1 reserva o carro primeiro e comita com sucesso:
        sessaoVendedor1.transitionTo(StatusVeiculo.RESERVADO);
        veiculoRepository.saveAndFlush(sessaoVendedor1);

        // A versão no PostgreSQL subiu para 1!
        Veiculo noBancoAposVendedor1 = veiculoRepository.findById(veiculoId).orElseThrow();
        assertThat(noBancoAposVendedor1.getVersion()).isEqualTo(1L);
        assertThat(noBancoAposVendedor1.getStatus()).isEqualTo(StatusVeiculo.RESERVADO);

        // 4. Vendedor 2 tenta vender o carro usando sua tela desatualizada (que ainda
        // tem version = 0):
        sessaoVendedor2.transitionTo(StatusVeiculo.VENDIDO);

        // O Hibernate detecta que a versão no banco é 1 e a da entidade é 0 -> Dispara
        // OptimisticLockingFailureException!
        assertThatThrownBy(() -> veiculoRepository.saveAndFlush(sessaoVendedor2))
                .isInstanceOf(OptimisticLockingFailureException.class);
    }

    @Test
    @DisplayName("Deve permitir apenas 1 atualização com sucesso em execução paralela com Threads reais")
    void shouldHandleConcurrentUpdatesWithMultipleThreads() throws Exception {
        TenantContext.setTenantId(loja.getId());
        Veiculo veiculo = veiculoRepository.save(buildVeiculo());
        UUID veiculoId = veiculo.getId();

        AtomicInteger sucessos = new AtomicInteger(0);
        AtomicInteger conflitos = new AtomicInteger(0);

        // Thread 1: Vendedor A
        CompletableFuture<Void> tarefaVendedorA = CompletableFuture.runAsync(() -> {
            try {
                TenantContext.setTenantId(loja.getId());
                Veiculo v = veiculoRepository.findById(veiculoId).orElseThrow();
                Thread.sleep(50); // Simula uma leve latência
                v.transitionTo(StatusVeiculo.RESERVADO);
                veiculoRepository.saveAndFlush(v);
                sucessos.incrementAndGet();
            } catch (OptimisticLockingFailureException e) {
                conflitos.incrementAndGet();
            } catch (Exception ignored) {
            }
        });

        // Thread 2: Vendedor B
        CompletableFuture<Void> tarefaVendedorB = CompletableFuture.runAsync(() -> {
            try {
                TenantContext.setTenantId(loja.getId());
                Veiculo v = veiculoRepository.findById(veiculoId).orElseThrow();
                Thread.sleep(50); // Simula a mesma latência
                v.transitionTo(StatusVeiculo.VENDIDO);
                veiculoRepository.saveAndFlush(v);
                sucessos.incrementAndGet();
            } catch (OptimisticLockingFailureException e) {
                conflitos.incrementAndGet();
            } catch (Exception ignored) {
            }
        });

        // Aguarda as duas threads terminarem
        CompletableFuture.allOf(tarefaVendedorA, tarefaVendedorB).join();

        // Prova matemática de concorrência: apenas 1 conseguiu salvar, a outra tomou
        // OptimisticLockingFailureException!
        assertThat(sucessos.get()).isEqualTo(1);
        assertThat(conflitos.get()).isEqualTo(1);

        // O banco terminou em estado consistente (version = 1)
        Veiculo estadoFinal = veiculoRepository.findById(veiculoId).orElseThrow();
        assertThat(estadoFinal.getVersion()).isEqualTo(1L);
    }
}