package com.isaccosta.carflow.veiculo;

public enum StatusVeiculo {

    EM_PREPARACAO,
    DISPONIVEL,
    RESERVADO,
    VENDIDO;

    public boolean canTransitionTo(StatusVeiculo newStatus) {
        if (newStatus == null) {
            return false;
        }
        return switch (this) {
            case EM_PREPARACAO -> newStatus == DISPONIVEL;
            case DISPONIVEL -> newStatus == RESERVADO || newStatus == VENDIDO || newStatus == EM_PREPARACAO;
            case RESERVADO -> newStatus == DISPONIVEL || newStatus == VENDIDO;
            case VENDIDO -> false;
        };
    }
}
