package com.isaccosta.carflow.veiculo;

import com.isaccosta.carflow.veiculo.dto.AtualizarStatusVeiculoRequest;
import com.isaccosta.carflow.veiculo.dto.CadastroVeiculoRequest;
import com.isaccosta.carflow.veiculo.dto.VeiculoResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/veiculos")
public class VeiculoController {

    private final VeiculoService veiculoService;

    public VeiculoController(VeiculoService veiculoService) {
        this.veiculoService = veiculoService;
    }

    @PostMapping
    public ResponseEntity<VeiculoResponse> cadastrar(@RequestBody @Valid CadastroVeiculoRequest request) {
        VeiculoResponse response = veiculoService.cadastrar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<VeiculoResponse>> listar(
            @RequestParam(required = false) StatusVeiculo status,
            Pageable pageable) {
        return ResponseEntity.ok(veiculoService.listarEstoque(status, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VeiculoResponse> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(veiculoService.buscarPorId(id));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<VeiculoResponse> atualizarStatus(
            @PathVariable UUID id,
            @RequestBody @Valid AtualizarStatusVeiculoRequest request) {
        return ResponseEntity.ok(veiculoService.atualizarStatus(id, request.novoStatus()));
    }
}