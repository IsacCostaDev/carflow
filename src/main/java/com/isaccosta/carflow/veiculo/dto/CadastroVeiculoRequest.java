package com.isaccosta.carflow.veiculo.dto;

import com.isaccosta.carflow.veiculo.Cambio;
import com.isaccosta.carflow.veiculo.Combustivel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

public record CadastroVeiculoRequest(
        @NotBlank(message = "A marca é obrigatória") @Size(max = 50, message = "A marca deve ter no máximo 50 caracteres") String marca,

        @NotBlank(message = "O modelo é obrigatório") @Size(max = 100, message = "O modelo deve ter no máximo 100 caracteres") String modelo,

        @Size(max = 100, message = "A versão deve ter no máximo 100 caracteres") String versao,

        @NotNull(message = "O ano de fabricação é obrigatório") @Positive(message = "Ano de fabricação inválido") Integer anoFabricacao,

        @NotNull(message = "O ano do modelo é obrigatório") @Positive(message = "Ano do modelo inválido") Integer anoModelo,

        @NotBlank(message = "A placa é obrigatória") @Size(max = 10, message = "A placa deve ter no máximo 10 caracteres") String placa,

        @NotBlank(message = "O chassi é obrigatório") @Size(min = 17, max = 17, message = "O chassi deve ter exatamente 17 caracteres") String chassi,

        @NotBlank(message = "A cor é obrigatória") @Size(max = 30, message = "A cor deve ter no máximo 30 caracteres") String cor,

        @NotNull(message = "O km atual é obrigatório") @PositiveOrZero(message = "O km atual não pode ser negativo") Integer kmAtual,

        @NotNull(message = "O combustível é obrigatório") Combustivel combustivel,

        @NotNull(message = "O câmbio é obrigatório") Cambio cambio,

        @NotNull(message = "O preço de compra é obrigatório") @Positive(message = "O preço de compra deve ser maior que zero") BigDecimal precoCompra,

        @NotNull(message = "O preço de anúncio é obrigatório") @Positive(message = "O preço de anúncio deve ser maior que zero") BigDecimal precoAnuncio,

        String descricao,

        Set<UUID> opcionaisIds) {
}
