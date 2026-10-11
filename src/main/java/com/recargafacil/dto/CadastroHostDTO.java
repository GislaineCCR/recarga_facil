package com.recargafacil.dto;

import com.recargafacil.model.Local.TipoAmbiente;
import com.recargafacil.model.Wallbox.Potencia;
import com.recargafacil.model.Wallbox.Status;
import java.math.BigDecimal;

public class CadastroHostDTO {
    // Dados Pessoais e de Pagamento do Host
    private String nome;
    private String email;
    private String senha;
    private String dadosBancarios;
    private String telefone;
    private String cpf;

    // Dados do Equipamento Wallbox
    private String modelo;
    private Potencia potencia;
    private Status status;
    private BigDecimal precoHora;
    private String comodidades;
    private String fotoUrl;

    // Dados do Local de Instalação
    private String enderecoCompleto;
    private Double latitude;
    private Double longitude;
    private TipoAmbiente tipoAmbiente;
    private String instrucoesAcesso;
    private Boolean acessoEnderecoAposPagamento;
    private Boolean possuiVagaEstacionamento;
    private String tipoVagaEstacionamento;
    private Boolean autorizacaoCondominio;
    private String detalhesVaga;

    // Getters and Setters
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    public String getDadosBancarios() { return dadosBancarios; }
    public void setDadosBancarios(String dadosBancarios) { this.dadosBancarios = dadosBancarios; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }

    public Potencia getPotencia() { return potencia; }
    public void setPotencia(Potencia potencia) { this.potencia = potencia; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public BigDecimal getPrecoHora() { return precoHora; }
    public void setPrecoHora(BigDecimal precoHora) { this.precoHora = precoHora; }

    public String getComodidades() { return comodidades; }
    public void setComodidades(String comodidades) { this.comodidades = comodidades; }

    public String getFotoUrl() { return fotoUrl; }
    public void setFotoUrl(String fotoUrl) { this.fotoUrl = fotoUrl; }

    public String getEnderecoCompleto() { return enderecoCompleto; }
    public void setEnderecoCompleto(String enderecoCompleto) { this.enderecoCompleto = enderecoCompleto; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public TipoAmbiente getTipoAmbiente() { return tipoAmbiente; }
    public void setTipoAmbiente(TipoAmbiente tipoAmbiente) { this.tipoAmbiente = tipoAmbiente; }

    public String getInstrucoesAcesso() { return instrucoesAcesso; }
    public void setInstrucoesAcesso(String instrucoesAcesso) { this.instrucoesAcesso = instrucoesAcesso; }

    public Boolean getAcessoEnderecoAposPagamento() { return acessoEnderecoAposPagamento; }
    public void setAcessoEnderecoAposPagamento(Boolean acessoEnderecoAposPagamento) { this.acessoEnderecoAposPagamento = acessoEnderecoAposPagamento; }

    public Boolean getPossuiVagaEstacionamento() { return possuiVagaEstacionamento; }
    public void setPossuiVagaEstacionamento(Boolean possuiVagaEstacionamento) { this.possuiVagaEstacionamento = possuiVagaEstacionamento; }

    public String getTipoVagaEstacionamento() { return tipoVagaEstacionamento; }
    public void setTipoVagaEstacionamento(String tipoVagaEstacionamento) { this.tipoVagaEstacionamento = tipoVagaEstacionamento; }

    public Boolean getAutorizacaoCondominio() { return autorizacaoCondominio; }
    public void setAutorizacaoCondominio(Boolean autorizacaoCondominio) { this.autorizacaoCondominio = autorizacaoCondominio; }

    public String getDetalhesVaga() { return detalhesVaga; }
    public void setDetalhesVaga(String detalhesVaga) { this.detalhesVaga = detalhesVaga; }
}
