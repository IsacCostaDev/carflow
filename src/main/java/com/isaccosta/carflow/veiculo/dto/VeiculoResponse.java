package com.isaccosta.carflow.veiculo.dto;

import com.isaccosta.carflow.veiculo.Cambio;
import com.isaccosta.carflow.veiculo.Combustivel;
import com.isaccosta.carflow.veiculo.Opcional;
import com.isaccosta.carflow.veiculo.StatusVeiculo;
import com.isaccosta.carflow.veiculo.Veiculo;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public record VeiculoResponse(
        UUID id,
        UUID lojaId,
        String marca,
        String modelo,
        String versao,
        Integer anoFabricacao,
        Integer anoModelo,
        String placa,
        String chassi,
        String cor,
        Integer kmAtual,
        Combustivel combustivel,
        Cambio cambio,
        BigDecimal precoCompra,
        BigDecimal precoAnuncio,
        StatusVeiculo status,
        String descricao,
        Long version,
        Set<String> opcionais,
        Instant createdAt,
        Instant updatedAt) {
    public static VeiculoResponse fromEntity(Veiculo v) {
        Set<String> nomesOpcionais = v.getOpcionais() != null
                ? v.getOpcionais().stream().map(Opcional::getNome).collect(Collectors.toSet())
                : Set.of();

        return new VeiculoResponse(
                v.getId(),
                v.getLojaId(),
                v.getMarca(),
                v.getModelo(),
                v.getVersao(),
                v.getAnoFabricacao(),
                v.getAnoModelo(),
                v.getPlaca(),
                v.getChassi(),
                v.getCor(),
                v.getKmAtual(),
                v.getCombustivel(),
                v.getCambio(),
                v.getPrecoCompra(),
                v.getPrecoAnuncio(),
                v.getStatus(),
                v.getDescricao(),
                v.getVersion(),
                nomesOpcionais,
                v.getCreatedAt(),
                v.getUpdatedAt());
    }
}
