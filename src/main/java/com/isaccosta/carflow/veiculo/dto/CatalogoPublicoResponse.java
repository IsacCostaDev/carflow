package com.isaccosta.carflow.veiculo.dto;

import com.isaccosta.carflow.veiculo.Cambio;
import com.isaccosta.carflow.veiculo.Combustivel;
import com.isaccosta.carflow.veiculo.Opcional;
import com.isaccosta.carflow.veiculo.Veiculo;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public record CatalogoPublicoResponse(
        UUID id,
        String marca,
        String modelo,
        String versao,
        Integer anoFabricacao,
        Integer anoModelo,
        String cor,
        Integer kmAtual,
        Combustivel combustivel,
        Cambio cambio,
        BigDecimal precoAnuncio,
        String descricao,
        Set<String> opcionais) {
    public static CatalogoPublicoResponse fromEntity(Veiculo v) {
        Set<String> nomesOpcionais = v.getOpcionais() != null
                ? v.getOpcionais().stream().map(Opcional::getNome).collect(Collectors.toSet())
                : Set.of();

        return new CatalogoPublicoResponse(
                v.getId(),
                v.getMarca(),
                v.getModelo(),
                v.getVersao(),
                v.getAnoFabricacao(),
                v.getAnoModelo(),
                v.getCor(),
                v.getKmAtual(),
                v.getCombustivel(),
                v.getCambio(),
                v.getPrecoAnuncio(),
                v.getDescricao(),
                nomesOpcionais);
    }
}
