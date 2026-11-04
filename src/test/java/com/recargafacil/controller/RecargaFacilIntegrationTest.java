package com.recargafacil.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.recargafacil.dto.LoginRequest;
import com.recargafacil.dto.PagamentoDTO;
import com.recargafacil.dto.ReservaDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("h2")
@DisplayName("Testes de Integração da API REST - Recarga Fácil (RF01 a RF09)")
class RecargaFacilIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("CT15 - Deve autenticar Anfitrião (Host) com credenciais válidas (200 OK)")
    void deveAutenticarHostComSucesso() throws Exception {
        LoginRequest login = new LoginRequest();
        login.setEmail("carlos@recargafacil.com");
        login.setSenha("senha123");

        mockMvc.perform(post("/api/auth/login-host")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("HOST"))
                .andExpect(jsonPath("$.email").value("carlos@recargafacil.com"));
    }

    @Test
    @DisplayName("CT16 - Deve autenticar Motorista com credenciais válidas (200 OK)")
    void deveAutenticarMotoristaComSucesso() throws Exception {
        LoginRequest login = new LoginRequest();
        login.setEmail("gislainecesario@recargafacil.com");
        login.setSenha("1234");

        mockMvc.perform(post("/api/auth/login-motorista")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("MOTORISTA"))
                .andExpect(jsonPath("$.email").value("gislainecesario@recargafacil.com"));
    }

    @Test
    @DisplayName("CT17 - Deve rejeitar login com senha incorreta retornando 401 Unauthorized")
    void deveRejeitarLoginComSenhaIncorreta() throws Exception {
        LoginRequest login = new LoginRequest();
        login.setEmail("carlos@recargafacil.com");
        login.setSenha("senhaErrada999");

        mockMvc.perform(post("/api/auth/login-host")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error", containsString("incorretos")));
    }

    @Test
    @DisplayName("CT18 - Deve bloquear Motorista sem perfil de Host ao tentar logar no painel de Anfitrião (403 Forbidden)")
    void deveBloquearMotoristaSemPerfilHostNoLoginHost() throws Exception {
        LoginRequest login = new LoginRequest();
        login.setEmail("motorista@email.com");
        login.setSenha("senha123");

        mockMvc.perform(post("/api/auth/login-host")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error", containsString("não possui perfil de Anfitrião")));
    }

    @Test
    @DisplayName("CT19 - Deve listar as estações de recarga (Wallboxes) disponíveis (RF06)")
    void deveListarEstacoesDeRecargaDisponiveis() throws Exception {
        mockMvc.perform(get("/api/wallboxes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(3))))
                .andExpect(jsonPath("$[0].modelo", notNullValue()))
                .andExpect(jsonPath("$[0].status").value("DISPONIVEL"));
    }

    @Test
    @DisplayName("CT20 - Fluxo Integrado Ponta a Ponta: Agendar Reserva (RF07) e Processar Pagamento (RF08/RF09)")
    void deveExecutarFluxoCompletoDeReservaEPagamento() throws Exception {
        // 1. Criar nova Reserva (RF07)
        ReservaDTO novaReserva = new ReservaDTO();
        novaReserva.setIdMotorista(1L);
        novaReserva.setIdWallbox(1L);
        novaReserva.setIdVeiculo(1L);
        novaReserva.setDataReserva(LocalDate.now().plusDays(1));
        novaReserva.setHorario(LocalTime.of(16, 0));

        MvcResult reservaResult = mockMvc.perform(post("/api/reservas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(novaReserva)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusReserva").value("PENDENTE"))
                .andReturn();

        JsonNode reservaJson = objectMapper.readTree(reservaResult.getResponse().getContentAsString());
        Long idReservaCriada = reservaJson.get("id").asLong();

        // 2. Processar Pagamento da Reserva (RF08)
        PagamentoDTO pagamentoDTO = new PagamentoDTO();
        pagamentoDTO.setIdReserva(idReservaCriada);
        pagamentoDTO.setValorTotal(new BigDecimal("40.00"));

        mockMvc.perform(post("/api/pagamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pagamentoDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusPagamento").value("APROVADO"))
                .andExpect(jsonPath("$.valorTotal").value(40.00))
                .andExpect(jsonPath("$.taxaSistema").value(10.00)); // 25% de 40.00

        // 3. Verificar que a Reserva passou para CONCLUIDA (RF09)
        mockMvc.perform(get("/api/reservas/" + idReservaCriada))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusReserva").value("CONCLUIDA"));
    }
}
