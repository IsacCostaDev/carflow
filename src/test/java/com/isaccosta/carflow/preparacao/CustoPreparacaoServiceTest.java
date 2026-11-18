package com.isaccosta.carflow.preparacao;

import com.isaccosta.carflow.preparacao.dto.CadastroCustoRequest;
import com.isaccosta.carflow.preparacao.dto.CustoResponse;
import com.isaccosta.carflow.preparacao.dto.ResumoFinanceiroVeiculoResponse;
import com.isaccosta.carflow.veiculo.StatusVeiculo;
import com.isaccosta.carflow.veiculo.Veiculo;
import com.isaccosta.carflow.veiculo.VeiculoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustoPreparacaoServiceTest {

    @Mock
    private CustoPreparacaoRepository custoPreparacaoRepository;

    @Mock
    private VeiculoRepository veiculoRepository;

    @InjectMocks
    private CustoPreparacaoService custoPreparacaoService;

    private Veiculo buildVeiculo(StatusVeiculo status) {
        return Veiculo.builder()
                .id(UUID.randomUUID())
                .lojaId(UUID.randomUUID())
                .marca("Honda")
                .modelo("Civic")
                .placa("CIV2024")
                .precoCompra(new BigDecimal("100000.00"))
                .precoAnuncio(new BigDecimal("120000.00"))
                .status(status)
                .build();
    }

    private CadastroCustoRequest buildRequest() {
        return new CadastroCustoRequest(
                CategoriaCusto.MANUTENCAO,
                "Troca de óleo e filtros",
                new BigDecimal("650.00"),
                LocalDate.now(),
                "Auto Mecânica Central");
    }

    @Test
    @DisplayName("Deve cadastrar custo de preparação com sucesso em veículo em preparação")
    void shouldAdicionarCustoComSucesso() {
        Veiculo veiculo = buildVeiculo(StatusVeiculo.EM_PREPARACAO);
        CadastroCustoRequest request = buildRequest();

        when(veiculoRepository.findById(veiculo.getId())).thenReturn(Optional.of(veiculo));
        when(custoPreparacaoRepository.save(any(CustoPreparacao.class))).thenAnswer(invocation -> {
            CustoPreparacao entity = invocation.getArgument(0);
            entity.setId(UUID.randomUUID());
            entity.setCreatedAt(Instant.now());
            return entity;
        });

        CustoResponse response = custoPreparacaoService.adicionarCusto(veiculo.getId(), request);

        assertThat(response).isNotNull();
        assertThat(response.veiculoId()).isEqualTo(veiculo.getId());
        assertThat(response.descricao()).isEqualTo("Troca de óleo e filtros");
        assertThat(response.valor()).isEqualByComparingTo("650.00");
        assertThat(response.categoria()).isEqualTo(CategoriaCusto.MANUTENCAO);

        verify(custoPreparacaoRepository).save(any(CustoPreparacao.class));
    }

    @Test
    @DisplayName("Deve impedir lançamento de despesa em veículo já vendido")
    void shouldFailWhenAdicionarCustoEmVeiculoVendido() {
        Veiculo veiculoVendido = buildVeiculo(StatusVeiculo.VENDIDO);
        CadastroCustoRequest request = buildRequest();

        when(veiculoRepository.findById(veiculoVendido.getId())).thenReturn(Optional.of(veiculoVendido));

        assertThatThrownBy(() -> custoPreparacaoService.adicionarCusto(veiculoVendido.getId(), request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Não é permitido lançar custos de preparação para veículo já vendido.");

        verify(custoPreparacaoRepository, never()).save(any(CustoPreparacao.class));
    }

    @Test
    @DisplayName("Deve falhar ao tentar adicionar custo para veículo inexistente")
    void shouldFailWhenAdicionarCustoEmVeiculoInexistente() {
        UUID idInexistente = UUID.randomUUID();
        CadastroCustoRequest request = buildRequest();

        when(veiculoRepository.findById(idInexistente)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> custoPreparacaoService.adicionarCusto(idInexistente, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Veículo não encontrado");

        verify(custoPreparacaoRepository, never()).save(any(CustoPreparacao.class));
    }

    @Test
    @DisplayName("Deve listar os custos do veículo com sucesso")
    void shouldListarCustosPorVeiculoComSucesso() {
        UUID veiculoId = UUID.randomUUID();
        Veiculo veiculo = buildVeiculo(StatusVeiculo.DISPONIVEL);
        veiculo.setId(veiculoId);

        CustoPreparacao custo1 = CustoPreparacao.builder()
                .id(UUID.randomUUID())
                .veiculo(veiculo)
                .categoria(CategoriaCusto.ESTETICA)
                .descricao("Higienização interna")
                .valor(new BigDecimal("400.00"))
                .dataRealizacao(LocalDate.now())
                .createdAt(Instant.now())
                .build();

        when(veiculoRepository.existsById(veiculoId)).thenReturn(true);
        when(custoPreparacaoRepository.findByVeiculoIdOrderByDataRealizacaoDesc(veiculoId))
                .thenReturn(List.of(custo1));

        List<CustoResponse> lista = custoPreparacaoService.listarCustosPorVeiculo(veiculoId);

        assertThat(lista).hasSize(1);
        assertThat(lista.getFirst().descricao()).isEqualTo("Higienização interna");
    }

    @Test
    @DisplayName("Deve falhar ao listar custos de um veículo inexistente")
    void shouldFailWhenListarCustosDeVeiculoInexistente() {
        UUID idInexistente = UUID.randomUUID();
        when(veiculoRepository.existsById(idInexistente)).thenReturn(false);

        assertThatThrownBy(() -> custoPreparacaoService.listarCustosPorVeiculo(idInexistente))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Veículo não encontrado");
    }

    @Test
    @DisplayName("Deve calcular resumo financeiro com Custo Real e Margem Projetada exatos")
    void shouldObterResumoFinanceiroComCalculosPrecisos() {
        Veiculo veiculo = buildVeiculo(StatusVeiculo.DISPONIVEL);
        // Preço compra = 100.000,00 | Preço anúncio = 120.000,00
        BigDecimal custosPreparacao = new BigDecimal("8500.50");

        when(veiculoRepository.findById(veiculo.getId())).thenReturn(Optional.of(veiculo));
        when(custoPreparacaoRepository.sumValorByVeiculoId(veiculo.getId())).thenReturn(custosPreparacao);

        ResumoFinanceiroVeiculoResponse resumo = custoPreparacaoService.obterResumoFinanceiro(veiculo.getId());

        assertThat(resumo).isNotNull();
        assertThat(resumo.veiculoId()).isEqualTo(veiculo.getId());
        assertThat(resumo.precoCompra()).isEqualByComparingTo("100000.00");
        assertThat(resumo.totalPreparacao()).isEqualByComparingTo("8500.50");
        // Custo Real = 100.000,00 + 8.500,50 = 108.500,50
        assertThat(resumo.custoReal()).isEqualByComparingTo("108500.50");
        // Margem Projetada = 120.000,00 - 108.500,50 = 11.499,50
        assertThat(resumo.margemProjetada()).isEqualByComparingTo("11499.50");
    }
}