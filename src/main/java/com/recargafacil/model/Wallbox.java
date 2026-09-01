package com.recargafacil.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * ============================================================================
 * ENTIDADE JPA: Wallbox (Ponto de Recarga Privado)
 * ============================================================================
 * FUNÇÃO NO SISTEMA:
 * Representa o equipamento físico de recarga cadastrado por um Host (Anfitrião)
 * e instalado em um Local específico (Residencial ou Comercial).
 * 
 * CONEXÃO COM O FRONT-END:
 * - Tela 1 (Busca no Mapa): Fornece a localização (latitude/longitude), potência,
 *   tipo de ambiente e preço/hora para desenhar os marcadores interativos no mapa Leaflet.
 * - Tela 2 (Detalhes da Wallbox): Alimenta os cards de especificações técnicas
 *   (potência em kW, modelo do carregador, status de disponibilidade e anfitrião).
 * - Tela 3 (Agendamento): O id desta Wallbox é enviado no corpo da requisição
 *   (JSON) para associar a nova reserva do motorista ao ponto correto.
 * ============================================================================
 */
@Entity
public class Wallbox {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_host")
    private Host host;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "id_local")
    private Local local;

    private String modelo;

    @Enumerated(EnumType.STRING)
    private Potencia potencia;

    @Column(precision = 10, scale = 2)
    private BigDecimal precoHora = new BigDecimal("15.00");

    private String comodidades = "wifi,cafe,coberto,banheiro,cameras";

    private String fotoUrl;

    @Enumerated(EnumType.STRING)
    private Status status = Status.DISPONIVEL;

    public enum Potencia {
        P_7KW("7kW"), P_11KW("11kW"), P_22KW("22kW");
        private String value;
        Potencia(String value) { this.value = value; }
        public String getValue() { return value; }
    }

    public enum Status {
        DISPONIVEL, OCUPADO, MANUTENCAO, INDISPONIVEL
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Host getHost() { return host; }
    public void setHost(Host host) { this.host = host; }

    public Local getLocal() { return local; }
    public void setLocal(Local local) { this.local = local; }

    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }

    public Potencia getPotencia() { return potencia; }
    public void setPotencia(Potencia potencia) { this.potencia = potencia; }

    public BigDecimal getPrecoHora() { return precoHora; }
    public void setPrecoHora(BigDecimal precoHora) { this.precoHora = precoHora; }

    public String getComodidades() { return comodidades; }
    public void setComodidades(String comodidades) { this.comodidades = comodidades; }

    public String getFotoUrl() { return fotoUrl; }
    public void setFotoUrl(String fotoUrl) { this.fotoUrl = fotoUrl; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
}