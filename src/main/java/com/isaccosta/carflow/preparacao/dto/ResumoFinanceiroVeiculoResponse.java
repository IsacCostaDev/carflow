package com.isaccosta.carflow.preparacao.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ResumoFinanceiroVeiculoResponse(
                UUID veiculoId,
                String modelo,
                String placa,
                BigDecimal precoCompra,
                BigDecimal totalPreparacao,
                BigDecimal custoReal,
                BigDecimal precoAnuncio,
                BigDecimal margemProjetada) {
}