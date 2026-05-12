package com.example.recargafacil.dto;

public class HostDTO {
    private Long idUsuario;
    private String nome;
    private String email;
    private String dadosBancarios;

    // Getters and Setters
    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getDadosBancarios() { return dadosBancarios; }
    public void setDadosBancarios(String dadosBancarios) { this.dadosBancarios = dadosBancarios; }
}