SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE Pagamento;
TRUNCATE TABLE Reserva;
TRUNCATE TABLE Agenda;
TRUNCATE TABLE Wallbox;
TRUNCATE TABLE Veiculo;
TRUNCATE TABLE Local;
TRUNCATE TABLE Host;
TRUNCATE TABLE Motorista;
TRUNCATE TABLE usuario;
SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO usuario (nome, email, senha) VALUES ('João Silva', 'joao@email.com', '123');
SET @user_joao = LAST_INSERT_ID();

INSERT INTO usuario (nome, email, senha) VALUES ('Maria Oliveira', 'maria@email.com', '456');
SET @user_maria = LAST_INSERT_ID();

INSERT INTO Motorista (id_usuario, nome, cpf, email, telefone) VALUES
(@user_joao, 'João Silva', '111.222.333-44', 'joao@email.com', '(15) 99999-1111');
SET @id_motorista = LAST_INSERT_ID();

INSERT INTO Host (id_usuario, nome, email, dados_bancarios) VALUES
(@user_maria, 'Maria Oliveira', 'maria@email.com', 'PIX: maria@email.com');
SET @id_host = LAST_INSERT_ID();

INSERT INTO Local (latitude, longitude, endereco_completo, tipo_ambiente, instrucoes_acesso) VALUES
(-23.4735, -47.4475, 'Rua das Flores, 123, Sorocaba', 'RESIDENCIAL', 'Garagem lateral esquerda.');
SET @id_local = LAST_INSERT_ID();

INSERT INTO Veiculo (id_motorista, modelo, marca, placa, renavan, tipo_conector) VALUES
(@id_motorista, 'Model S', 'Tesla', 'EVS-1234', '98765432109', 'TIPO_2');
SET @id_veiculo = LAST_INSERT_ID();

-- Tentando inserir com '7KW' (Removido o prefixo P_ para compatibilidade)
INSERT INTO Wallbox (id_host, id_local, modelo, potencia, status) VALUES 
(@id_host, @id_local, 'Wallbox Pulsar Plus', '7KW', 'DISPONIVEL');SET @id_wallbox = LAST_INSERT_ID();

INSERT INTO Agenda (id_wallbox, dia_semana, horario_inicio, horario_fim, esta_ativo) VALUES
(@id_wallbox, 'SEGUNDA', '08:00:00', '18:00:00', TRUE);

INSERT INTO Reserva (id_motorista, id_veiculo, id_wallbox, data_reserva, horario, status_reserva) VALUES
(@id_motorista, @id_veiculo, @id_wallbox, '2026-05-18', '10:00:00', 'PENDENTE');
SET @id_reserva = LAST_INSERT_ID();

INSERT INTO Pagamento (id_reserva, valor_total, taxa_sistema, status_pagamento) VALUES
(@id_reserva, 50.00, 5.00, 'PENDENTE');

SELECT 'DADOS CADASTRADOS COM SUCESSO!' AS Resultado;
