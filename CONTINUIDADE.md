# CarFlow — Contexto de Continuidade e Estado Atual do Projeto

> **Para retomar em qualquer sessão ou máquina:** Este arquivo consolida todas as decisões arquiteturais, o estado exato dos arquivos criados e os próximos passos para continuar sem perda de contexto.

---

## 1. Identificação do Projeto
* **Nome do Projeto:** CarFlow
* **Pacote Base:** `com.isaccosta.carflow`
* **Caminho Local:** `/home/isaccosta/Projetos/carflow`
* **Repositório Remoto:** `https://github.com/IsacCostaDev/carflow.git`
* **Projeto Paralelo:** `/home/isaccosta/Projetos/mygarage` (mantido 100% intacto, reservado para projetos pessoais/vibe codado).

---

## 2. Princípios de Estudo e Trabalho do Isac
* **Objetivo:** Portfólio de altíssimo nível para vaga Java Backend.
* **Regra de Ouro:** **O Isac digita o código e guia a execução.** A IA deve sempre explicar o **PORQUÊ** de cada decisão técnica, mostrar alternativas e trade-offs, sem despejar blocos de código sem explicação prévia.
* **Comunicação:** Direto ao ponto, com rigor técnico e sem pop-ups intrusivos bloqueando a leitura.

---

## 3. Decisões Arquiteturais Batidas (Com ADRs)

| Decisão | Escolha | Por que essa escolha? | ADR |
|---|---|---|---|
| **Linguagem & Framework** | Java 21 (LTS) + Spring Boot 3.4.x | Virtual Threads (concorrência leve), Records (código imutável sem boilerplate) e padrão enterprise moderno. | — |
| **Isolamento Multi-tenant** | Hibernate 6 `@TenantId` + `TenantContext` | Injeção automática de `WHERE loja_id = ?` nas queries JPA. Zero risco de vazamento de dados entre revendas. | [ADR 0001](docs/adr/0001-multi-tenant-architecture.md) |
| **Chaves Primárias (PK)** | UUID v7 (RFC 9562) | Ordenação temporal (mesma performance de B-Tree que IDs sequenciais) + anti-IDOR/anti-raspagem na vitrine pública. | [ADR 0002](docs/adr/0002-uuidv7-primary-keys.md) |
| **Concorrência na Venda** | Optimistic Locking (`@Version`) | Evita "venda dupla" de veículos concorrentes sem travar o PostgreSQL, respondendo HTTP 409 Conflict. | [ADR 0003](docs/adr/0003-optimistic-locking-version.md) |
| **Ciclo de Vida do Veículo** | `EM_PREPARACAO` → `DISPONIVEL` → `RESERVADO` → `VENDIDO` | Realista: o carro só vai para a vitrine pública (`/api/v1/catalogo`) quando está pronto (`DISPONIVEL`). | — |
| **Comissão & DRE** | Regra dinâmica + Snapshot contábil imutável | O dono pode alterar a regra de comissão quando quiser; as vendas passadas mantêm o snapshot histórico imutável. | — |
| **Precisão Monetária** | `BigDecimal` obrigatório | Elimina erros de arredondamento de ponto flutuante do IEEE 754 em todos os cálculos de margem líquida. | — |
| **Banco & Migrações** | PostgreSQL 16 + Flyway (`V1__...`) | `ddl-auto: validate`. O schema é 100% versionado em scripts SQL reproduzíveis. | — |
| **Organização** | *Package by Feature* | Pacotes por contexto de negócio (`veiculo`, `preparacao`, `venda`, `cliente`, `loja`, `usuario`). | — |

---

## 4. O que já está pronto na máquina
1. **Estrutura base gerada:**
   * `pom.xml` completo (Java 21, Spring Boot 3.4.3, JPA, Security, Validation, PostgreSQL, Flyway, Lombok, java-uuid-generator, Tests).
   * `src/main/resources/application.yml` configurado para Postgres + Flyway.
   * `docker-compose.yml` com PostgreSQL 16 Alpine e volume persistente configurado.
   * `CarFlowApplication.java` e teste de contexto.
   * `README.md` e ADRs 0001, 0002 e 0003 em `docs/adr/`.
2. **Git Local:**
   * Repositório inicializado na branch `main`.
   * Commit inicial realizado.
   * Remote configurado: `https://github.com/IsacCostaDev/carflow.git`.

---

## 5. Roteiro exato para quando você voltar

### Passo 1: Abrir o projeto no Antigravity
* Vá em **File > Open Folder...** e selecione `/home/isaccosta/Projetos/carflow`.

### Passo 2: Publicar no GitHub
* Clique no ícone de **Source Control** (`Ctrl + Shift + G`) na barra lateral esquerda e clique em **Publish to GitHub** (ou suba pelo terminal).

### Passo 3: Onde vamos recomeçar a codificação
* Desenhar o **DER (Modelo de Dados do Banco)**:
  * Tabela `lojas` (tenants)
  * Tabela `usuarios` (vendedores / administradores)
  * Tabela `clientes` (compradores)
  * Tabela `veiculos` (dados técnicos + `@Version`)
  * Tabela `custos_preparacao` (despesas rastreadas)
  * Tabela `vendas` (comissões e snapshots do DRE)
* Escrever o primeiro script Flyway: `src/main/resources/db/migration/V1__init_schema.sql`.
* Subir o banco via Docker (`docker compose up -d`) e rodar a primeira migração!
