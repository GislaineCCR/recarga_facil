package com.example.recargafacil.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
public class Pagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "id_reserva", nullable = false, unique = true)
    private Reserva reserva;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal valorTotal;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal taxaSistema;

    @Enumerated(EnumType.STRING)
    private StatusPagamento statusPagamento = StatusPagamento.PENDENTE;

    public enum StatusPagamento {
        PENDENTE, APROVADO, REJEITADO
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Reserva getReserva() { return reserva; }
    public void setReserva(Reserva reserva) { this.reserva = reserva; }

    public BigDecimal getValorTotal() { return valorTotal; }
    public void setValorTotal(BigDecimal valorTotal) { this.valorTotal = valorTotal; }

    public BigDecimal getTaxaSistema() { return taxaSistema; }
    public void setTaxaSistema(BigDecimal taxaSistema) { this.taxaSistema = taxaSistema; }

    public StatusPagamento getStatusPagamento() { return statusPagamento; }
    public void setStatusPagamento(StatusPagamento statusPagamento) { this.statusPagamento = statusPagamento; }
}
