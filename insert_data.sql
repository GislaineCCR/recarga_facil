-- ==========================================================
-- SCRIPT DE POPULAÇÃO DE DADOS (MOCK / SEED)
-- PROJETO: RECARGA FÁCIL (UPX 4)
-- CONEXÃO: Alimenta as 5 telas da interface Front-end Web
-- ==========================================================

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE pagamento;
TRUNCATE TABLE reserva;
TRUNCATE TABLE agenda;
TRUNCATE TABLE wallbox;
TRUNCATE TABLE veiculo;
TRUNCATE TABLE local;
TRUNCATE TABLE host;
TRUNCATE TABLE motorista;
TRUNCATE TABLE usuario;
SET FOREIGN_KEY_CHECKS = 1;

-- 1. USUÁRIOS BASE (LOGIN)
INSERT INTO usuario (nome, email, senha) VALUES 
('Carlos Eduardo Mendes', 'carlos@recargafacil.com', 'senha123'),
('Nexus Coworking Hub', 'contato@nexushub.com', 'senha123'),
('Dra. Mariana Santos', 'mariana@recargafacil.com', 'senha123'),
('Motorista Demo EV', 'motorista@email.com', 'senha123'),
('Gislaine Cristina Cesario Rodrigues', 'gislaine.c.cesario@gmail.com', '1234');

SET @user_carlos = 1;
SET @user_nexus = 2;
SET @user_mariana = 3;
SET @user_motorista = 4;
SET @user_gislaine = 5;

-- 2. MOTORISTAS REGISTRADOS (Para agendamentos na Tela 3)
INSERT INTO motorista (id_usuario, nome, cpf, email, telefone) VALUES
(@user_motorista, 'Motorista Demo EV', '123.456.789-00', 'motorista@email.com', '(15) 99876-5432'),
(@user_gislaine, 'Gislaine Cristina Cesario Rodrigues', '36316445822', 'gislaine.c.cesario@gmail.com', '(11) 99269-6670');

SET @id_motorista_demo = 1;
SET @id_motorista_gislaine = 2;

-- 3. VEÍCULOS ELÉTRICOS (SUPORTE A MULTI-VEÍCULOS POR CONDUTOR)
INSERT INTO veiculo (id_motorista, modelo, marca, placa, renavan, tipo_conector) VALUES
-- Veículos do Motorista Demo:
(@id_motorista_demo, 'Dolphin EV', 'BYD', 'BRA2E19', '12345678901', 'TIPO_2'),
(@id_motorista_demo, 'EX30 Ultra', 'Volvo', 'VOL2E26', '98765432109', 'CCS2'),
-- Multi-Veículos da Motorista Gislaine:
(@id_motorista_gislaine, 'Song Plus DM-i', 'BYD', 'GIS2026', '55443322110', 'TIPO_2'),
(@id_motorista_gislaine, 'EX30 Plus', 'Volvo', 'GIS3E26', '99887766554', 'CCS2');

SET @id_veiculo_demo = 1;
SET @id_veiculo_gislaine_1 = 3;
SET @id_veiculo_gislaine_2 = 4;

-- 4. ANFITRIÕES (HOSTS)
INSERT INTO host (id_usuario, nome, email, cpf, telefone, dados_bancarios) VALUES
(@user_carlos, 'Carlos Eduardo M.', 'carlos@recargafacil.com', '321.654.987-11', '(15) 99123-8899', 'PIX: carlos@recargafacil.com (Nubank)'),
(@user_nexus, 'Nexus Hub Inovação', 'contato@nexushub.com', '12.345.678/0001-90', '(15) 3211-9000', 'PIX: 12.345.678/0001-90 (Itaú)'),
(@user_mariana, 'Dra. Mariana Santos', 'mariana@recargafacil.com', '456.789.123-55', '(15) 98144-5566', 'PIX: mariana@recargafacil.com (Santander)'),
(@user_gislaine, 'Gislaine Cristina Cesario Rodrigues', 'gislaine.c.cesario@gmail.com', '36316445822', '(11) 99269-6670', 'PIX: gislaine.c.cesario@gmail.com (Nubank)');

SET @host_carlos = 1;
SET @host_nexus = 2;
SET @host_mariana = 3;
SET @host_gislaine = 4;

-- 5. LOCAIS (COORDENADAS SOROCABA/SP - TELA 1 E ENDEREÇO PROTEGIDO)
INSERT INTO local (latitude, longitude, endereco_completo, tipo_ambiente, instrucoes_acesso, acesso_endereco_apos_pagamento) VALUES
-- Ponto 1: Residencial Campolim (Sorocaba)
(-23.5238, -47.4645, 'Rua Antonio Perez Hernandez, 480 - Condomínio Terra Nova, Casa 34, Parque Campolim, Sorocaba - SP', 'RESIDENCIAL', 'Identifique-se na portaria com o código de reserva. Vaga na garagem da direita. Anfitrião Carlos já notificado.', 1),

-- Ponto 2: Comercial Alto da Boa Vista (Sorocaba)
(-23.5015, -47.4580, 'Avenida Engenheiro Carlos Reinaldo Mendes, 2015 - Subsolo 1, Alto da Boa Vista, Sorocaba - SP', 'COMERCIAL', 'Retire o ticket na cancela. Vagas EV 01 e 02 no Piso -1. Ativação via QR Code.', 1),

-- Ponto 3: Residencial Jardim América (Sorocaba - Energia Solar)
(-23.5180, -47.4720, 'Rua Santa Clara, 1120 - Jardim América, Sorocaba - SP', 'RESIDENCIAL', 'Tocar interfone 01. Estacionar na vaga coberta frontal ao lado da Wallbox WEG.', 1);

SET @local_campolim = 1;
SET @local_nexus = 2;
SET @local_jdam = 3;

-- 6. WALLBOXES (PONTOS DE RECARGA NO MAPA)
INSERT INTO wallbox (id_host, id_local, modelo, potencia, preco_hora, comodidades, foto_url, status) VALUES
(@host_carlos, @local_campolim, 'Wallbox EcoCharge Campolim 7.4kW', 'P_7KW', 15.00, 'wifi,cafe,coberto,banheiro,cameras', 'https://images.unsplash.com/photo-1593941707882-a5bba14938c7?auto=format&fit=crop&w=800&q=80', 'DISPONIVEL'),
(@host_nexus, @local_nexus, 'Hub FastCharge Nexus 22kW', 'P_22KW', 22.00, 'wifi,cafe,banheiro,trabalho,cameras', 'https://images.unsplash.com/photo-1563986768609-322da13575f3?auto=format&fit=crop&w=800&q=80', 'DISPONIVEL'),
(@host_mariana, @local_jdam, 'Residência Solar Verde 11kW', 'P_11KW', 14.00, 'wifi,coberto,cameras,cafe', 'https://images.unsplash.com/photo-1617788138017-80ad40651399?auto=format&fit=crop&w=800&q=80', 'DISPONIVEL');

SET @wb_campolim = 1;
SET @wb_nexus = 2;
SET @wb_jdam = 3;

-- 7. HORÁRIOS DISPONÍVEIS NA AGENDA (SEMANA COMPLETA)
INSERT INTO agenda (id_wallbox, dia_semana, horario_inicio, horario_fim, esta_ativo) VALUES
-- Wallbox 1 (Residencial Campolim)
(@wb_campolim, 'SEGUNDA', '08:00:00', '22:00:00', 1),
(@wb_campolim, 'TERCA',   '08:00:00', '22:00:00', 1),
(@wb_campolim, 'QUARTA',  '08:00:00', '22:00:00', 1),
(@wb_campolim, 'QUINTA',  '08:00:00', '22:00:00', 1),
(@wb_campolim, 'SEXTA',   '08:00:00', '22:00:00', 1),
(@wb_campolim, 'SABADO',  '09:00:00', '20:00:00', 1),
(@wb_campolim, 'DOMINGO', '09:00:00', '18:00:00', 1),

-- Wallbox 2 (Hub Nexus Comercial)
(@wb_nexus, 'SEGUNDA', '07:00:00', '20:00:00', 1),
(@wb_nexus, 'TERCA',   '07:00:00', '20:00:00', 1),
(@wb_nexus, 'QUARTA',  '07:00:00', '20:00:00', 1),
(@wb_nexus, 'QUINTA',  '07:00:00', '20:00:00', 1),
(@wb_nexus, 'SEXTA',   '07:00:00', '20:00:00', 1),
(@wb_nexus, 'SABADO',  '08:00:00', '16:00:00', 1),
(@wb_nexus, 'DOMINGO', '08:00:00', '14:00:00', 0),

-- Wallbox 3 (Residencial Solar Verde)
(@wb_jdam, 'SEGUNDA', '08:00:00', '19:00:00', 1),
(@wb_jdam, 'TERCA',   '08:00:00', '19:00:00', 1),
(@wb_jdam, 'QUARTA',  '08:00:00', '19:00:00', 1),
(@wb_jdam, 'QUINTA',  '08:00:00', '19:00:00', 1),
(@wb_jdam, 'SEXTA',   '08:00:00', '19:00:00', 1),
(@wb_jdam, 'SABADO',  '09:00:00', '18:00:00', 1),
(@wb_jdam, 'DOMINGO', '10:00:00', '16:00:00', 1);

-- 8. EXEMPLO DE RESERVA E PAGAMENTO (HISTÓRICO)
INSERT INTO reserva (id_motorista, id_veiculo, id_wallbox, data_reserva, horario, status_reserva) VALUES
(@id_motorista_demo, @id_veiculo_demo, @wb_campolim, '2026-08-26', '14:00:00', 'CONCLUIDA');
SET @reserva_demo = 1;

-- Pagamento com a taxa de 25% calculada: Subtotal R$ 30,00 + Taxa R$ 7,50 = R$ 37,50
INSERT INTO pagamento (id_reserva, valor_total, taxa_sistema, status_pagamento) VALUES
(@reserva_demo, 37.50, 7.50, 'APROVADO');

SELECT 'BANCO DE DADOS RECARGA FÁCIL POPULADO COM SUCESSO!' AS Status;
