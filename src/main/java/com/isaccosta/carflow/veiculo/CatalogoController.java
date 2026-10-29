package com.isaccosta.carflow.veiculo;

import com.isaccosta.carflow.veiculo.dto.CatalogoPublicoResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/catalogo")
public class CatalogoController {

    private final VeiculoService veiculoService;

    public CatalogoController(VeiculoService veiculoService) {
        this.veiculoService = veiculoService;
    }

    @GetMapping("/{lojaId}")
    public ResponseEntity<Page<CatalogoPublicoResponse>> listarCatalogo(
            @PathVariable UUID lojaId,
            Pageable pageable) {
        return ResponseEntity.ok(veiculoService.listarCatalogoPublico(lojaId, pageable));
    }
}