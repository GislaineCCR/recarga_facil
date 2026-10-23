package com.recargafacil.service;

import com.recargafacil.dto.AgendaDTO;
import com.recargafacil.dto.CadastroHostDTO;
import com.recargafacil.dto.CadastroMotoristaDTO;
import com.recargafacil.dto.VeiculoDTO;
import com.recargafacil.model.*;
import com.recargafacil.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários - Serviços de Cadastro e Disponibilidade (RF01 a RF05)")
class CadastroServiceTest {

    @Mock
    private MotoristaRepository motoristaRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private VeiculoRepository veiculoRepository;
    @Mock
    private HostRepository hostRepository;
    @Mock
    private WallboxRepository wallboxRepository;
    @Mock
    private AgendaRepository agendaRepository;

    @InjectMocks
    private MotoristaService motoristaService;

    @InjectMocks
    private VeiculoService veiculoService;

    @InjectMocks
    private HostService hostService;

    @InjectMocks
    private AgendaService agendaService;

    @Test
    @DisplayName("CT11 - Deve cadastrar Motorista com Veículo e padronizar placa em maiúsculas (RF01 e RF02)")
    void deveCadastrarMotoristaComVeiculo() {
        CadastroMotoristaDTO dto = new CadastroMotoristaDTO();
        dto.setNome("João Motorista");
        dto.setEmail("joao@ev.com");
        dto.setCpf("111.222.333-44");
        dto.setTelefone("(15) 99999-1111");
        dto.setSenha("senhaSegura");
        dto.setMarca("BYD");
        dto.setModelo("Dolphin Mini");
        dto.setPlaca("abc1d23");
        dto.setTipoConector(Veiculo.TipoConector.TIPO_2);

        when(usuarioRepository.findFirstByEmail("joao@ev.com")).thenReturn(Optional.empty());
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));
        when(motoristaRepository.save(any(Motorista.class))).thenAnswer(inv -> inv.getArgument(0));
        when(veiculoRepository.save(any(Veiculo.class))).thenAnswer(inv -> inv.getArgument(0));

        Motorista motorista = motoristaService.cadastrarComVeiculo(dto);

        assertNotNull(motorista);
        assertEquals("João Motorista", motorista.getNome());
        assertEquals("joao@ev.com", motorista.getEmail());

        ArgumentCaptor<Veiculo> veiculoCaptor = ArgumentCaptor.forClass(Veiculo.class);
        verify(veiculoRepository, times(1)).save(veiculoCaptor.capture());
        assertEquals("ABC1D23", veiculoCaptor.getValue().getPlaca());
        assertEquals("Dolphin Mini", veiculoCaptor.getValue().getModelo());
    }

    @Test
    @DisplayName("CT12 - Deve cadastrar Veículo adicional vinculado a um Motorista existente (RF02)")
    void deveCadastrarVeiculoParaMotoristaExistente() {
        Motorista motorista = new Motorista();
        motorista.setId(1L);
        motorista.setNome("Gislaine");

        VeiculoDTO dto = new VeiculoDTO();
        dto.setIdMotorista(1L);
        dto.setMarca("Volvo");
        dto.setModelo("EX30");
        dto.setPlaca("GIS3E26");
        dto.setTipoConector(Veiculo.TipoConector.CCS2);

        when(motoristaRepository.findById(1L)).thenReturn(Optional.of(motorista));
        when(veiculoRepository.save(any(Veiculo.class))).thenAnswer(inv -> inv.getArgument(0));

        Veiculo salvo = veiculoService.cadastrar(dto);

        assertNotNull(salvo);
        assertEquals("Volvo", salvo.getMarca());
        assertEquals(Veiculo.TipoConector.CCS2, salvo.getTipoConector());
        assertEquals(motorista, salvo.getMotorista());
    }

    @Test
    @DisplayName("CT13 - Deve cadastrar Anfitrião (Host) junto com Local e estação Wallbox (RF03 e RF04)")
    void deveCadastrarHostComWallboxELocal() {
        CadastroHostDTO dto = new CadastroHostDTO();
        dto.setNome("Anfitrião Solar");
        dto.setEmail("host@solar.com");
        dto.setSenha("123456");
        dto.setDadosBancarios("PIX: host@solar.com");
        dto.setModelo("WEG Wemob Parking 22kW");
        dto.setPotencia(Wallbox.Potencia.P_22KW);
        dto.setPrecoHora(new BigDecimal("18.50"));
        dto.setEnderecoCompleto("Av. General Carneiro, 1000 - Sorocaba/SP");

        when(usuarioRepository.findFirstByEmail("host@solar.com")).thenReturn(Optional.empty());
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));
        when(hostRepository.save(any(Host.class))).thenAnswer(inv -> inv.getArgument(0));
        when(wallboxRepository.save(any(Wallbox.class))).thenAnswer(inv -> inv.getArgument(0));

        Host host = hostService.cadastrarComWallbox(dto);

        assertNotNull(host);
        assertEquals("Anfitrião Solar", host.getNome());

        ArgumentCaptor<Wallbox> wbCaptor = ArgumentCaptor.forClass(Wallbox.class);
        verify(wallboxRepository, times(1)).save(wbCaptor.capture());
        assertEquals("WEG Wemob Parking 22kW", wbCaptor.getValue().getModelo());
        assertEquals(new BigDecimal("18.50"), wbCaptor.getValue().getPrecoHora());
        assertNotNull(wbCaptor.getValue().getLocal());
        assertEquals("Av. General Carneiro, 1000 - Sorocaba/SP", wbCaptor.getValue().getLocal().getEnderecoCompleto());
    }

    @Test
    @DisplayName("CT14 - Deve cadastrar horário de funcionamento na Agenda e alternar status de ativação (RF05)")
    void deveGerenciarDisponibilidadeNaAgenda() {
        Wallbox wb = new Wallbox();
        wb.setId(1L);

        AgendaDTO dto = new AgendaDTO();
        dto.setIdWallbox(1L);
        dto.setDiaSemana(Agenda.DiaSemana.SEGUNDA);
        dto.setHorarioInicio(LocalTime.of(8, 0));
        dto.setHorarioFim(LocalTime.of(18, 0));
        dto.setEstaAtivo(true);

        when(wallboxRepository.findById(1L)).thenReturn(Optional.of(wb));
        when(agendaRepository.save(any(Agenda.class))).thenAnswer(inv -> inv.getArgument(0));

        Agenda agendaCriada = agendaService.cadastrar(dto);
        assertTrue(agendaCriada.getEstaAtivo());
        assertEquals(Agenda.DiaSemana.SEGUNDA, agendaCriada.getDiaSemana());

        when(agendaRepository.findById(10L)).thenReturn(Optional.of(agendaCriada));
        Agenda agendaAlternada = agendaService.alternarStatusDia(10L);
        assertFalse(agendaAlternada.getEstaAtivo());
    }
}
