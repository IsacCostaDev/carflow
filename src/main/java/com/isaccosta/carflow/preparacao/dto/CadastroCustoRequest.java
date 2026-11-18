package com.isaccosta.carflow.preparacao.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import com.isaccosta.carflow.preparacao.CategoriaCusto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CadastroCustoRequest(
                @NotNull(message = "A categoria é obrigatória") CategoriaCusto categoria,

                @NotBlank(message = "A descrição é obrigatória") String descricao,

                @NotNull(message = "O valor é obrigatório") @Positive(message = "O valor deve ser positivo") BigDecimal valor,

                @NotNull(message = "A data de realização é obrigatória") @PastOrPresent(message = "A data de realização não pode ser futura") LocalDate dataRealizacao,

                String fornecedor) {
}