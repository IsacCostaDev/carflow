package com.isaccosta.carflow.veiculo;

import com.isaccosta.carflow.tenant.TenantContext;
import com.isaccosta.carflow.veiculo.dto.CadastroVeiculoRequest;
import com.isaccosta.carflow.veiculo.dto.CatalogoPublicoResponse;
import com.isaccosta.carflow.veiculo.dto.VeiculoResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VeiculoServiceTest {

    @Mock
    private VeiculoRepository veiculoRepository;

    @Mock
    private OpcionalRepository opcionalRepository;

    @InjectMocks
    private VeiculoService veiculoService;

    private CadastroVeiculoRequest buildRequestValido() {
        return new CadastroVeiculoRequest(
                "Toyota",
                "Corolla",
                "XEi 2.0",
                2023,
                2024,
                "bra2e19", // minúscula proposital para testar normalização
                "9brbl42e0p0123456",
                "Prata",
                15000,
                Combustivel.FLEX,
                Cambio.AUTOMATICO,
                new BigDecimal("120000.00"),
                new BigDecimal("135000.00"),
                "Carro impecável",
                Set.of(UUID.randomUUID()));
    }

    private Veiculo buildVeiculoMock() {
        return Veiculo.builder()
                .id(UUID.randomUUID())
                .lojaId(UUID.randomUUID())
                .marca("Toyota")
                .modelo("Corolla")
                .versao("XEi 2.0")
                .anoFabricacao(2023)
                .anoModelo(2024)
                .placa("BRA2E19")
                .chassi("9BRBL42E0P0123456")
                .cor("Prata")
                .kmAtual(15000)
                .combustivel(Combustivel.FLEX)
                .cambio(Cambio.AUTOMATICO)
                .precoCompra(new BigDecimal("120000.00"))
                .precoAnuncio(new BigDecimal("135000.00"))
                .status(StatusVeiculo.EM_PREPARACAO)
                .version(0L)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Test
    @DisplayName("Deve cadastrar veículo com sucesso normalizando placa e chassi para maiúsculas")
    void shouldRegisterVehicleSuccessfullyWithNormalizedPlacaAndChassi() {
        CadastroVeiculoRequest request = buildRequestValido();

        when(veiculoRepository.existsByPlaca("BRA2E19")).thenReturn(false);
        when(veiculoRepository.existsByChassi("9BRBL42E0P0123456")).thenReturn(false);
        when(opcionalRepository.findAllById(any())).thenReturn(List.of());
        when(veiculoRepository.save(any(Veiculo.class))).thenAnswer(invocation -> {
            Veiculo v = invocation.getArgument(0);
            v.setId(UUID.randomUUID());
            return v;
        });

        VeiculoResponse response = veiculoService.cadastrar(request);

        assertThat(response.id()).isNotNull();
        assertThat(response.placa()).isEqualTo("BRA2E19");
        assertThat(response.chassi()).isEqualTo("9BRBL42E0P0123456");
        assertThat(response.status()).isEqualTo(StatusVeiculo.EM_PREPARACAO);
        verify(veiculoRepository).save(any(Veiculo.class));
    }

    @Test
    @DisplayName("Deve lançar exceção se a placa já existir na revenda")
    void shouldThrowExceptionWhenPlacaAlreadyExists() {
        CadastroVeiculoRequest request = buildRequestValido();
        when(veiculoRepository.existsByPlaca("BRA2E19")).thenReturn(true);

        assertThatThrownBy(() -> veiculoService.cadastrar(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Já existe um veículo cadastrado com a placa: BRA2E19");

        verify(veiculoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção se o chassi já existir na revenda")
    void shouldThrowExceptionWhenChassiAlreadyExists() {
        CadastroVeiculoRequest request = buildRequestValido();
        when(veiculoRepository.existsByPlaca("BRA2E19")).thenReturn(false);
        when(veiculoRepository.existsByChassi("9BRBL42E0P0123456")).thenReturn(true);

        assertThatThrownBy(() -> veiculoService.cadastrar(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Já existe um veículo cadastrado com o chassi: 9BRBL42E0P0123456");

        verify(veiculoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve atualizar status do veículo respeitando a máquina de estados")
    void shouldUpdateStatusSuccessfully() {
        UUID id = UUID.randomUUID();
        Veiculo veiculo = buildVeiculoMock(); // Inicia como EM_PREPARACAO
        when(veiculoRepository.findWithOpcionaisById(id)).thenReturn(Optional.of(veiculo));
        when(veiculoRepository.save(any(Veiculo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VeiculoResponse response = veiculoService.atualizarStatus(id, StatusVeiculo.DISPONIVEL);

        assertThat(response.status()).isEqualTo(StatusVeiculo.DISPONIVEL);
        verify(veiculoRepository).save(veiculo);
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar transição de status inválida no service")
    void shouldThrowExceptionWhenStatusTransitionIsInvalid() {
        UUID id = UUID.randomUUID();
        Veiculo veiculo = buildVeiculoMock(); // EM_PREPARACAO
        when(veiculoRepository.findWithOpcionaisById(id)).thenReturn(Optional.of(veiculo));

        assertThatThrownBy(() -> veiculoService.atualizarStatus(id, StatusVeiculo.VENDIDO))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Transição de status inválida");

        verify(veiculoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve listar catálogo público com status DISPONIVEL e limpar TenantContext no final")
    void shouldListPublicCatalogAndCleanTenantContext() {
        UUID lojaId = UUID.randomUUID();
        Veiculo veiculoDisponivel = buildVeiculoMock();
        veiculoDisponivel.setStatus(StatusVeiculo.DISPONIVEL);

        Page<Veiculo> paginaMock = new PageImpl<>(List.of(veiculoDisponivel));
        when(veiculoRepository.findByStatus(eq(StatusVeiculo.DISPONIVEL), any())).thenReturn(paginaMock);

        Page<CatalogoPublicoResponse> resultado = veiculoService.listarCatalogoPublico(lojaId, PageRequest.of(0, 10));

        assertThat(resultado.getTotalElements()).isEqualTo(1);
        assertThat(resultado.getContent().getFirst().modelo()).isEqualTo("Corolla");

        // Regra de segurança crítica: o TenantContext DEVE estar limpo após a execução!
        assertThat(TenantContext.getTenantId()).isNull();
    }
}
