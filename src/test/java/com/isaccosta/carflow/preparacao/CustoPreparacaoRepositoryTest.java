package com.isaccosta.carflow.preparacao;

import com.isaccosta.carflow.tenant.Loja;
import com.isaccosta.carflow.tenant.LojaRepository;
import com.isaccosta.carflow.tenant.TenantContext;
import com.isaccosta.carflow.tenant.TenantIdentifierResolver;
import com.isaccosta.carflow.veiculo.Cambio;
import com.isaccosta.carflow.veiculo.Combustivel;
import com.isaccosta.carflow.veiculo.StatusVeiculo;
import com.isaccosta.carflow.veiculo.Veiculo;
import com.isaccosta.carflow.veiculo.VeiculoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TenantIdentifierResolver.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class CustoPreparacaoRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private CustoPreparacaoRepository custoPreparacaoRepository;

    @Autowired
    private VeiculoRepository veiculoRepository;

    @Autowired
    private LojaRepository lojaRepository;

    @Autowired
    private JdbcClient jdbcClient;

    private Loja lojaA;
    private Loja lojaB;
    private Veiculo veiculoLojaA;

    @BeforeEach
    void setUp() {
        limparBanco();

        lojaA = lojaRepository.save(Loja.builder()
                .razaoSocial("Auto A Ltda")
                .nomeFantasia("Auto A")
                .cnpj("11111111000101")
                .telefone("11911111111")
                .slug("auto-a")
                .build());

        lojaB = lojaRepository.save(Loja.builder()
                .razaoSocial("Auto B Ltda")
                .nomeFantasia("Auto B")
                .cnpj("22222222000102")
                .telefone("11922222222")
                .slug("auto-b")
                .build());

        TenantContext.setTenantId(lojaA.getId());
        veiculoLojaA = veiculoRepository.save(Veiculo.builder()
                .lojaId(lojaA.getId())
                .marca("Toyota")
                .modelo("Corolla")
                .versao("XEi")
                .anoFabricacao(2022)
                .anoModelo(2023)
                .placa("ABC1D23")
                .chassi("9BRBL42E0P1234567")
                .cor("Prata")
                .kmAtual(35000)
                .combustivel(Combustivel.FLEX)
                .cambio(Cambio.AUTOMATICO)
                .precoCompra(new BigDecimal("110000.00"))
                .precoAnuncio(new BigDecimal("125000.00"))
                .status(StatusVeiculo.EM_PREPARACAO)
                .build());
    }

    @AfterEach
    void tearDown() {
        limparBanco();
    }

    private void limparBanco() {
        TenantContext.clear();
        jdbcClient.sql("DELETE FROM custos_preparacao").update();
        jdbcClient.sql("DELETE FROM veiculo_opcionais").update();
        jdbcClient.sql("DELETE FROM veiculos").update();
        jdbcClient.sql("DELETE FROM opcionais").update();
        jdbcClient.sql("DELETE FROM lojas").update();
    }

    @Test
    @DisplayName("Deve buscar custos de um veículo ordenados pela data de realização decrescente")
    void shouldFindCustosByVeiculoIdOrderedByDataRealizacaoDesc() {
        TenantContext.setTenantId(lojaA.getId());

        CustoPreparacao custoAntigo = custoPreparacaoRepository.save(CustoPreparacao.builder()
                .lojaId(lojaA.getId())
                .veiculo(veiculoLojaA)
                .categoria(CategoriaCusto.MANUTENCAO)
                .descricao("Troca de pastilhas de freio")
                .valor(new BigDecimal("450.00"))
                .dataRealizacao(LocalDate.of(2026, 11, 10))
                .fornecedor("Oficina do Zé")
                .build());

        CustoPreparacao custoRecente = custoPreparacaoRepository.save(CustoPreparacao.builder()
                .lojaId(lojaA.getId())
                .veiculo(veiculoLojaA)
                .categoria(CategoriaCusto.ESTETICA)
                .descricao("Polimento técnico e higienização")
                .valor(new BigDecimal("800.00"))
                .dataRealizacao(LocalDate.of(2026, 11, 15))
                .fornecedor("Detailing Studio")
                .build());

        List<CustoPreparacao> custos = custoPreparacaoRepository
                .findByVeiculoIdOrderByDataRealizacaoDesc(veiculoLojaA.getId());

        assertThat(custos).hasSize(2);
        assertThat(custos.getFirst().getId()).isEqualTo(custoRecente.getId());
        assertThat(custos.get(1).getId()).isEqualTo(custoAntigo.getId());
    }

    @Test
    @DisplayName("Deve somar o valor total de custos de um veículo com precisão")
    void shouldSumValorByVeiculoIdCorrectly() {
        TenantContext.setTenantId(lojaA.getId());

        custoPreparacaoRepository.save(CustoPreparacao.builder()
                .lojaId(lojaA.getId())
                .veiculo(veiculoLojaA)
                .categoria(CategoriaCusto.MANUTENCAO)
                .descricao("4 Pneus novos")
                .valor(new BigDecimal("2400.50"))
                .dataRealizacao(LocalDate.now())
                .build());

        custoPreparacaoRepository.save(CustoPreparacao.builder()
                .lojaId(lojaA.getId())
                .veiculo(veiculoLojaA)
                .categoria(CategoriaCusto.DOCUMENTACAO)
                .descricao("Transferência e IPVA proporcional")
                .valor(new BigDecimal("1250.25"))
                .dataRealizacao(LocalDate.now())
                .build());

        BigDecimal total = custoPreparacaoRepository.sumValorByVeiculoId(veiculoLojaA.getId());

        assertThat(total).isEqualByComparingTo(new BigDecimal("3650.75"));
    }

    @Test
    @DisplayName("Deve retornar ZERO ao somar custos de um veículo que não possui despesas")
    void shouldReturnZeroWhenVehicleHasNoCustos() {
        TenantContext.setTenantId(lojaA.getId());

        BigDecimal total = custoPreparacaoRepository.sumValorByVeiculoId(veiculoLojaA.getId());

        assertThat(total).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Deve isolar custos entre diferentes lojas através do TenantId")
    void shouldIsolateCustosByTenant() {
        TenantContext.setTenantId(lojaA.getId());

        custoPreparacaoRepository.save(CustoPreparacao.builder()
                .lojaId(lojaA.getId())
                .veiculo(veiculoLojaA)
                .categoria(CategoriaCusto.MANUTENCAO)
                .descricao("Revisão de suspensão")
                .valor(new BigDecimal("1500.00"))
                .dataRealizacao(LocalDate.now())
                .build());

        // Troca de contexto para Loja B:
        TenantContext.setTenantId(lojaB.getId());

        List<CustoPreparacao> custosVistosPelaLojaB = custoPreparacaoRepository
                .findByVeiculoIdOrderByDataRealizacaoDesc(veiculoLojaA.getId());
        BigDecimal totalVistoPelaLojaB = custoPreparacaoRepository
                .sumValorByVeiculoId(veiculoLojaA.getId());

        assertThat(custosVistosPelaLojaB).isEmpty();
        assertThat(totalVistoPelaLojaB).isEqualByComparingTo(BigDecimal.ZERO);
    }
}