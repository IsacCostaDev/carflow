package com.isaccosta.carflow.veiculo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VeiculoTest {

    private Veiculo buildVeiculoValido(StatusVeiculo statusInicial) {
        return Veiculo.builder()
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
                .status(statusInicial)
                .build();
    }

    @Test
    @DisplayName("Deve permitir transição válida de EM_PREPARACAO para DISPONIVEL")
    void shouldAllowTransitionFromEmPreparacaoToDisponivel() {
        Veiculo veiculo = buildVeiculoValido(StatusVeiculo.EM_PREPARACAO);

        veiculo.transitionTo(StatusVeiculo.DISPONIVEL);

        assertThat(veiculo.getStatus()).isEqualTo(StatusVeiculo.DISPONIVEL);
    }

    @Test
    @DisplayName("Deve permitir fluxo completo: DISPONIVEL -> RESERVADO -> VENDIDO")
    void shouldAllowCompleteSaleFlow() {
        Veiculo veiculo = buildVeiculoValido(StatusVeiculo.DISPONIVEL);

        veiculo.transitionTo(StatusVeiculo.RESERVADO);
        assertThat(veiculo.getStatus()).isEqualTo(StatusVeiculo.RESERVADO);

        veiculo.transitionTo(StatusVeiculo.VENDIDO);
        assertThat(veiculo.getStatus()).isEqualTo(StatusVeiculo.VENDIDO);
    }

    @Test
    @DisplayName("Deve permitir cancelamento de reserva voltando para DISPONIVEL")
    void shouldAllowTransitionFromReservadoBackToDisponivel() {
        Veiculo veiculo = buildVeiculoValido(StatusVeiculo.RESERVADO);

        veiculo.transitionTo(StatusVeiculo.DISPONIVEL);

        assertThat(veiculo.getStatus()).isEqualTo(StatusVeiculo.DISPONIVEL);
    }

    @Test
    @DisplayName("Não deve permitir venda direta de veículo que ainda está EM_PREPARACAO")
    void shouldFailWhenTransitioningDirectlyFromEmPreparacaoToVendido() {
        Veiculo veiculo = buildVeiculoValido(StatusVeiculo.EM_PREPARACAO);

        assertThatThrownBy(() -> veiculo.transitionTo(StatusVeiculo.VENDIDO))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Transição de status inválida");
    }

    @Test
    @DisplayName("Não deve permitir nenhuma transição a partir do estado terminal VENDIDO")
    void shouldFailWhenTransitioningFromTerminalStatusVendido() {
        Veiculo veiculo = buildVeiculoValido(StatusVeiculo.VENDIDO);

        assertThatThrownBy(() -> veiculo.transitionTo(StatusVeiculo.DISPONIVEL))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Transição de status inválida");

        assertThatThrownBy(() -> veiculo.transitionTo(StatusVeiculo.EM_PREPARACAO))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Transição de status inválida");
    }

    @Test
    @DisplayName("Deve gerar UUID v7, status EM_PREPARACAO e timestamps no onCreate")
    void shouldInitializeFieldsCorrectlyOnCreate() {
        Veiculo veiculo = buildVeiculoValido(null);

        veiculo.onCreate();

        assertThat(veiculo.getId()).isNotNull();
        assertThat(veiculo.getStatus()).isEqualTo(StatusVeiculo.EM_PREPARACAO);
        assertThat(veiculo.getCreatedAt()).isNotNull();
        assertThat(veiculo.getUpdatedAt()).isNotNull();
    }
}
