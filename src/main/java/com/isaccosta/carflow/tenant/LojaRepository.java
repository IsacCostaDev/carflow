package com.isaccosta.carflow.tenant;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LojaRepository extends JpaRepository<Loja, UUID> {

    Optional<Loja> findBySlug(String slug);

    boolean existsByCnpj(String cnpj);

    boolean existsBySlug(String slug);
}
