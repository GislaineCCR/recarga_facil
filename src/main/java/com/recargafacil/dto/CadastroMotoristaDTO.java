package com.recargafacil.dto;

import com.recargafacil.model.Veiculo.TipoConector;

public class CadastroMotoristaDTO {
    // Dados Pessoais do Motorista
    private String nome;
    private String cpf;
    private String email;
    private String telefone;
    private String senha;

    // Dados do Veículo Elétrico
    private String marca;
    private String modelo;
    private String placa;
    private String renavan;
    private TipoConector tipoConector;

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }

    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }

    public String getPlaca() { return placa; }
    public void setPlaca(String placa) { this.placa = placa; }

    public String getRenavan() { return renavan; }
    public void setRenavan(String renavan) { this.renavan = renavan; }

    public TipoConector getTipoConector() { return tipoConector; }
    public void setTipoConector(TipoConector tipoConector) { this.tipoConector = tipoConector; }
}
