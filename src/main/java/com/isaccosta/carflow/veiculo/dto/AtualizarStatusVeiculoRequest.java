package com.isaccosta.carflow.veiculo.dto;

import com.isaccosta.carflow.veiculo.StatusVeiculo;
import jakarta.validation.constraints.NotNull;

public record AtualizarStatusVeiculoRequest(
        @NotNull(message = "O novo status é obrigatório") StatusVeiculo novoStatus) {
}
