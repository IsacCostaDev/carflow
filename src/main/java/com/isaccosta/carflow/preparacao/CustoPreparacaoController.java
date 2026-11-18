package com.isaccosta.carflow.preparacao;

import com.isaccosta.carflow.preparacao.dto.CadastroCustoRequest;
import com.isaccosta.carflow.preparacao.dto.CustoResponse;
import com.isaccosta.carflow.preparacao.dto.ResumoFinanceiroVeiculoResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/veiculos/{veiculoId}")
public class CustoPreparacaoController {

    private final CustoPreparacaoService custoPreparacaoService;

    public CustoPreparacaoController(CustoPreparacaoService custoPreparacaoService) {
        this.custoPreparacaoService = custoPreparacaoService;
    }

    @PostMapping("/custos")
    public ResponseEntity<CustoResponse> adicionarCusto(
            @PathVariable UUID veiculoId,
            @RequestBody @Valid CadastroCustoRequest request) {
        CustoResponse response = custoPreparacaoService.adicionarCusto(veiculoId, request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/custos")
    public ResponseEntity<List<CustoResponse>> listarCustos(@PathVariable UUID veiculoId) {
        return ResponseEntity.ok(custoPreparacaoService.listarCustosPorVeiculo(veiculoId));
    }

    @GetMapping("/financeiro-resumo")
    public ResponseEntity<ResumoFinanceiroVeiculoResponse> obterResumoFinanceiro(@PathVariable UUID veiculoId) {
        return ResponseEntity.ok(custoPreparacaoService.obterResumoFinanceiro(veiculoId));
    }
}