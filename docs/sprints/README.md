# CarFlow — Roadmap Oficial das Sprints

> Guia mestre de planejamento e execução do CarFlow. Cada sprint foi desenhada para ser enxuta, modular e focada em uma fatia funcional do sistema, respeitando blocos curtos de tempo contínuo e garantindo aprendizado profundo de padrões enterprise em Java 21 e Spring Boot 3.4+.

---

## 🧭 Pilares Técnicos Batidos

| Pilar | Decisão | Benefício Profissional |
|---|---|---|
| **Linguagem & Framework** | Java 21 (LTS) + Spring Boot 3.4.x | Records, Virtual Threads, Injeção de Dependências, Ecossistema moderno. |
| **Arquitetura de Código** | Package-by-Feature | Alta coesão por contexto de negócio (`tenant`, `veiculo`, `venda`, etc.). |
| **Isolamento de Dados** | Hibernate 6 `@TenantId` + Shared Schema | Multi-tenancy automático em nível de linha com zero risco de vazamento. |
| **Identificadores (PK)** | UUID v7 (RFC 9562) | Ordenação temporal sem gargalo de B-Tree e anti-enumeração pública. |
| **Segurança & Auth** | JWT Stateless (Access + Refresh Token) | Spring Security 6 desacoplado, sem estado de sessão no servidor. |
| **Concorrência** | Optimistic Locking (`@Version`) | Prevenção de venda dupla concorrente respondendo HTTP 409 Conflict. |
| **Precisão Financeira** | `BigDecimal` obrigatório | Elimina erros de ponto flutuante em custos, comissões e DRE. |
| **Banco & Migrações** | PostgreSQL 16 + Flyway (`V1__...`) | Schema versionado, auditável e 100% reproduzível. |
| **Testes Automatizados** | JUnit 5 + Mockito + Testcontainers | Testes unitários puros + testes de integração com Postgres real em Docker. |
| **Documentação da API** | OpenAPI 3 (Swagger) + RFC 7807 | Documentação interativa e tratamento de erros padronizado. |

---

## 📋 Sumário das Sprints

| Sprint | Arquivo | Foco Principal | Entregável Funcional |
|---|---|---|---|
| **Sprint 0** | `sprint-00-fundacao-git-ambiente.md` | Git, GitHub & Docker Compose | Repositório na nuvem, Postgres rodando via Docker e sanity check da app. |
| **Sprint 1** | `sprint-01-database-tenants-flyway.md` | Flyway, Tenants & UUIDv7 | Primeira migration (`V1__`), tabelas `lojas` e `usuarios`, testes com Testcontainers. |
| **Sprint 2** | `sprint-02-auth-security-jwt.md` | Spring Security 6 & JWT | Endpoints `/auth/login` e `/auth/refresh`, filtro `TenantContextFilter`. |
| **Sprint 3** | `sprint-03-veiculos-estoque.md` | Domínio Veículos & Concorrência | Ciclo de vida (`DISPONIVEL`, etc.), CRUD tenant-safe, `@Version` e Catálogo público. |
| **Sprint 4** | `sprint-04-preparacao-custos.md` | Custos de Oficina & Preparação | Lançamento de despesas por veículo, custo acumulado e impacto na margem. |
| **Sprint 5** | `sprint-05-vendas-comissoes-dre.md` | Venda, Comissões & Snapshot DRE | Fechamento de venda atômico, cálculo dinâmico de comissão e snapshot imutável. |
| **Sprint 6** | `sprint-06-observabilidade-testes-ci.md` | Swagger, RFC 7807 & GitHub Actions | Documentação Swagger ativa, Problem Details e pipeline de CI executando no GitHub. |

---

## 🛠️ Regra de Ouro do Isac
1. **O Isac digita o código e guia a execução:** nenhum arquivo de regra de negócio é gerado sem explicação prévia.
2. **Entender o "PORQUÊ":** cada anotação (`@TenantId`, `@Version`, `@Transactional`) deve ter seu trade-off e razão técnica compreendidos.
3. **Commits Semânticos Obrigatórios:** cada sprint deve ter commits limpos usando Conventional Commits (`feat:`, `chore:`, `fix:`, `docs:`, `test:`).
