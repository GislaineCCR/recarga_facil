package com.recargafacil.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.recargafacil.dto.CadastroHostDTO;
import com.recargafacil.dto.CadastroMotoristaDTO;
import com.recargafacil.dto.LoginRequest;
import com.recargafacil.dto.PagamentoDTO;
import com.recargafacil.dto.ReservaDTO;
import com.recargafacil.model.Host;
import com.recargafacil.model.Motorista;
import com.recargafacil.model.Veiculo;
import com.recargafacil.model.Wallbox;
import com.recargafacil.repository.WallboxRepository;
import com.recargafacil.service.HostService;
import com.recargafacil.service.MotoristaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
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

    @TestConfiguration
    static class TestConfig {
        @Bean
        public static BeanDefinitionRegistryPostProcessor disableDataInitializer() {
            return new BeanDefinitionRegistryPostProcessor() {
                @Override
                public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) {
                    if (registry.containsBeanDefinition("dataInitializer")) {
                        registry.removeBeanDefinition("dataInitializer");
                    }
                }

                @Override
                public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
                }
            };
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private HostService hostService;

    @Autowired
    private MotoristaService motoristaService;

    @Autowired
    private WallboxRepository wallboxRepository;

    private static boolean dadosInicializados = false;
    private static Long idMotoristaPadrao;
    private static Long idWallboxPadrao;

    @BeforeEach
    void setUp() {
        if (!dadosInicializados) {
            // 1. Cadastra Host + Local + Wallbox via serviço real (RF03 / RF04)
            CadastroHostDTO hostDto = new CadastroHostDTO();
            hostDto.setNome("Carlos Eduardo M.");
            hostDto.setEmail("carlos@recargafacil.com");
            hostDto.setSenha("senha123");
            hostDto.setCpf("321.654.987-11");
            hostDto.setTelefone("(15) 99123-8899");
            hostDto.setDadosBancarios("PIX: carlos@recargafacil.com");
            hostDto.setModelo("Wallbox EcoCharge Campolim 7.4kW");
            hostDto.setPotencia(Wallbox.Potencia.P_7KW);
            hostDto.setPrecoHora(new BigDecimal("15.00"));
            hostDto.setStatus(Wallbox.Status.DISPONIVEL);
            hostDto.setEnderecoCompleto("Rua Antonio Perez Hernandez, 480 - Campolim, Sorocaba - SP");
            Host host = hostService.cadastrarComWallbox(hostDto);

            idWallboxPadrao = wallboxRepository.findByHostId(host.getId()).get(0).getId();

            // 2. Cadastra Motorista Gislaine + Veículo via serviço real (RF01 / RF02)
            CadastroMotoristaDTO gislaineDto = new CadastroMotoristaDTO();
            gislaineDto.setNome("Gislaine Cristina Cesario Rodrigues");
            gislaineDto.setEmail("gislaine.c.cesario@gmail.com");
            gislaineDto.setSenha("1234");
            gislaineDto.setCpf("36616646633");
            gislaineDto.setTelefone("(11) 99269-6670");
            gislaineDto.setMarca("BYD");
            gislaineDto.setModelo("Song Plus DM-i");
            gislaineDto.setPlaca("GIS2026");
            gislaineDto.setTipoConector(Veiculo.TipoConector.TIPO_2);
            Motorista motorista = motoristaService.cadastrarComVeiculo(gislaineDto);
            idMotoristaPadrao = motorista.getId();

            // 3. Cadastra Motorista exclusivo (sem perfil de Host) para teste de autorização (CT18)
            CadastroMotoristaDTO motoristaDemoDto = new CadastroMotoristaDTO();
            motoristaDemoDto.setNome("Motorista Demo EV");
            motoristaDemoDto.setEmail("motorista@email.com");
            motoristaDemoDto.setSenha("senha123");
            motoristaDemoDto.setCpf("123.456.789-00");
            motoristaDemoDto.setTelefone("(15) 99876-5432");
            motoristaDemoDto.setMarca("BYD");
            motoristaDemoDto.setModelo("Dolphin EV");
            motoristaDemoDto.setPlaca("BRA2E19");
            motoristaService.cadastrarComVeiculo(motoristaDemoDto);

            dadosInicializados = true;
        }
    }

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
        login.setEmail("gislaine.c.cesario@gmail.com");
        login.setSenha("1234");

        mockMvc.perform(post("/api/auth/login-motorista")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("MOTORISTA"))
                .andExpect(jsonPath("$.email").value("gislaine.c.cesario@gmail.com"));
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
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].modelo", notNullValue()))
                .andExpect(jsonPath("$[0].status").value("DISPONIVEL"));
    }

    @Test
    @DisplayName("CT20 - Fluxo Integrado Ponta a Ponta: Agendar Reserva (RF07) e Processar Pagamento (RF08/RF09)")
    void deveExecutarFluxoCompletoDeReservaEPagamento() throws Exception {
        // 1. Criar nova Reserva (RF07)
        ReservaDTO novaReserva = new ReservaDTO();
        novaReserva.setIdMotorista(idMotoristaPadrao);
        novaReserva.setIdWallbox(idWallboxPadrao);
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
