package com.isaccosta.carflow.tenant;

import org.springframework.context.annotation.Import;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TenantIdentifierResolver.class)
class LojaRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private LojaRepository lojaRepository;

    @Test
    @DisplayName("Deve persistir uma Loja com UUID v7 e timestamps gerados via Flyway e JPA")
    void shouldPersistLojaSuccessfully() {
        Loja novaLoja = Loja.builder()
                .razaoSocial("Auto Motors Comercio de Veiculos Ltda")
                .nomeFantasia("Auto Motors")
                .cnpj("12345678000199")
                .telefone("11987654321")
                .slug("auto-motors")
                .build();

        Loja lojaSalva = lojaRepository.save(novaLoja);

        assertThat(lojaSalva.getId()).isNotNull();
        assertThat(lojaSalva.getCreatedAt()).isNotNull();
        assertThat(lojaSalva.getUpdatedAt()).isNotNull();
        assertThat(lojaSalva.isAtivo()).isTrue();

        Optional<Loja> lojaEncontrada = lojaRepository.findBySlug("auto-motors");
        assertThat(lojaEncontrada).isPresent();
        assertThat(lojaEncontrada.get().getNomeFantasia()).isEqualTo("Auto Motors");

        assertThat(lojaRepository.existsByCnpj("12345678000199")).isTrue();
        assertThat(lojaRepository.existsBySlug("auto-motors")).isTrue();
        assertThat(lojaRepository.existsBySlug("loja-inexistente")).isFalse();
    }
}
