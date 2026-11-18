package com.isaccosta.carflow.preparacao.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import com.isaccosta.carflow.preparacao.CategoriaCusto;
import com.isaccosta.carflow.preparacao.CustoPreparacao;

public record CustoResponse(
        UUID id,
        UUID veiculoId,
        CategoriaCusto categoria,
        String descricao,
        BigDecimal valor,
        LocalDate dataRealizacao,
        String fornecedor,
        Instant createdAt) {
    public static CustoResponse fromEntity(CustoPreparacao entity) {
        return new CustoResponse(
                entity.getId(),
                entity.getVeiculo().getId(),
                entity.getCategoria(),
                entity.getDescricao(),
                entity.getValor(),
                entity.getDataRealizacao(),
                entity.getFornecedor(),
                entity.getCreatedAt());
    }
}