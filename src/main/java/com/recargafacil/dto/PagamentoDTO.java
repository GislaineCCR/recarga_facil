package com.recargafacil.dto;

import java.math.BigDecimal;

public class PagamentoDTO {
    private Long idReserva;
    private BigDecimal valorTotal;
    private BigDecimal taxaSistema;

    // Getters and Setters
    public Long getIdReserva() { return idReserva; }
    public void setIdReserva(Long idReserva) { this.idReserva = idReserva; }

    public BigDecimal getValorTotal() { return valorTotal; }
    public void setValorTotal(BigDecimal valorTotal) { this.valorTotal = valorTotal; }

    public BigDecimal getTaxaSistema() { return taxaSistema; }
    public void setTaxaSistema(BigDecimal taxaSistema) { this.taxaSistema = taxaSistema; }
}
