package com.isaccosta.carflow.preparacao;

import com.isaccosta.carflow.preparacao.dto.CadastroCustoRequest;
import com.isaccosta.carflow.preparacao.dto.CustoResponse;
import com.isaccosta.carflow.preparacao.dto.ResumoFinanceiroVeiculoResponse;
import com.isaccosta.carflow.veiculo.StatusVeiculo;
import com.isaccosta.carflow.veiculo.Veiculo;
import com.isaccosta.carflow.veiculo.VeiculoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class CustoPreparacaoService {

    private final CustoPreparacaoRepository custoPreparacaoRepository;
    private final VeiculoRepository veiculoRepository;

    public CustoPreparacaoService(CustoPreparacaoRepository custoPreparacaoRepository,
            VeiculoRepository veiculoRepository) {
        this.custoPreparacaoRepository = custoPreparacaoRepository;
        this.veiculoRepository = veiculoRepository;
    }

    @Transactional
    public CustoResponse adicionarCusto(UUID veiculoId, CadastroCustoRequest request) {
        Veiculo veiculo = veiculoRepository.findById(veiculoId)
                .orElseThrow(
                        () -> new IllegalArgumentException("Veículo não encontrado com o ID: %s".formatted(veiculoId)));

        if (veiculo.getStatus() == StatusVeiculo.VENDIDO) {
            throw new IllegalStateException(
                    "Não é permitido lançar custos de preparação para veículo já vendido.");
        }

        CustoPreparacao custo = CustoPreparacao.builder()
                .veiculo(veiculo)
                .categoria(request.categoria())
                .descricao(request.descricao().trim())
                .valor(request.valor())
                .dataRealizacao(request.dataRealizacao())
                .fornecedor(request.fornecedor() != null ? request.fornecedor().trim() : null)
                .build();

        CustoPreparacao salvo = custoPreparacaoRepository.save(custo);
        return CustoResponse.fromEntity(salvo);
    }

    @Transactional(readOnly = true)
    public List<CustoResponse> listarCustosPorVeiculo(UUID veiculoId) {
        if (!veiculoRepository.existsById(veiculoId)) {
            throw new IllegalArgumentException("Veículo não encontrado com o ID: %s".formatted(veiculoId));
        }

        return custoPreparacaoRepository.findByVeiculoIdOrderByDataRealizacaoDesc(veiculoId)
                .stream()
                .map(CustoResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public ResumoFinanceiroVeiculoResponse obterResumoFinanceiro(UUID veiculoId) {
        Veiculo veiculo = veiculoRepository.findById(veiculoId)
                .orElseThrow(
                        () -> new IllegalArgumentException("Veículo não encontrado com o ID: %s".formatted(veiculoId)));

        BigDecimal totalPreparacao = custoPreparacaoRepository.sumValorByVeiculoId(veiculoId);
        BigDecimal custoReal = veiculo.getPrecoCompra().add(totalPreparacao);
        BigDecimal margemProjetada = veiculo.getPrecoAnuncio().subtract(custoReal);

        return new ResumoFinanceiroVeiculoResponse(
                veiculo.getId(),
                veiculo.getModelo(),
                veiculo.getPlaca(),
                veiculo.getPrecoCompra(),
                totalPreparacao,
                custoReal,
                veiculo.getPrecoAnuncio(),
                margemProjetada);
    }
}