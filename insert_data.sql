-- ==========================================================
-- SCRIPT DE POPULAÇÃO DE DADOS (MOCK / SEED)
-- PROJETO: RECARGA FÁCIL (UPX 4)
-- CONEXÃO: Alimenta as 5 telas da interface Front-end Web
-- ==========================================================

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

-- 1. USUÁRIOS BASE (LOGIN)
INSERT INTO usuario (nome, email, senha) VALUES 
('Carlos Eduardo Mendes', 'carlos@recargafacil.com', 'senha123'),
('Nexus Coworking Hub', 'contato@nexushub.com', 'senha123'),
('Dra. Mariana Santos', 'mariana@recargafacil.com', 'senha123'),
('Motorista Demo EV', 'motorista@email.com', 'senha123');

SET @user_carlos = 1;
SET @user_nexus = 2;
SET @user_mariana = 3;
SET @user_motorista = 4;

-- 2. MOTORISTA REGISTRADO (Para agendamentos na Tela 3)
INSERT INTO Motorista (id_usuario, nome, cpf, email, telefone) VALUES
(@user_motorista, 'Motorista Demo EV', '123.456.789-00', 'motorista@email.com', '(15) 99876-5432');
SET @id_motorista_demo = 1;

-- 3. VEÍCULO ELÉTRICO
INSERT INTO Veiculo (id_motorista, modelo, marca, placa, renavan, tipo_conector) VALUES
(@id_motorista_demo, 'Dolphin EV', 'BYD', 'BRA2E19', '12345678901', 'TIPO_2');
SET @id_veiculo_demo = 1;

-- 4. ANFITRIÕES (HOSTS)
INSERT INTO Host (id_usuario, nome, email, dados_bancarios) VALUES
(@user_carlos, 'Carlos Eduardo M.', 'carlos@recargafacil.com', 'PIX: carlos@recargafacil.com (Nubank)'),
(@user_nexus, 'Nexus Hub Inovação', 'contato@nexushub.com', 'PIX: 12.345.678/0001-90 (Itaú)'),
(@user_mariana, 'Dra. Mariana Santos', 'mariana@recargafacil.com', 'PIX: mariana@recargafacil.com (Santander)');

SET @host_carlos = 1;
SET @host_nexus = 2;
SET @host_mariana = 3;

-- 5. LOCAIS (COORDENADAS SOROCABA/SP - TELA 1 E ENDEREÇO PROTEGIDO)
INSERT INTO Local (latitude, longitude, endereco_completo, tipo_ambiente, instrucoes_acesso, acesso_endereco_apos_pagamento) VALUES
-- Ponto 1: Residencial Campolim (Sorocaba)
(-23.5238, -47.4645, 'Rua Antonio Perez Hernandez, 480 - Condomínio Terra Nova, Casa 34, Parque Campolim, Sorocaba - SP', 'RESIDENCIAL', 'Identifique-se na portaria com o código de reserva. Vaga na garagem da direita. Anfitrião Carlos já notificado.', TRUE),

-- Ponto 2: Comercial Alto da Boa Vista (Sorocaba)
(-23.5015, -47.4580, 'Avenida Engenheiro Carlos Reinaldo Mendes, 2015 - Subsolo 1, Alto da Boa Vista, Sorocaba - SP', 'COMERCIAL', 'Retire o ticket na cancela. Vagas EV 01 e 02 no Piso -1. Ativação via QR Code.', TRUE),

-- Ponto 3: Residencial Jardim América (Sorocaba - Energia Solar)
(-23.5180, -47.4720, 'Rua Santa Clara, 1120 - Jardim América, Sorocaba - SP', 'RESIDENCIAL', 'Tocar interfone 01. Estacionar na vaga coberta frontal ao lado da Wallbox WEG.', TRUE);

SET @local_campolim = 1;
SET @local_nexus = 2;
SET @local_jdamérica = 3;

-- 6. WALLBOXES (PONTOS DE RECARGA NO MAPA)
INSERT INTO Wallbox (id_host, id_local, modelo, potencia, preco_hora, comodidades, foto_url, status) VALUES
(@host_carlos, @local_campolim, 'Wallbox EcoCharge Campolim 7.4kW', 'P_7KW', 15.00, 'wifi,cafe,coberto,banheiro,cameras', 'https://images.unsplash.com/photo-1593941707882-a5bba14938c7?auto=format&fit=crop&w=800&q=80', 'DISPONIVEL'),
(@host_nexus, @local_nexus, 'Hub FastCharge Nexus 22kW', 'P_22KW', 22.00, 'wifi,cafe,banheiro,trabalho,cameras', 'https://images.unsplash.com/photo-1563986768609-322da13575f3?auto=format&fit=crop&w=800&q=80', 'DISPONIVEL'),
(@host_mariana, @local_jdamérica, 'Residência Solar Verde 11kW', 'P_11KW', 14.00, 'wifi,coberto,cameras,cafe', 'https://images.unsplash.com/photo-1617788138017-80ad40651399?auto=format&fit=crop&w=800&q=80', 'DISPONIVEL');

SET @wb_campolim = 1;
SET @wb_nexus = 2;
SET @wb_jdamérica = 3;

-- 7. HORÁRIOS DISPONÍVEIS NA AGENDA
INSERT INTO Agenda (id_wallbox, dia_semana, horario_inicio, horario_fim, esta_ativo) VALUES
(@wb_campolim, 'SEGUNDA', '08:00:00', '22:00:00', TRUE),
(@wb_nexus, 'SEGUNDA', '07:00:00', '20:00:00', TRUE),
(@wb_jdamérica, 'SEGUNDA', '09:00:00', '18:00:00', TRUE);

-- 8. EXEMPLO DE RESERVA E PAGAMENTO (HISTÓRICO)
INSERT INTO Reserva (id_motorista, id_veiculo, id_wallbox, data_reserva, horario, status_reserva) VALUES
(@id_motorista_demo, @id_veiculo_demo, @wb_campolim, '2026-08-26', '14:00:00', 'CONCLUIDA');
SET @reserva_demo = 1;

-- Pagamento com a taxa de 25% calculada: Subtotal R$ 30,00 + Taxa R$ 7,50 = R$ 37,50
INSERT INTO Pagamento (id_reserva, valor_total, taxa_sistema, metodo_pagamento, status_pagamento) VALUES
(@reserva_demo, 37.50, 7.50, 'PIX', 'APROVADO');

SELECT 'BANCO DE DADOS RECARGA FÁCIL POPULADO COM SUCESSO!' AS Status;
