package com.isaccosta.carflow.veiculo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface VeiculoRepository extends JpaRepository<Veiculo, UUID> {

    boolean existsByPlaca(String placa);

    boolean existsByChassi(String chassi);

    Optional<Veiculo> findByPlaca(String placa);

    Page<Veiculo> findByStatus(StatusVeiculo status, Pageable pageable);

    @EntityGraph(attributePaths = "opcionais")
    @Query("SELECT v FROM Veiculo v WHERE v.id = :id")
    Optional<Veiculo> findWithOpcionaisById(UUID id);

}
