-- ==========================================================
-- SCRIPT DE CRIAÇÃO DO BANCO DE DADOS: RECARGA FÁCIL
-- ==========================================================

CREATE DATABASE IF NOT EXISTS Recarga_Facil;
USE Recarga_Facil;

-- 1. Criação das Tabelas Independentes (Sem Chaves Estrangeiras)

CREATE TABLE Motorista (
    id_motorista INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    cpf VARCHAR(14) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    telefone VARCHAR(20) NOT NULL
);

CREATE TABLE Host (
    id_host INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    dados_bancarios VARCHAR(255) NOT NULL
);

CREATE TABLE Local (
    id_local INT AUTO_INCREMENT PRIMARY KEY,
    latitude DOUBLE NOT NULL,
    longitude DOUBLE NOT NULL,
    endereco_completo VARCHAR(255) NOT NULL,
    tipo_ambiente ENUM('RESIDENCIAL', 'COMERCIAL') NOT NULL,
    instrucoes_acesso TEXT,
    acesso_endereco_apos_pagamento BOOLEAN DEFAULT TRUE
);

-- 2. Criação das Tabelas Dependentes (Com Chaves Estrangeiras)

CREATE TABLE Veiculo (
    id_veiculo INT AUTO_INCREMENT PRIMARY KEY,
    id_motorista INT NOT NULL,
    modelo VARCHAR(50) NOT NULL,
    marca VARCHAR(50) NOT NULL,
    placa VARCHAR(10) NOT NULL UNIQUE,
    renavan VARCHAR(20) NOT NULL UNIQUE,
    tipo_conector ENUM('TIPO_1', 'TIPO_2', 'CCS2', 'GB_T') NOT NULL,
    CONSTRAINT fk_veiculo_motorista FOREIGN KEY (id_motorista) REFERENCES Motorista(id_motorista) ON DELETE CASCADE
);

CREATE TABLE Wallbox (
    id_wallbox INT AUTO_INCREMENT PRIMARY KEY,
    id_host INT NOT NULL,
    id_local INT NOT NULL,
    modelo VARCHAR(100) NOT NULL,
    potencia ENUM('7kW', '11kW', '22kW') NOT NULL,
    status ENUM('DISPONIVEL', 'OCUPADO', 'MANUTENCAO', 'INDISPONIVEL') DEFAULT 'DISPONIVEL',
    CONSTRAINT fk_wallbox_host FOREIGN KEY (id_host) REFERENCES Host(id_host) ON DELETE CASCADE,
    CONSTRAINT fk_wallbox_local FOREIGN KEY (id_local) REFERENCES Local(id_local) ON DELETE RESTRICT
);

CREATE TABLE Agenda (
    id_agenda INT AUTO_INCREMENT PRIMARY KEY,
    id_wallbox INT NOT NULL,
    dia_semana ENUM('DOMINGO', 'SEGUNDA', 'TERCA', 'QUARTA', 'QUINTA', 'SEXTA', 'SABADO') NOT NULL,
    horario_inicio TIME NOT NULL,
    horario_fim TIME NOT NULL,
    esta_ativo BOOLEAN DEFAULT TRUE,
    CONSTRAINT fk_agenda_wallbox FOREIGN KEY (id_wallbox) REFERENCES Wallbox(id_wallbox) ON DELETE CASCADE
);

CREATE TABLE Reserva (
    id_reserva INT AUTO_INCREMENT PRIMARY KEY,
    id_motorista INT NOT NULL,
    id_veiculo INT NOT NULL,
    id_wallbox INT NOT NULL,
    data_reserva DATE NOT NULL,
    horario TIME NOT NULL,
    status_reserva ENUM('PENDENTE', 'CONCLUIDA', 'NO_SHOW', 'CANCELADA') DEFAULT 'PENDENTE',
    CONSTRAINT fk_reserva_motorista FOREIGN KEY (id_motorista) REFERENCES Motorista(id_motorista),
    CONSTRAINT fk_reserva_veiculo FOREIGN KEY (id_veiculo) REFERENCES Veiculo(id_veiculo),
    CONSTRAINT fk_reserva_wallbox FOREIGN KEY (id_wallbox) REFERENCES Wallbox(id_wallbox)
);

CREATE TABLE Pagamento (
    id_pagamento INT AUTO_INCREMENT PRIMARY KEY,
    id_reserva INT NOT NULL UNIQUE, -- Uma reserva tem apenas UM pagamento (Relação 1:1)
    valor_total DECIMAL(10,2) NOT NULL,
    taxa_sistema DECIMAL(10,2) NOT NULL,
    status_pagamento ENUM('PENDENTE', 'APROVADO', 'REJEITADO') DEFAULT 'PENDENTE',
    CONSTRAINT fk_pagamento_reserva FOREIGN KEY (id_reserva) REFERENCES Reserva(id_reserva) ON DELETE RESTRICT
);