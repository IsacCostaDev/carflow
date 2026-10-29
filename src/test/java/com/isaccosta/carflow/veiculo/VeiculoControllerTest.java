package com.isaccosta.carflow.veiculo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.isaccosta.carflow.security.JwtAuthenticationFilter;
import com.isaccosta.carflow.security.JwtService;
import com.isaccosta.carflow.security.SecurityConfig;
import com.isaccosta.carflow.veiculo.dto.AtualizarStatusVeiculoRequest;
import com.isaccosta.carflow.veiculo.dto.CadastroVeiculoRequest;
import com.isaccosta.carflow.veiculo.dto.CatalogoPublicoResponse;
import com.isaccosta.carflow.veiculo.dto.VeiculoResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = { VeiculoController.class, CatalogoController.class })
@Import({ SecurityConfig.class, JwtAuthenticationFilter.class })
class VeiculoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private VeiculoService veiculoService;

    @MockitoBean
    private JwtService jwtService;

    private CadastroVeiculoRequest buildRequestValido() {
        return new CadastroVeiculoRequest(
                "Honda",
                "Civic",
                "Touring",
                2022,
                2023,
                "BRA2E19",
                "9BWZZZ377VT000001",
                "Preto",
                20000,
                Combustivel.GASOLINA,
                Cambio.AUTOMATICO,
                new BigDecimal("110000.00"),
                new BigDecimal("125000.00"),
                "Excelente estado",
                Set.of());
    }

    private VeiculoResponse buildResponseMock(UUID id) {
        return new VeiculoResponse(
                id,
                UUID.randomUUID(),
                "Honda",
                "Civic",
                "Touring",
                2022,
                2023,
                "BRA2E19",
                "9BWZZZ377VT000001",
                "Preto",
                20000,
                Combustivel.GASOLINA,
                Cambio.AUTOMATICO,
                new BigDecimal("110000.00"),
                new BigDecimal("125000.00"),
                StatusVeiculo.EM_PREPARACAO,
                "Excelente estado",
                0L,
                Set.of(),
                Instant.now(),
                Instant.now());
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/v1/veiculos - Deve cadastrar veículo e retornar HTTP 201 com Location")
    void shouldRegisterVehicleAndReturn201() throws Exception {
        UUID idGerado = UUID.randomUUID();
        when(veiculoService.cadastrar(any())).thenReturn(buildResponseMock(idGerado));

        mockMvc.perform(post("/api/v1/veiculos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(buildRequestValido())))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/v1/veiculos/" + idGerado))
                .andExpect(jsonPath("$.id").value(idGerado.toString()))
                .andExpect(jsonPath("$.modelo").value("Civic"))
                .andExpect(jsonPath("$.status").value("EM_PREPARACAO"));
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/v1/veiculos - Deve retornar HTTP 400 quando campos obrigatórios estiverem inválidos")
    void shouldReturn400WhenPayloadIsInvalid() throws Exception {
        // Envia campos em branco e valores negativos
        CadastroVeiculoRequest payloadInvalido = new CadastroVeiculoRequest(
                "", // marca vazia
                "", // modelo vazio
                null,
                -2022, // ano negativo
                -2023,
                "", // placa vazia
                "123", // chassi com tamanho menor que 17
                "",
                -10, // km negativo
                null,
                null,
                new BigDecimal("-50.00"), // preço negativo
                new BigDecimal("-100.00"),
                null,
                null);

        mockMvc.perform(post("/api/v1/veiculos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payloadInvalido)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/veiculos - Deve bloquear com 401/403 requisições anônimas")
    void shouldBlockUnauthenticatedRequestsToBackoffice() throws Exception {
        mockMvc.perform(get("/api/v1/veiculos"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/v1/catalogo/{lojaId} - Deve permitir acesso público à vitrine sem login")
    void shouldAllowPublicAccessToCatalogWithoutAuthentication() throws Exception {
        UUID lojaId = UUID.randomUUID();
        CatalogoPublicoResponse catalogoItem = new CatalogoPublicoResponse(
                UUID.randomUUID(),
                "Honda",
                "Civic",
                "Touring",
                2022,
                2023,
                "Preto",
                20000,
                Combustivel.GASOLINA,
                Cambio.AUTOMATICO,
                new BigDecimal("125000.00"),
                "Excelente estado",
                Set.of("Teto Solar"));

        when(veiculoService.listarCatalogoPublico(eq(lojaId), any()))
                .thenReturn(new PageImpl<>(List.of(catalogoItem)));

        mockMvc.perform(get("/api/v1/catalogo/{lojaId}", lojaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].modelo").value("Civic"))
                .andExpect(jsonPath("$.content[0].precoAnuncio").value(125000.00))
                // Garantia que dado sigiloso não vaza
                .andExpect(jsonPath("$.content[0].precoCompra").doesNotExist());
    }

    @Test
    @WithMockUser
    @DisplayName("PATCH /api/v1/veiculos/{id}/status - Deve atualizar status com sucesso")
    void shouldUpdateStatusSuccessfully() throws Exception {
        UUID id = UUID.randomUUID();
        VeiculoResponse responseAtualizado = buildResponseMock(id);

        when(veiculoService.atualizarStatus(eq(id), eq(StatusVeiculo.DISPONIVEL))).thenReturn(responseAtualizado);

        AtualizarStatusVeiculoRequest request = new AtualizarStatusVeiculoRequest(StatusVeiculo.DISPONIVEL);

        mockMvc.perform(patch("/api/v1/veiculos/{id}/status", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}