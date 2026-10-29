package com.isaccosta.carflow.veiculo;

import com.isaccosta.carflow.tenant.TenantContext;
import com.isaccosta.carflow.veiculo.dto.CadastroVeiculoRequest;
import com.isaccosta.carflow.veiculo.dto.CatalogoPublicoResponse;
import com.isaccosta.carflow.veiculo.dto.VeiculoResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
public class VeiculoService {

    private final VeiculoRepository veiculoRepository;
    private final OpcionalRepository opcionalRepository;

    public VeiculoService(VeiculoRepository veiculoRepository, OpcionalRepository opcionalRepository) {
        this.veiculoRepository = veiculoRepository;
        this.opcionalRepository = opcionalRepository;
    }

    @Transactional
    public VeiculoResponse cadastrar(CadastroVeiculoRequest request) {
        String placaFormatada = request.placa().toUpperCase().trim();
        String chassiFormatado = request.chassi().toUpperCase().trim();

        if (veiculoRepository.existsByPlaca(placaFormatada)) {
            throw new IllegalArgumentException(
                    "Já existe um veículo cadastrado com a placa: %s".formatted(placaFormatada));
        }

        if (veiculoRepository.existsByChassi(chassiFormatado)) {
            throw new IllegalArgumentException(
                    "Já existe um veículo cadastrado com o chassi: %s".formatted(chassiFormatado));
        }

        Set<Opcional> opcionais = new HashSet<>();
        if (request.opcionaisIds() != null && !request.opcionaisIds().isEmpty()) {
            opcionais.addAll(opcionalRepository.findAllById(request.opcionaisIds()));
        }

        Veiculo veiculo = Veiculo.builder()
                .marca(request.marca())
                .modelo(request.modelo())
                .versao(request.versao())
                .anoFabricacao(request.anoFabricacao())
                .anoModelo(request.anoModelo())
                .placa(placaFormatada)
                .chassi(chassiFormatado)
                .cor(request.cor())
                .kmAtual(request.kmAtual())
                .combustivel(request.combustivel())
                .cambio(request.cambio())
                .precoCompra(request.precoCompra())
                .precoAnuncio(request.precoAnuncio())
                .status(StatusVeiculo.EM_PREPARACAO)
                .descricao(request.descricao())
                .opcionais(opcionais)
                .build();

        Veiculo salvo = veiculoRepository.save(veiculo);
        return VeiculoResponse.fromEntity(salvo);
    }

    @Transactional(readOnly = true)
    public VeiculoResponse buscarPorId(UUID id) {
        Veiculo veiculo = veiculoRepository.findWithOpcionaisById(id)
                .orElseThrow(() -> new IllegalArgumentException("Veículo não encontrado com o ID: %s".formatted(id)));

        return VeiculoResponse.fromEntity(veiculo);
    }

    @Transactional(readOnly = true)
    public Page<VeiculoResponse> listarEstoque(StatusVeiculo status, Pageable pageable) {
        Page<Veiculo> pagina = (status != null)
                ? veiculoRepository.findByStatus(status, pageable)
                : veiculoRepository.findAll(pageable);

        return pagina.map(VeiculoResponse::fromEntity);
    }

    @Transactional
    public VeiculoResponse atualizarStatus(UUID id, StatusVeiculo novoStatus) {
        Veiculo veiculo = veiculoRepository.findWithOpcionaisById(id)
                .orElseThrow(() -> new IllegalArgumentException("Veículo não encontrado com o ID: %s".formatted(id)));

        veiculo.transitionTo(novoStatus);

        Veiculo atualizado = veiculoRepository.save(veiculo);
        return VeiculoResponse.fromEntity(atualizado);
    }

    @Transactional(readOnly = true)
    public Page<CatalogoPublicoResponse> listarCatalogoPublico(UUID lojaId, Pageable pageable) {
        try {
            TenantContext.setTenantId(lojaId);
            return veiculoRepository.findByStatus(StatusVeiculo.DISPONIVEL, pageable)
                    .map(CatalogoPublicoResponse::fromEntity);
        } finally {
            TenantContext.clear();
        }
    }
}
