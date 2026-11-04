# Plano e Relatório de Qualidade e Teste de Software — Projeto Recarga Fácil

**Projeto:** Plataforma Recarga Fácil (Compartilhamento P2P de Estações de Recarga Veicular — Wallbox)  
**Contexto Acadêmico:** UPX 4 / Disciplina de Qualidade e Teste de Software (4º Semestre — Facens)  
**Tecnologias de Teste:** Java, Spring Boot Test (`3.2.3`), JUnit 5 (Jupiter), Mockito, Spring `MockMvc` e Banco em Memória H2  
**Data da Execução:** 22/10/2026  
**Resultado Geral:** **20 Casos de Teste Executados | 20 Aprovados (100% de Sucesso) | 0 Falhas | 0 Erros**

---

## 1. Introdução e Objetivos de Qualidade (ISO/IEC 25010)

Este documento registra o planejamento, a especificação, a implementação e os resultados da bateria de testes automatizados aplicada ao sistema **Recarga Fácil**, em conformidade com os padrões de documentação de testes **IEEE 829 / ISO/IEC/IEEE 29119** e os atributos de qualidade de produto de software da norma **ISO/IEC 25010**:

- **Adequação Funcional (Functional Suitability):** Garantir que todos os Requisitos Funcionais (`RF01` a `RF09`) — desde o cadastro de motoristas, veículos e anfitriões até o agendamento de reservas e o cálculo automático da taxa de intermediação de 25% — apresentem resultados exatos.
- **Confiabilidade (Reliability):** Assegurar que o sistema trate entradas inválidas e entidades inexistentes (como reservas ou estações não encontradas) lançando exceções controladas sem corromper o banco de dados.
- **Segurança (Security):** Validar o controle de acesso e autenticação por perfil (`HOST` vs. `MOTORISTA`), impedindo acessos com credenciais incorretas (`HTTP 401`) ou elevação indevida de privilégio entre perfis (`HTTP 403`).
- **Manutenibilidade e Testabilidade (Maintainability):** Permitir a validação contínua do sistema em ambiente isolado (banco SQL em memória H2 e dublês de teste Mockito), sem impactar o banco de dados de produção.

---

## 2. Estratégia e Níveis de Teste (Pirâmide de Testes)

A arquitetura de testes foi estruturada seguindo o modelo da **Pirâmide de Testes**, combinando técnicas de **Caixa-Branca (Estrutural)** e **Caixa-Preta (Funcional)**:

1. **Nível 1 — Testes Unitários Isolados (JUnit 5 + Mockito — Caixa-Branca):**
   - Utiliza `@ExtendWith(MockitoExtension.class)`, `@Mock` e `@InjectMocks`.
   - Isola a camada de serviço (`PagamentoService`, `ReservaService`, `MotoristaService`, `VeiculoService`, `HostService`, `AgendaService`) simulando o comportamento dos repositórios JPA.
   - Verifica ramificações condicionais (*branch coverage*), tratamento de exceções (`assertThrows`) e número exato de invocações ao banco (`verify(..., times(1))`).
2. **Nível 2 — Particionamento de Equivalência e Análise de Valor Limite (Caixa-Preta):**
   - Partições válidas e inválidas no cálculo de pagamento (com taxa informada vs. taxa nula calculada automaticamente em 25%).
   - Credenciais válidas (`200 OK`), credenciais inválidas (`401 Unauthorized`) e perfil sem permissão (`403 Forbidden`).
3. **Nível 3 — Testes de Integração e Fluxo Ponta a Ponta (`@SpringBootTest` + `MockMvc` + H2):**
   - Sobe o contexto real do Spring Boot utilizando o perfil `@ActiveProfiles("h2")` com banco de dados relacional em memória.
   - Exercita toda a pilha arquitetural: `Requisição HTTP JSON` -> `Controller` -> `Service` -> `Repository (Hibernate/JPA)` -> `Banco H2` -> `Resposta HTTP JSON`.

---

## 3. Arquitetura dos Arquivos de Teste Implementados

Os testes foram implementados no diretório padrão Maven `src/test/java/com/recargafacil/`, mantendo total separação do código de produção:

| Classe de Teste | Tipo / Nível | Camada Alvo | Qtd. de Testes |
| :--- | :--- | :--- | :---: |
| `src/test/java/com/recargafacil/service/PagamentoServiceTest.java` | Unitário (JUnit 5 + Mockito) | `PagamentoService` | 5 (`CT01`–`CT05`) |
| `src/test/java/com/recargafacil/service/ReservaServiceTest.java` | Unitário (JUnit 5 + Mockito) | `ReservaService` | 5 (`CT06`–`CT10`) |
| `src/test/java/com/recargafacil/service/CadastroServiceTest.java` | Unitário (JUnit 5 + Mockito) | `MotoristaService`, `VeiculoService`, `HostService`, `AgendaService` | 4 (`CT11`–`CT14`) |
| `src/test/java/com/recargafacil/controller/RecargaFacilIntegrationTest.java` | Integração / E2E (`MockMvc` + H2) | `AuthController`, `WallboxController`, `ReservaController`, `PagamentoController` | 6 (`CT15`–`CT20`) |

---

## 4. Matriz de Rastreabilidade (Requisitos Funcionais x Casos de Teste)

A matriz abaixo comprova que **100% dos Requisitos Funcionais (`RF01` a `RF09`)** do projeto possuem cobertura de testes automatizados:

| Código RF | Descrição do Requisito Funcional | Casos de Teste Associados | Nível de Teste | Status |
| :---: | :--- | :--- | :--- | :---: |
| **RF01** | Cadastrar Motorista e Autenticação de Cliente | `CT11`, `CT16`, `CT17` | Unitário e Integração | Aprovado |
| **RF02** | Cadastrar Veículo Elétrico vinculado ao Motorista | `CT11`, `CT12` | Unitário | Aprovado |
| **RF03** | Cadastrar Anfitrião (Host) e Autenticação de Host | `CT13`, `CT15`, `CT17`, `CT18` | Unitário e Integração | Aprovado |
| **RF04** | Cadastrar Ponto de Recarga (Wallbox e Local) | `CT13` | Unitário e Integração | Aprovado |
| **RF05** | Gerenciar Disponibilidade (Agenda de Horários) | `CT14` | Unitário | Aprovado |
| **RF06** | Buscar e Listar Estações de Recarga Disponíveis | `CT19` | Integração API | Aprovado |
| **RF07** | Registrar Reserva de Recarga | `CT06`, `CT07`, `CT08`, `CT10`, `CT20` | Unitário e Integração | Aprovado |
| **RF08** | Processar Pagamento e Taxa de 25% da Plataforma | `CT01`, `CT02`, `CT03`, `CT20` | Unitário e Integração | Aprovado |
| **RF09** | Atualizar Status da Reserva e do Pagamento | `CT01`, `CT04`, `CT05`, `CT09`, `CT20` | Unitário e Integração | Aprovado |

---

## 5. Especificação Detalhada dos Casos de Teste (CT01 a CT20)

| ID | Caso de Teste | Requisito | Entrada / Pré-condição | Resultado Esperado | Resultado Obtido | Status |
| :---: | :--- | :---: | :--- | :--- | :--- | :---: |
| **CT01** | Calcular taxa automática de 25% e concluir reserva | RF08 / RF09 | Reserva `ID=1` (`PENDENTE`), `valorTotal = 100.00`, `taxaSistema = null` | `taxaSistema = 25.00`, pagamento `APROVADO` e reserva alterada para `CONCLUIDA` | `taxaSistema = 25.00`, status `APROVADO` e reserva `CONCLUIDA` | **PASSOU** |
| **CT02** | Preservar taxa do sistema quando informada no DTO | RF08 | Reserva `ID=2`, `valorTotal = 60.00`, `taxaSistema = 15.00` | Manter `taxaSistema = 15.00` e aprovar pagamento | `taxaSistema = 15.00`, status `APROVADO` | **PASSOU** |
| **CT03** | Bloquear pagamento para reserva inexistente | RF08 | `idReserva = 999` (inexistente no banco) | Lançar `RuntimeException("Reserva não encontrada")` e não gravar pagamento | Exceção lançada com sucesso; `save()` nunca chamado | **PASSOU** |
| **CT04** | Atualizar status de um pagamento existente | RF09 | Pagamento `ID=10` (`PENDENTE`), novo status `REJEITADO` | Status do pagamento atualizado para `REJEITADO` | Status alterado para `REJEITADO` | **PASSOU** |
| **CT05** | Impedir atualização de pagamento inexistente | RF09 | `idPagamento = 404` (inexistente) | Lançar `RuntimeException` | Exceção lançada conforme esperado | **PASSOU** |
| **CT06** | Agendar nova reserva com sucesso | RF07 | `idMotorista=1`, `idWallbox=2`, `idVeiculo=3`, data `25/10/2026` às `14:30` | Reserva criada com status inicial `PENDENTE` e entidades vinculadas | Reserva salva com status `PENDENTE` e FKs corretas | **PASSOU** |
| **CT07** | Bloquear agendamento com Motorista inexistente | RF07 | `idMotorista=99` (inexistente), `idWallbox=1` | Lançar `RuntimeException("Motorista não encontrado")` | Exceção lançada; reserva não persistida | **PASSOU** |
| **CT08** | Bloquear agendamento com Wallbox inexistente | RF07 | `idMotorista=1`, `idWallbox=88` (inexistente) | Lançar `RuntimeException("Wallbox não encontrada")` | Exceção lançada; reserva não persistida | **PASSOU** |
| **CT09** | Atualizar status de reserva (Cancelamento) | RF09 | Reserva `ID=5` (`PENDENTE`), novo status `CANCELADA` | Reserva atualizada para `CANCELADA` | Status atualizado para `CANCELADA` | **PASSOU** |
| **CT10** | Consultar reservas por Host e por Motorista | RF07 | `hostId=10` e `motoristaId=20` | Retornar listas filtradas correspondentes | Listas retornadas com 1 registro cada | **PASSOU** |
| **CT11** | Cadastrar Motorista com Veículo e padronizar placa | RF01 / RF02 | Dados do motorista + veículo com placa em minúsculas `"abc1d23"` | Criar Usuário, Motorista e salvar Veículo com placa `"ABC1D23"` | Entidades criadas e placa convertida para `"ABC1D23"` | **PASSOU** |
| **CT12** | Cadastrar Veículo adicional para Motorista existente | RF02 | `idMotorista=1`, Volvo EX30, conector `CCS2` | Salvar veículo vinculado ao motorista `ID=1` | Veículo salvo e associado corretamente | **PASSOU** |
| **CT13** | Cadastrar Anfitrião (Host) com Local e Wallbox | RF03 / RF04 | Host `"Anfitrião Solar"`, Wallbox `22kW`, preço `R$ 18,50` e endereço | Salvar Usuário, Host e Wallbox com Local embutido | Host, Local e Wallbox persistidos com sucesso | **PASSOU** |
| **CT14** | Cadastrar horário na Agenda e alternar disponibilidade | RF05 | Wallbox `ID=1`, `SEGUNDA`, `08:00` às `18:00`, `ativo=true` | Criar agenda ativa e, ao alternar status, mudar `estaAtivo` para `false` | Agenda criada (`true`) e alternada (`false`) | **PASSOU** |
| **CT15** | Login de Anfitrião (Host) via API REST | RF03 | `POST /api/auth/login-host` com `carlos@recargafacil.com` / `senha123` | `HTTP 200 OK`, JSON com `tipo: "HOST"` | `HTTP 200 OK`, `tipo: "HOST"` | **PASSOU** |
| **CT16** | Login de Motorista via API REST | RF01 | `POST /api/auth/login-motorista` com `gislainecesario@recargafacil.com` / `1234` | `HTTP 200 OK`, JSON com `tipo: "MOTORISTA"` | `HTTP 200 OK`, `tipo: "MOTORISTA"` | **PASSOU** |
| **CT17** | Rejeitar login com senha incorreta via API REST | RF01 / RF03 | `POST /api/auth/login-host` com senha inválida | `HTTP 401 Unauthorized` e mensagem de erro | `HTTP 401 Unauthorized` | **PASSOU** |
| **CT18** | Impedir Motorista sem perfil de Host no painel de Host | RF03 | `POST /api/auth/login-host` com conta exclusiva de motorista | `HTTP 403 Forbidden` informando ausência de perfil de Anfitrião | `HTTP 403 Forbidden` | **PASSOU** |
| **CT19** | Listar estações de recarga disponíveis via API REST | RF06 | `GET /api/wallboxes` | `HTTP 200 OK`, lista JSON com estações `DISPONIVEL` | `HTTP 200 OK`, estação listada como `DISPONIVEL` | **PASSOU** |
| **CT20** | Fluxo Integrado Ponta a Ponta (Reserva -> Pagamento -> Conclusão) | RF07 / RF08 / RF09 | `POST /api/reservas` -> `POST /api/pagamentos` (`R$ 40,00`) -> `GET /api/reservas/{id}` | Reserva inicia `PENDENTE`, pagamento calcula `R$ 10,00` (25%) e reserva muda para `CONCLUIDA` | Reserva `CONCLUIDA` e taxa `10.00` gravada no banco H2 | **PASSOU** |

---

## 6. Evidência de Execução Automatizada (`mvn test`)

Comando utilizado para execução da suíte de testes:

```bash
mvn test
```

Saída real obtida pelo executor **Maven Surefire Plugin (`3.1.2`)**:

```text
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.recargafacil.controller.RecargaFacilIntegrationTest
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 18.46 s -- in com.recargafacil.controller.RecargaFacilIntegrationTest
[INFO] Running com.recargafacil.service.PagamentoServiceTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.689 s -- in com.recargafacil.service.PagamentoServiceTest
[INFO] Running com.recargafacil.service.CadastroServiceTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.386 s -- in com.recargafacil.service.CadastroServiceTest
[INFO] Running com.recargafacil.service.ReservaServiceTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.037 s -- in com.recargafacil.service.ReservaServiceTest
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 20, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  30.026 s
[INFO] Finished at: 2026-10-22T20:52:03-03:00
[INFO] ------------------------------------------------------------------------
```

---

## 7. Conclusão Técnica da Análise de Qualidade

A aplicação da bateria de testes automatizados validou com sucesso todas as camadas críticas do sistema **Recarga Fácil**:
1. **Regras financeiras e de agendamento (`RF07`, `RF08`, `RF09`):** A comissão de 25% da plataforma e a máquina de estados das reservas (`PENDENTE` -> `CONCLUIDA` / `CANCELADA`) funcionam sem desvios de arredondamento (`BigDecimal` com `RoundingMode.HALF_UP`).
2. **Controle de acesso e autenticação (`RF01`, `RF03`):** O sistema distingue corretamente os perfis de Motorista e Anfitrião (Host), retornando os códigos semânticos corretos do protocolo HTTP (`200`, `401`, `403`).
3. **Execução não-destrutiva:** Como toda a suíte roda utilizando mocks (Mockito) e banco em memória (H2), os testes podem ser executados a qualquer momento com `mvn test` sem alterar nenhum dado do ambiente principal.
