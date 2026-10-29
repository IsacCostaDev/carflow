package com.isaccosta.carflow.veiculo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OpcionalRepository extends JpaRepository<Opcional, UUID> {

    Optional<Opcional> findByNome(String nome);

    boolean existsByNome(String nome);
}
