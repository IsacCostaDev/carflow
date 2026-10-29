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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TenantIdentifierResolver.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class VeiculoRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private VeiculoRepository veiculoRepository;

    @Autowired
    private OpcionalRepository opcionalRepository;

    @Autowired
    private LojaRepository lojaRepository;

    @Autowired
    private JdbcClient jdbcClient;

    private Loja lojaA;
    private Loja lojaB;

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

    private Veiculo buildVeiculo(Loja loja, String placa, String chassi, StatusVeiculo status) {
        return Veiculo.builder()
                .lojaId(loja.getId())
                .marca("Honda")
                .modelo("Civic")
                .versao("Touring")
                .anoFabricacao(2022)
                .anoModelo(2023)
                .placa(placa)
                .chassi(chassi)
                .cor("Preto")
                .kmAtual(20000)
                .combustivel(Combustivel.GASOLINA)
                .cambio(Cambio.AUTOMATICO)
                .precoCompra(new BigDecimal("110000.00"))
                .precoAnuncio(new BigDecimal("125000.00"))
                .status(status)
                .build();
    }

    @Test
    @DisplayName("Deve persistir veículo e filtrar automaticamente por tenant")
    void shouldPersistAndFilterByTenantAutomatically() {
        // 1. Cadastra o veículo no contexto da Loja A
        TenantContext.setTenantId(lojaA.getId());
        Veiculo veiculoLojaA = veiculoRepository
                .save(buildVeiculo(lojaA, "ABC1D23", "9BWZZZ377VT000001", StatusVeiculo.DISPONIVEL));

        assertThat(veiculoLojaA.getId()).isNotNull();
        assertThat(veiculoLojaA.getVersion()).isEqualTo(0L);

        // 2. Consulta dentro do contexto da Loja A: deve encontrar 1 veículo
        Page<Veiculo> veiculosLojaA = veiculoRepository.findAll(PageRequest.of(0, 10));
        assertThat(veiculosLojaA.getTotalElements()).isEqualTo(1);

        // 3. Troca o contexto para a Loja B: NÃO pode enxergar o veículo da Loja A!
        TenantContext.setTenantId(lojaB.getId());
        Page<Veiculo> veiculosLojaB = veiculoRepository.findAll(PageRequest.of(0, 10));
        assertThat(veiculosLojaB.getTotalElements()).isZero();

        // 4. Busca direta por ID na Loja B também deve vir vazia
        Optional<Veiculo> buscaCrossTenant = veiculoRepository.findById(veiculoLojaA.getId());
        assertThat(buscaCrossTenant).isEmpty();
    }

    @Test
    @DisplayName("Deve filtrar veículos por status respeitando a paginação")
    void shouldFilterVehiclesByStatus() {
        TenantContext.setTenantId(lojaA.getId());

        veiculoRepository.save(buildVeiculo(lojaA, "AAA1111", "9BWZZZ377VT000002", StatusVeiculo.DISPONIVEL));
        veiculoRepository.save(buildVeiculo(lojaA, "BBB2222", "9BWZZZ377VT000003", StatusVeiculo.EM_PREPARACAO));

        Page<Veiculo> disponiveis = veiculoRepository.findByStatus(StatusVeiculo.DISPONIVEL, PageRequest.of(0, 10));
        assertThat(disponiveis.getTotalElements()).isEqualTo(1);
        assertThat(disponiveis.getContent().getFirst().getPlaca()).isEqualTo("AAA1111");
    }

    @Test
    @DisplayName("Deve verificar existência de placa e chassi respeitando o tenant")
    void shouldCheckExistsByPlacaAndChassiWithinTenant() {
        TenantContext.setTenantId(lojaA.getId());
        veiculoRepository.save(buildVeiculo(lojaA, "BRA2E19", "9BWZZZ377VT000004", StatusVeiculo.DISPONIVEL));

        // Na Loja A, placa e chassi existem
        assertThat(veiculoRepository.existsByPlaca("BRA2E19")).isTrue();
        assertThat(veiculoRepository.existsByPlaca("XYZ9999")).isFalse();
        assertThat(veiculoRepository.existsByChassi("9BWZZZ377VT000004")).isTrue();

        // Na Loja B, a placa NÃO deve constar como existente (isolamento de tenant)
        TenantContext.setTenantId(lojaB.getId());
        assertThat(veiculoRepository.existsByPlaca("BRA2E19")).isFalse();
        assertThat(veiculoRepository.existsByChassi("9BWZZZ377VT000004")).isFalse();
    }

    @Test
    @DisplayName("Deve persistir veículo com opcionais associados")
    void shouldPersistVehicleWithOpcionais() {
        TenantContext.setTenantId(lojaA.getId());

        Opcional tetoSolar = opcionalRepository.save(Opcional.builder().nome("Teto Solar").build());
        Opcional arDigital = opcionalRepository.save(Opcional.builder().nome("Ar-condicionado Digital").build());

        Veiculo veiculo = buildVeiculo(lojaA, "OPC1111", "9BWZZZ377VT000005", StatusVeiculo.DISPONIVEL);
        veiculo.addOpcional(tetoSolar);
        veiculo.addOpcional(arDigital);

        Veiculo salvo = veiculoRepository.save(veiculo);

        Optional<Veiculo> encontrado = veiculoRepository.findWithOpcionaisById(salvo.getId());
        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getOpcionais()).hasSize(2);
    }
}
