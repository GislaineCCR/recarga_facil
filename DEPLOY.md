# Deploy da apresentação (UPX 4) — Recarga Fácil

> **Esta branch (`deploy`) existe apenas para hospedar o site durante a apresentação.**
> Ela **não substitui** a `main`. O projeto oficial continua na `main`.

## Link do site
https://recargafacil-d4ul.onrender.com

> Plano gratuito do Render: após ~15 min sem acesso o serviço "dorme" e o primeiro acesso leva cerca de 1 minuto. Abra o link uns 10 minutos antes de apresentar.

## O que difere da `main`
- O front-end está copiado em `src/main/resources/static/` e servido pelo próprio Spring Boot.
- A API é chamada por caminho relativo (`/api`), em vez de `http://localhost:8080/api`.
- Há um `Dockerfile` (build com Maven e execução com Java 17) usado pelo Render.
- O Render usa o perfil `h2` (banco em memória); o `DataInitializer` recria os dados de demonstração a cada início.
- O console H2 vem desligado (`H2_CONSOLE=false`).
- O e-mail pessoal no seed foi trocado por um fictício.
- Porta e credenciais do banco são lidas de variáveis de ambiente (`PORT`, `DB_URL`, `DB_USER`, `DB_PASSWORD`), com os valores locais como padrão.

## Como atualizar o deploy após mudanças no projeto
1. Leve a alteração para esta branch (`deploy`).
2. Se mudou o front, copie os arquivos para `src/main/resources/static/` e mantenha `/api` no lugar de `http://localhost:8080/api`.
3. `git push origin deploy` — o Render atualiza sozinho em ~3 a 5 minutos.

> Cada push na `deploy` refaz o deploy e **zera os dados cadastrados** (banco em memória). Evite fazer isso no dia da apresentação.

## Logins de demonstração (dados fictícios)
- Motorista: `motorista@email.com` / `senha123`
- Host: `carlos@recargafacil.com` / `senha123`
- Motorista/Host: `gislainecesario@recargafacil.com` / `1234`

## Depois da apresentação
O serviço pode ser apagado no Render (**Settings → Delete Web Service**). O projeto na `main` não é afetado.
