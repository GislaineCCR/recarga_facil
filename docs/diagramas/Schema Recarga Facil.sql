-- ==========================================================
-- SCRIPT DE CRIAÇÃO DO BANCO DE DADOS: RECARGA FÁCIL
-- ==========================================================

CREATE DATABASE IF NOT EXISTS recargafacil;
USE recargafacil;

-- 1. Criação das Tabelas Independentes

CREATE TABLE usuario (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    senha VARCHAR(255) NOT NULL
);

CREATE TABLE Motorista (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_usuario BIGINT UNIQUE,
    nome VARCHAR(100) NOT NULL,
    cpf VARCHAR(14) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    telefone VARCHAR(20) NOT NULL,
    CONSTRAINT fk_motorista_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id) ON DELETE CASCADE
);

CREATE TABLE Host (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_usuario BIGINT UNIQUE,
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    dados_bancarios VARCHAR(255) NOT NULL,
    CONSTRAINT fk_host_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id) ON DELETE CASCADE
);

CREATE TABLE Local (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    latitude DOUBLE NOT NULL,
    longitude DOUBLE NOT NULL,
    endereco_completo VARCHAR(255) NOT NULL,
    tipo_ambiente ENUM('RESIDENCIAL', 'COMERCIAL') NOT NULL,
    instrucoes_acesso TEXT,
    acesso_endereco_apos_pagamento BOOLEAN DEFAULT TRUE
);

-- 2. Criação das Tabelas Dependentes

CREATE TABLE Veiculo (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_motorista BIGINT NOT NULL,
    modelo VARCHAR(50) NOT NULL,
    marca VARCHAR(50) NOT NULL,
    placa VARCHAR(10) NOT NULL UNIQUE,
    renavan VARCHAR(20) NOT NULL UNIQUE,
    tipo_conector ENUM('TIPO_1', 'TIPO_2', 'CCS2', 'GB_T') NOT NULL,
    CONSTRAINT fk_veiculo_motorista FOREIGN KEY (id_motorista) REFERENCES Motorista(id) ON DELETE CASCADE
);

CREATE TABLE Wallbox (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_host BIGINT NOT NULL,
    id_local BIGINT NOT NULL,
    modelo VARCHAR(100) NOT NULL,
    potencia ENUM('P_7KW', 'P_11KW', 'P_22KW') NOT NULL,
    status ENUM('DISPONIVEL', 'OCUPADO', 'MANUTENCAO', 'INDISPONIVEL') DEFAULT 'DISPONIVEL',
    CONSTRAINT fk_wallbox_host FOREIGN KEY (id_host) REFERENCES Host(id) ON DELETE CASCADE,
    CONSTRAINT fk_wallbox_local FOREIGN KEY (id_local) REFERENCES Local(id) ON DELETE RESTRICT
);

CREATE TABLE Agenda (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_wallbox BIGINT NOT NULL,
    dia_semana ENUM('DOMINGO', 'SEGUNDA', 'TERCA', 'QUARTA', 'QUINTA', 'SEXTA', 'SABADO') NOT NULL,
    horario_inicio TIME NOT NULL,
    horario_fim TIME NOT NULL,
    esta_ativo BOOLEAN DEFAULT TRUE,
    CONSTRAINT fk_agenda_wallbox FOREIGN KEY (id_wallbox) REFERENCES Wallbox(id) ON DELETE CASCADE
);

CREATE TABLE Reserva (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_motorista BIGINT NOT NULL,
    id_veiculo BIGINT NOT NULL,
    id_wallbox BIGINT NOT NULL,
    data_reserva DATE NOT NULL,
    horario TIME NOT NULL,
    status_reserva ENUM('PENDENTE', 'CONCLUIDA', 'NO_SHOW', 'CANCELADA') DEFAULT 'PENDENTE',
    CONSTRAINT fk_reserva_motorista FOREIGN KEY (id_motorista) REFERENCES Motorista(id),
    CONSTRAINT fk_reserva_veiculo FOREIGN KEY (id_veiculo) REFERENCES Veiculo(id),
    CONSTRAINT fk_reserva_wallbox FOREIGN KEY (id_wallbox) REFERENCES Wallbox(id)
);

CREATE TABLE Pagamento (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_reserva BIGINT NOT NULL UNIQUE,
    valor_total DECIMAL(10,2) NOT NULL,
    taxa_sistema DECIMAL(10,2) NOT NULL,
    status_pagamento ENUM('PENDENTE', 'APROVADO', 'REJEITADO') DEFAULT 'PENDENTE',
    CONSTRAINT fk_pagamento_reserva FOREIGN KEY (id_reserva) REFERENCES Reserva(id) ON DELETE RESTRICT
);

-- Inserção de usuário de teste
INSERT INTO usuario (nome, email, senha) VALUES ('Usuario Teste', 'teste@email.com', '123456');
