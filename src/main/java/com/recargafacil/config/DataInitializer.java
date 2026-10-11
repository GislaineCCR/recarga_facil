package com.recargafacil.config;

import com.recargafacil.model.*;
import com.recargafacil.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * ==============================================================================
 * AUTO-POVOAMENTO DO BANCO DE DADOS (SEED / DATA INITIALIZER)
 * ==============================================================================
 * Garante que qualquer pessoa que clone o projeto execute a aplicação e encontre
 * o banco de dados imediatamente populado com dados válidos de Sorocaba/SP,
 * anfitriões, pontos de recarga e agendas para testes.
 * ==============================================================================
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final MotoristaRepository motoristaRepository;
    private final VeiculoRepository veiculoRepository;
    private final HostRepository hostRepository;
    private final LocalRepository localRepository;
    private final WallboxRepository wallboxRepository;
    private final AgendaRepository agendaRepository;
    private final ReservaRepository reservaRepository;
    private final PagamentoRepository pagamentoRepository;

    public DataInitializer(UsuarioRepository usuarioRepository,
                           MotoristaRepository motoristaRepository,
                           VeiculoRepository veiculoRepository,
                           HostRepository hostRepository,
                           LocalRepository localRepository,
                           WallboxRepository wallboxRepository,
                           AgendaRepository agendaRepository,
                           ReservaRepository reservaRepository,
                           PagamentoRepository pagamentoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.motoristaRepository = motoristaRepository;
        this.veiculoRepository = veiculoRepository;
        this.hostRepository = hostRepository;
        this.localRepository = localRepository;
        this.wallboxRepository = wallboxRepository;
        this.agendaRepository = agendaRepository;
        this.reservaRepository = reservaRepository;
        this.pagamentoRepository = pagamentoRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Se já existirem wallboxes no banco, o povoamento já foi feito anteriormente
        if (wallboxRepository.count() > 0) {
            System.out.println(">>> [Recarga Fácil] Banco de dados já contém registros. Auto-povoamento dispensado.");
            return;
        }

        System.out.println(">>> [Recarga Fácil] Banco de dados vazio detectado. Inicializando povoamento automático com dados de teste...");

        // 1. Usuários de Acesso
        Usuario uCarlos = new Usuario();
        uCarlos.setNome("Carlos Eduardo Mendes");
        uCarlos.setEmail("carlos@recargafacil.com");
        uCarlos.setSenha("senha123");
        usuarioRepository.save(uCarlos);

        Usuario uNexus = new Usuario();
        uNexus.setNome("Nexus Coworking Hub");
        uNexus.setEmail("contato@nexushub.com");
        uNexus.setSenha("senha123");
        usuarioRepository.save(uNexus);

        Usuario uMariana = new Usuario();
        uMariana.setNome("Dra. Mariana Santos");
        uMariana.setEmail("mariana@recargafacil.com");
        uMariana.setSenha("senha123");
        usuarioRepository.save(uMariana);

        Usuario uMotorista = new Usuario();
        uMotorista.setNome("Motorista Demo EV");
        uMotorista.setEmail("motorista@email.com");
        uMotorista.setSenha("senha123");
        usuarioRepository.save(uMotorista);

        // 2. Motorista e Veículo Demo
        Motorista motorista = new Motorista();
        motorista.setUsuario(uMotorista);
        motorista.setNome("Motorista Demo EV");
        motorista.setCpf("123.456.789-00");
        motorista.setEmail("motorista@email.com");
        motorista.setTelefone("(15) 99876-5432");
        motoristaRepository.save(motorista);

        Veiculo veiculo = new Veiculo();
        veiculo.setMotorista(motorista);
        veiculo.setMarca("BYD");
        veiculo.setModelo("Dolphin EV");
        veiculo.setPlaca("BRA2E19");
        veiculo.setRenavan("12345678901");
        veiculo.setTipoConector(Veiculo.TipoConector.TIPO_2);
        veiculoRepository.save(veiculo);

        Veiculo veiculo2 = new Veiculo();
        veiculo2.setMotorista(motorista);
        veiculo2.setMarca("Volvo");
        veiculo2.setModelo("EX30 Ultra");
        veiculo2.setPlaca("VOL2E26");
        veiculo2.setRenavan("98765432109");
        veiculo2.setTipoConector(Veiculo.TipoConector.CCS2);
        veiculoRepository.save(veiculo2);

        // Usuário Persona Gislaine (Motorista + Host com Multi-veículos)
        Usuario uGislaine = new Usuario();
        uGislaine.setNome("Gislaine Cristina Cesario Rodrigues");
        uGislaine.setEmail("gislaine.c.cesario@gmail.com");
        uGislaine.setSenha("1234");
        usuarioRepository.save(uGislaine);

        Motorista motoristaGislaine = new Motorista();
        motoristaGislaine.setUsuario(uGislaine);
        motoristaGislaine.setNome("Gislaine Cristina Cesario Rodrigues");
        motoristaGislaine.setCpf("36616646633");
        motoristaGislaine.setEmail("gislaine.c.cesario@gmail.com");
        motoristaGislaine.setTelefone("(11) 99269-6670");
        motoristaRepository.save(motoristaGislaine);

        Veiculo vGislaine1 = new Veiculo();
        vGislaine1.setMotorista(motoristaGislaine);
        vGislaine1.setMarca("BYD");
        vGislaine1.setModelo("Song Plus DM-i");
        vGislaine1.setPlaca("GIS2026");
        vGislaine1.setRenavan("55443322110");
        vGislaine1.setTipoConector(Veiculo.TipoConector.TIPO_2);
        veiculoRepository.save(vGislaine1);

        Veiculo vGislaine2 = new Veiculo();
        vGislaine2.setMotorista(motoristaGislaine);
        vGislaine2.setMarca("Volvo");
        vGislaine2.setModelo("EX30 Plus");
        vGislaine2.setPlaca("GIS3E26");
        vGislaine2.setRenavan("99887766554");
        vGislaine2.setTipoConector(Veiculo.TipoConector.CCS2);
        veiculoRepository.save(vGislaine2);

        // 3. Anfitriões (Hosts)
        Host hostCarlos = new Host();
        hostCarlos.setUsuario(uCarlos);
        hostCarlos.setNome("Carlos Eduardo M.");
        hostCarlos.setEmail("carlos@recargafacil.com");
        hostCarlos.setCpf("321.654.987-11");
        hostCarlos.setTelefone("(15) 99123-8899");
        hostCarlos.setDadosBancarios("PIX: carlos@recargafacil.com (Nubank)");
        hostCarlos.setFotoUrl("https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=150&q=80");
        hostRepository.save(hostCarlos);

        Host hostNexus = new Host();
        hostNexus.setUsuario(uNexus);
        hostNexus.setNome("Nexus Hub Inovação");
        hostNexus.setEmail("contato@nexushub.com");
        hostNexus.setCpf("12.345.678/0001-90");
        hostNexus.setTelefone("(15) 3211-9000");
        hostNexus.setDadosBancarios("PIX: 12.345.678/0001-90 (Itaú)");
        hostNexus.setFotoUrl("https://images.unsplash.com/photo-1486406146926-c627a92ad1ab?auto=format&fit=crop&w=150&q=80");
        hostRepository.save(hostNexus);

        Host hostMariana = new Host();
        hostMariana.setUsuario(uMariana);
        hostMariana.setNome("Dra. Mariana Santos");
        hostMariana.setEmail("mariana@recargafacil.com");
        hostMariana.setCpf("456.789.123-55");
        hostMariana.setTelefone("(15) 98144-5566");
        hostMariana.setDadosBancarios("PIX: mariana@recargafacil.com (Santander)");
        hostMariana.setFotoUrl("https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?auto=format&fit=crop&w=150&q=80");
        hostRepository.save(hostMariana);

        Host hostGislaine = new Host();
        hostGislaine.setUsuario(uGislaine);
        hostGislaine.setNome("Gislaine Cristina Cesario Rodrigues");
        hostGislaine.setEmail("gislaine.c.cesario@gmail.com");
        hostGislaine.setCpf("36616646633");
        hostGislaine.setTelefone("(11) 99269-6670");
        hostGislaine.setDadosBancarios("PIX: gislaine.c.cesario@gmail.com (Nubank)");
        hostGislaine.setFotoUrl("https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=150&q=80");
        hostRepository.save(hostGislaine);

        // 4. Locais em Sorocaba/SP (Geolocalização Leaflet)
        Local lCampolim = new Local();
        lCampolim.setLatitude(-23.5238);
        lCampolim.setLongitude(-47.4645);
        lCampolim.setEnderecoCompleto("Rua Antonio Perez Hernandez, 480 - Condomínio Terra Nova, Casa 34, Parque Campolim, Sorocaba - SP");
        lCampolim.setTipoAmbiente(Local.TipoAmbiente.RESIDENCIAL);
        lCampolim.setInstrucoesAcesso("Identifique-se na portaria com o código de reserva. Vaga na garagem da direita. Anfitrião Carlos já notificado.");
        lCampolim.setAcessoEnderecoAposPagamento(true);
        lCampolim.setPossuiVagaEstacionamento(true);
        lCampolim.setTipoVagaEstacionamento("GARAGEM_PRIVATIVA_COBERTA");
        lCampolim.setAutorizacaoCondominio(true);
        lCampolim.setDetalhesVaga("Vaga na garagem coberta privativa da casa 34.");
        localRepository.save(lCampolim);

        Local lNexus = new Local();
        lNexus.setLatitude(-23.5015);
        lNexus.setLongitude(-47.4580);
        lNexus.setEnderecoCompleto("Avenida Engenheiro Carlos Reinaldo Mendes, 2015 - Subsolo 1, Alto da Boa Vista, Sorocaba - SP");
        lNexus.setTipoAmbiente(Local.TipoAmbiente.COMERCIAL);
        lNexus.setInstrucoesAcesso("Retire o ticket na cancela. Vagas EV 01 e 02 no Piso -1. Ativação via QR Code.");
        lNexus.setAcessoEnderecoAposPagamento(true);
        lNexus.setPossuiVagaEstacionamento(true);
        lNexus.setTipoVagaEstacionamento("ESTACIONAMENTO_COMERCIAL_ROTATIVO");
        lNexus.setAutorizacaoCondominio(true);
        lNexus.setDetalhesVaga("Piso -1 vagas demarcadas em verde para veículos elétricos.");
        localRepository.save(lNexus);

        Local lJdAm = new Local();
        lJdAm.setLatitude(-23.5180);
        lJdAm.setLongitude(-47.4720);
        lJdAm.setEnderecoCompleto("Rua Santa Clara, 1120 - Jardim América, Sorocaba - SP");
        lJdAm.setTipoAmbiente(Local.TipoAmbiente.RESIDENCIAL);
        lJdAm.setInstrucoesAcesso("Tocar interfone 01. Estacionar na vaga coberta frontal ao lado da Wallbox WEG.");
        lJdAm.setAcessoEnderecoAposPagamento(true);
        lJdAm.setPossuiVagaEstacionamento(true);
        lJdAm.setTipoVagaEstacionamento("GARAGEM_PRIVATIVA_COBERTA");
        lJdAm.setAutorizacaoCondominio(true);
        lJdAm.setDetalhesVaga("Garagem frontal da residência.");
        localRepository.save(lJdAm);

        // 5. Wallboxes (Estações de Recarga)
        Wallbox wb1 = new Wallbox();
        wb1.setHost(hostCarlos);
        wb1.setLocal(lCampolim);
        wb1.setModelo("Wallbox EcoCharge Campolim 7.4kW");
        wb1.setPotencia(Wallbox.Potencia.P_7KW);
        wb1.setPrecoHora(new BigDecimal("15.00"));
        wb1.setComodidades("wifi,cafe,coberto,banheiro,cameras");
        wb1.setFotoUrl("https://images.unsplash.com/photo-1593941707882-a5bba14938c7?auto=format&fit=crop&w=800&q=80");
        wb1.setStatus(Wallbox.Status.DISPONIVEL);
        wallboxRepository.save(wb1);

        Wallbox wb2 = new Wallbox();
        wb2.setHost(hostNexus);
        wb2.setLocal(lNexus);
        wb2.setModelo("Hub FastCharge Nexus 22kW");
        wb2.setPotencia(Wallbox.Potencia.P_22KW);
        wb2.setPrecoHora(new BigDecimal("22.00"));
        wb2.setComodidades("wifi,cafe,banheiro,trabalho,cameras");
        wb2.setFotoUrl("https://images.unsplash.com/photo-1563986768609-322da13575f3?auto=format&fit=crop&w=800&q=80");
        wb2.setStatus(Wallbox.Status.DISPONIVEL);
        wallboxRepository.save(wb2);

        Wallbox wb3 = new Wallbox();
        wb3.setHost(hostMariana);
        wb3.setLocal(lJdAm);
        wb3.setModelo("Residência Solar Verde 11kW");
        wb3.setPotencia(Wallbox.Potencia.P_11KW);
        wb3.setPrecoHora(new BigDecimal("14.00"));
        wb3.setComodidades("wifi,coberto,cameras,cafe");
        wb3.setFotoUrl("https://images.unsplash.com/photo-1617788138017-80ad40651399?auto=format&fit=crop&w=800&q=80");
        wb3.setStatus(Wallbox.Status.DISPONIVEL);
        wallboxRepository.save(wb3);

        // 6. Horários na Agenda (Semana completa para cada Wallbox)
        criarGradeSemanal(wb1, LocalTime.of(8, 0), LocalTime.of(22, 0));
        criarGradeSemanal(wb2, LocalTime.of(7, 0), LocalTime.of(20, 0));
        criarGradeSemanal(wb3, LocalTime.of(8, 0), LocalTime.of(19, 0));

        // 7. Reserva e Pagamento Histórico
        Reserva res = new Reserva();
        res.setMotorista(motorista);
        res.setVeiculo(veiculo);
        res.setWallbox(wb1);
        res.setDataReserva(LocalDate.now());
        res.setHorario(LocalTime.of(14, 0));
        res.setStatusReserva(Reserva.StatusReserva.CONCLUIDA);
        reservaRepository.save(res);

        Pagamento pag = new Pagamento();
        pag.setReserva(res);
        pag.setValorTotal(new BigDecimal("37.50"));
        pag.setTaxaSistema(new BigDecimal("7.50"));
        pag.setStatusPagamento(Pagamento.StatusPagamento.APROVADO);
        pagamentoRepository.save(pag);

        System.out.println(">>> [Recarga Fácil] Auto-povoamento concluído com sucesso! 3 estações, anfitriões e motoristas prontos para uso.");
    }

    private void criarGradeSemanal(Wallbox wb, LocalTime inicio, LocalTime fim) {
        for (Agenda.DiaSemana dia : Agenda.DiaSemana.values()) {
            Agenda ag = new Agenda();
            ag.setWallbox(wb);
            ag.setDiaSemana(dia);
            ag.setHorarioInicio(inicio);
            ag.setHorarioFim(fim);
            ag.setEstaAtivo(dia != Agenda.DiaSemana.DOMINGO || !wb.getModelo().contains("Nexus"));
            agendaRepository.save(ag);
        }
    }
}
