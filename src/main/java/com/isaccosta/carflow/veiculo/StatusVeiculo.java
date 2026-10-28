package com.isaccosta.carflow.veiculo;

import java.util.Set;

public enum StatusVeiculo {

    EM_PREPARACAO {
        @Override
        public boolean canTransitionTo(StatusVeiculo newStatus) {
            return Set.of(DISPONIVEL).contains(newStatus);
        }
    },

    DISPONIVEL {
        @Override
        public boolean canTransitionTo(StatusVeiculo newStatus) {
            return Set.of(RESERVADO, VENDIDO, EM_PREPARACAO).contains(newStatus);
        }
    },

    RESERVADO {
        @Override
        public boolean canTransitionTo(StatusVeiculo newStatus) {
            return Set.of(DISPONIVEL, VENDIDO).contains(newStatus);
        }
    },

    VENDIDO {
        @Override
        public boolean canTransitionTo(StatusVeiculo newStatus) {
            // Estado terminal: nenhuma transição permitida
            return false;
        }
    };

    public abstract boolean canTransitionTo(StatusVeiculo newStatus);
}
