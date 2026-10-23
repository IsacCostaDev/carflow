# Sprint 0: Fundação, Git & Ambiente Local

## 🎯 Objetivo da Sprint
Consolidar a fundação do projeto, estabelecer a conexão segura com o GitHub na nuvem, subir o banco de dados PostgreSQL via Docker Compose e validar que o ecossistema Spring Boot inicia com sucesso.

---

## 📦 Tarefas Detalhadas

### 1. Git & GitHub
- [x] Entender a árvore de trabalho do Git: Working Directory vs. Staging Area vs. Local Repository.
- [x] Criar o repositório remoto no GitHub (`https://github.com/IsacCostaDev/carflow`).
- [x] Fazer o primeiro push (`git push -u origin main`).
- [x] Aprender o fluxo de branches: criar a branch `sprint/00-setup` para praticar o fluxo de Pull Request / Merge.

### 2. Infraestrutura Docker
- [x] Inspecionar o arquivo `docker-compose.yml`.
- [x] Entender por que usar PostgreSQL 16 Alpine (imagem leve, isolada e sem poluir o sistema operacional local).
- [x] Iniciar o container com `docker compose up -d`.
- [x] Validar a saúde do container com `docker ps` e logs (`docker compose logs -f`).

### 3. Conexão da Aplicação
- [x] Compreender o `src/main/resources/application.yml` (URL JDBC, credenciais, pooling com HikariCP).
- [x] Executar `./mvnw clean test` para garantir que o contexto do Spring Boot sobe e valida as conexões.

---

## 🏆 Critérios de Aceite da Sprint 0
1. Repositório público visível no GitHub: `https://github.com/IsacCostaDev/carflow`.
2. PostgreSQL 16 rodando localmente na porta 5432 via Docker.
3. Teste de Sanity do Spring Boot passando sem erros (`BUILD SUCCESS`).
4. Commit semântico final registrado: `chore: complete sprint 0 setup and environment validation`.
