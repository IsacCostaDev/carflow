package com.isaccosta.carflow.preparacao;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.isaccosta.carflow.preparacao.dto.CadastroCustoRequest;
import com.isaccosta.carflow.preparacao.dto.CustoResponse;
import com.isaccosta.carflow.preparacao.dto.ResumoFinanceiroVeiculoResponse;
import com.isaccosta.carflow.security.JwtAuthenticationFilter;
import com.isaccosta.carflow.security.JwtService;
import com.isaccosta.carflow.security.SecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CustoPreparacaoController.class)
@Import({ SecurityConfig.class, JwtAuthenticationFilter.class })
class CustoPreparacaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CustoPreparacaoService custoPreparacaoService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    @WithMockUser
    @DisplayName("POST /api/v1/veiculos/{id}/custos - Deve cadastrar despesa e retornar 201 Created com header Location")
    void shouldAdicionarCustoComSucesso() throws Exception {
        UUID veiculoId = UUID.randomUUID();
        UUID custoId = UUID.randomUUID();

        CadastroCustoRequest request = new CadastroCustoRequest(
                CategoriaCusto.MANUTENCAO,
                "Troca de óleo e filtro",
                new BigDecimal("450.00"),
                LocalDate.now(),
                "Oficina Prime");

        CustoResponse response = new CustoResponse(
                custoId,
                veiculoId,
                CategoriaCusto.MANUTENCAO,
                "Troca de óleo e filtro",
                new BigDecimal("450.00"),
                LocalDate.now(),
                "Oficina Prime",
                Instant.now());

        when(custoPreparacaoService.adicionarCusto(eq(veiculoId), any(CadastroCustoRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/veiculos/{veiculoId}/custos", veiculoId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location",
                        "http://localhost/api/v1/veiculos/%s/custos/%s".formatted(veiculoId, custoId)))
                .andExpect(jsonPath("$.id").value(custoId.toString()))
                .andExpect(jsonPath("$.descricao").value("Troca de óleo e filtro"))
                .andExpect(jsonPath("$.valor").value(450.00))
                .andExpect(jsonPath("$.categoria").value("MANUTENCAO"));
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/v1/veiculos/{id}/custos - Deve validar Bean Validation e retornar 400 Bad Request")
    void shouldFailWhenAdicionarCustoComPayloadInvalido() throws Exception {
        UUID veiculoId = UUID.randomUUID();

        CadastroCustoRequest payloadInvalido = new CadastroCustoRequest(
                null, // categoria nula
                "", // descrição vazia
                new BigDecimal("-50.00"), // valor negativo
                LocalDate.now().plusDays(1), // data futura inválida
                null);

        mockMvc.perform(post("/api/v1/veiculos/{veiculoId}/custos", veiculoId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payloadInvalido)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/v1/veiculos/{id}/custos - Deve retornar histórico de despesas com 200 OK")
    void shouldListarCustosPorVeiculo() throws Exception {
        UUID veiculoId = UUID.randomUUID();

        CustoResponse custo = new CustoResponse(
                UUID.randomUUID(),
                veiculoId,
                CategoriaCusto.ESTETICA,
                "Polimento e cristalização",
                new BigDecimal("600.00"),
                LocalDate.now(),
                "Studio Car",
                Instant.now());

        when(custoPreparacaoService.listarCustosPorVeiculo(veiculoId)).thenReturn(List.of(custo));

        mockMvc.perform(get("/api/v1/veiculos/{veiculoId}/custos", veiculoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].categoria").value("ESTETICA"))
                .andExpect(jsonPath("$[0].valor").value(600.00));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/v1/veiculos/{id}/financeiro-resumo - Deve retornar resumo de margem com 200 OK")
    void shouldObterResumoFinanceiro() throws Exception {
        UUID veiculoId = UUID.randomUUID();

        ResumoFinanceiroVeiculoResponse resumo = new ResumoFinanceiroVeiculoResponse(
                veiculoId,
                "Civic Touring",
                "CIV2024",
                new BigDecimal("100000.00"),
                new BigDecimal("5000.00"),
                new BigDecimal("105000.00"),
                new BigDecimal("120000.00"),
                new BigDecimal("15000.00"));

        when(custoPreparacaoService.obterResumoFinanceiro(veiculoId)).thenReturn(resumo);

        mockMvc.perform(get("/api/v1/veiculos/{veiculoId}/financeiro-resumo", veiculoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.modelo").value("Civic Touring"))
                .andExpect(jsonPath("$.precoCompra").value(100000.00))
                .andExpect(jsonPath("$.totalPreparacao").value(5000.00))
                .andExpect(jsonPath("$.custoReal").value(105000.00))
                .andExpect(jsonPath("$.margemProjetada").value(15000.00));
    }

    @Test
    @DisplayName("GET /api/v1/veiculos/{id}/custos - Deve bloquear requisições não autenticadas com 403 Forbidden")
    void shouldBlockUnauthenticatedRequests() throws Exception {
        UUID veiculoId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/veiculos/{veiculoId}/custos", veiculoId))
                .andExpect(status().isForbidden());
    }
}