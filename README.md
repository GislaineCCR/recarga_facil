# Recarga Fácil (SyncEnergy)

> **Plataforma Peer-to-Peer (P2P) para localização, agendamento e utilização de pontos de recarga para veículos elétricos.**  
> Projeto de Extensão em Mobilidade e Urbanismo — Usina de Projetos Experimentais IV  
> **Centro Universitário Facens (Sorocaba - SP, 2026)**

---

## 📌 Sobre o Projeto

O **Recarga Fácil** é uma solução web responsiva desenvolvida para mitigar o gargalo de infraestrutura da mobilidade elétrica no Brasil, conectando motoristas de veículos elétricos a donos de carregadores residenciais e comerciais ociosos (Wallboxes).

A plataforma utiliza o conceito de economia compartilhada para transformar tomadas e carregadores privados em uma rede descentralizada de abastecimento, combatendo a "ansiedade de autonomia" de motoristas em cidades do interior e regiões com carência de eletropostos públicos (com foco inicial na região metropolitana de Sorocaba e Alumínio - SP).

### 🎯 Alinhamento com as ODS da ONU (Agenda 2030)
- **ODS 7 (Energia Limpa e Acessível):** Facilita o acesso descentralizado à recarga em bairros e cidades do interior.
- **ODS 8 (Trabalho Decente e Crescimento Econômico):** Gera renda extra para proprietários residenciais e novos clientes para comércios parceiros.
- **ODS 9 (Indústria, Inovação e Infraestrutura):** Transforma o software em infraestrutura inteligente sem necessidade de grandes obras civis.
- **ODS 11 (Cidades e Comunidades Sustentáveis):** Otimiza o uso de equipamentos existentes nas garagens, evitando impacto na via pública.
- **ODS 13 (Ação Contra a Mudança Global do Clima):** Estimula a transição energética e a redução de emissão de gases de efeito estufa.

---

## 💻 Funcionalidades & Módulos da Aplicação

O sistema foi concebido sob a abordagem **Mobile-First**, funcionando de maneira fluida em navegadores de smartphones e desktops, dividido em três portais dedicados:

### 1. Portal do Motorista (`app.html`)
- **Mapa Interativo & Geolocalização:** Busca e visualização de carregadores no mapa via **Leaflet.js** e OpenStreetMap, com cálculo de rota e distância.
- **Filtros Personalizados:** Filtragem por tipo de ambiente (*Residencial* ou *Comercial*) e potência do carregador (7.4 kW, 11 kW, 22 kW).
- **Agendamento Inteligente:** Reserva de horários sincronizada diretamente com a grade semanal disponibilizada pelo anfitrião.
- **Proteção de Privacidade (RN01):** O endereço exato da residência e os detalhes de acesso permanecem protegidos, sendo liberados apenas após a confirmação do agendamento/pagamento.
- **Perfil do Motorista:** Gestão de dados pessoais, cadastro e seleção do veículo elétrico (modelo, placa, conector) e foto de perfil.

### 2. Portal do Anfitrião / Host (`host.html`)
- **Dashboard & Indicadores (BI):** Acompanhamento de sessões realizadas, total faturado e valor líquido a receber.
- **Cadastro de Estações Wallbox:** Inclusão de dados técnicos, comodidades, localização no mapa e fotos.
- **Validação Mandatória de Vaga de Estacionamento:** Requisito obrigatório de comprovação de vaga física para o veículo estacionar durante a recarga e declaração de autorização condominial.
- **Disponibilidade Rápida:** Chave liga/desliga para pausar ou reativar a estação a qualquer momento.
- **Gestão da Grade Semanal de Horários:** Configuração individual por dia da semana (segunda a domingo) com horários de início e término integrados ao banco de dados.
- **Perfil do Anfitrião:** Edição de dados de contato, chave PIX para repasse financeiro e upload de foto de perfil.

### 3. Painel Administrativo (`admin.html`)
- **Monitoramento de Rede:** Visão consolidada de todas as estações cadastradas e seus status em tempo real.
- **Auditoria de Reservas:** Histórico detalhado de agendamentos, motoristas atendidos, transações e repasses.

---

## ⚖️ Regras de Negócio do Sistema (RN)

- **RN01 — Segurança Residencial:** O endereço completo e as instruções de acesso da residência só são revelados ao motorista após a confirmação do pagamento da reserva.
- **RN02 — Prioridade de Busca:** A listagem inicial de busca prioriza e ordena os pontos pela menor distância física em relação ao motorista.
- **RN03 — Modelo Financeiro & Intermediação:** A plataforma adota como referência o valor de R$ 15,00/hora em wallbox residencial de 7 kW, aplicando taxa de intermediação de 25% para manutenção do serviço e repassando o valor líquido diretamente ao anfitrião.
- **RN04 — Tolerância e No-Show:** Tolerância de 15 minutos para início da recarga; caso o motorista não compareça, o horário é liberado para novos usuários e são aplicadas regras de cancelamento.
- **Requisito Físico de Estacionamento:** O host só pode ativar sua estação se comprovar disponibilidade de vaga própria/segura para o veículo, garantindo a fluidez da via pública.

---

## 🛠️ Tecnologias Utilizadas

### Front-end
- **HTML5 semântico** & **CSS3 moderno** (Variáveis CSS, Flexbox, CSS Grid)
- **JavaScript (ES6+) modular** & **Fetch API** nativa
- **Leaflet.js** (Renderização cartográfica interativa e marcação de pontos de recarga)
- **OpenStreetMap** (Camada cartográfica global gratuita)
- **Remix Icon** (Iconografia vetorial)
- **Google Fonts** (Tipografia *Plus Jakarta Sans*)

### Back-end
- **Java 17**
- **Spring Boot 3**
- **Spring Web** (Construção de APIs RESTful)
- **Spring Data JPA / Hibernate** (Mapeamento Objeto-Relacional e persistência)
- **Spring Boot Starter Test** (Testes unitários e de integração)
- **Springdoc OpenAPI / Swagger UI** (Documentação interativa dos endpoints REST)
- **Maven** (Gerenciamento de dependências e build)

### Banco de Dados
- **MySQL 8.0** (Banco de dados relacional principal em produção e desenvolvimento)
- **H2 Database** (Banco em memória para testes imediatos sem dependências)

### Infraestrutura & Ferramentas
- **Docker** & **Docker Compose** (Containerização do banco MySQL)
- **Git** & **GitHub** (Versionamento e controle de branches)
- **Trello** & **Microsoft Teams** (Gestão ágil de atividades)

---

## 📂 Estrutura do Repositório

```text
recarga_facil/
│
├── css/                                # Folhas de estilo da aplicação
│   ├── components.css                  # Componentes reutilizáveis (botões, cards, badges)
│   ├── landing.css                     # Estilos da página de entrada
│   ├── main.css                        # Variáveis de tema e reset global
│   └── screens.css                     # Estilos de telas e layouts do mapa
│
├── js/                                 # Scripts do Front-end
│   ├── api.js                          # Módulo de comunicação com a API REST
│   └── app.js                          # Lógica do mapa Leaflet, filtros e reservas
│
├── src/
│   ├── main/
│   │   ├── java/com/recargafacil/
│   │   │   ├── controller/             # Controladores REST (Wallbox, Host, Motorista, Agenda, Reserva)
│   │   │   ├── dto/                    # Objetos de Transferência de Dados (DTOs)
│   │   │   ├── model/                  # Entidades JPA (Motorista, Veiculo, Host, Wallbox, Local, Agenda, Reserva)
│   │   │   ├── repository/             # Interfaces Spring Data JPA
│   │   │   └── service/                # Camada de Regras de Negócio e Serviços
│   │   │
│   │   └── resources/
│   │       ├── application.properties  # Configuração mestre do Spring Boot
│   │       ├── application-h2.properties
│   │       └── application-mysql.properties
│   │
│   └── test/                           # Testes automatizados
│
├── admin.html                          # Painel Administrativo
├── app.html                            # Portal do Motorista (Mapa e Reservas)
├── host.html                           # Portal do Anfitrião (Gestão e Agenda)
├── index.html                          # Landing Page da plataforma
├── docker-compose.yml                  # Configuração do container MySQL
├── Dockerfile                          # Build do container da aplicação
├── insert_data.sql                     # Script de carga inicial e dados de simulação
├── pom.xml                             # Configurações de dependências Maven
└── README.md                           # Documentação oficial do projeto
```

---

## 🚀 Como Executar o Projeto Localmente

### Pré-requisitos
- **Java 17 JDK** instalado
- **Maven 3.8+** instalado
- **Docker e Docker Compose** instalados (opcional caso use H2)
- Navegador web moderno

---

### Passo 1: Inicializar o Banco de Dados

#### Opção A — MySQL via Docker (Recomendado para simulação real)
No terminal, execute:
```bash
docker compose up -d mysql
```
*O serviço subirá na porta mapeada `3307` conforme configurado no projeto.*

#### Opção B — H2 Database (Sem necessidade de Docker)
Basta ativar o profile `h2` na execução do Spring Boot.

---

### Passo 2: Inicializar o Back-end (Spring Boot)

Com o banco ativo, execute:
```bash
mvn clean spring-boot:run
```
O servidor inicializará na porta padrão **`8080`**.

#### Acessar a Documentação Interativa da API (Swagger UI):
Após subir a aplicação, abra no navegador:
```text
http://localhost:8080/swagger-ui.html
```

---

### Passo 3: Inicializar o Front-end

Você pode servir os arquivos estáticos através de qualquer servidor HTTP simples. Por exemplo, utilizando Python:
```bash
python3 -m http.server 3000
```
Ou utilizando a extensão **Live Server** no VS Code.

Em seguida, acesse no navegador:
- **Landing Page:** [http://localhost:3000/index.html](http://localhost:3000/index.html)
- **Portal do Motorista:** [http://localhost:3000/app.html](http://localhost:3000/app.html)
- **Portal do Anfitrião:** [http://localhost:3000/host.html](http://localhost:3000/host.html)
- **Painel Administrativo:** [http://localhost:3000/admin.html](http://localhost:3000/admin.html)

---

## 👥 Integrantes do Projeto (SyncEnergy)

| Integrante | Papel no Projeto |
| :--- | :--- |
| **Letícia Maria Pedroso Nunes** | Líder do Projeto & Gestão Ágil |
| **Eduardo Henrique da Silva** | Analista de Requisitos e Negócio |
| **Gislaine Cristina Cesário Rodrigues** | Desenvolvedora Back-end & Banco de Dados |
| **Breno Garcia da Silva** | Desenvolvedor Front-end & UI/UX |
| **Luís Filipe de Souza Pereira** | Analista de Dados & Modelagem de Banco |
| **Josué Carvalho** | Analista de Testes e Qualidade (QA) |
