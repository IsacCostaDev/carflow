package com.isaccosta.carflow.common.util;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.TimeBasedEpochGenerator;

import java.util.UUID;

public final class IdGenerator {

    // Instância única estática e thread-safe do gerador UUID v7
    private static final TimeBasedEpochGenerator GENERATOR = Generators.timeBasedEpochGenerator();

    // Construtor privado para evitar que a classe seja instanciada
    private IdGenerator() {
    }

    /**
     * Gera um novo UUID v7 ordenado cronologicamente por milissegundos.
     */
    public static UUID v7() {
        return GENERATOR.generate();
    }
}
