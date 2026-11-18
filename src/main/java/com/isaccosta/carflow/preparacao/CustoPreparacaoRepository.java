package com.isaccosta.carflow.preparacao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface CustoPreparacaoRepository extends JpaRepository<CustoPreparacao, UUID> {

    List<CustoPreparacao> findByVeiculoIdOrderByDataRealizacaoDesc(UUID veiculoId);

    @Query("SELECT COALESCE(SUM(c.valor), 0) FROM CustoPreparacao c WHERE c.veiculo.id = :veiculoId")
    BigDecimal sumValorByVeiculoId(@Param("veiculoId") UUID veiculoId);
}