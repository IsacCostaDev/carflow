# CarFlow 🚗💨

> **Sistema Comercial e de Gestão Inteligente para Revendas de Veículos**

O **CarFlow** é uma plataforma SaaS multi-tenant desenvolvida para concessionárias e lojas independentes de veículos seminovos. O sistema resolve duas dores centrais do mercado automotivo:
1. **Vitrine Pública Desacoplada (`/api/v1/catalogo`):** Exposição online do estoque disponível em tempo real para compradores finais.
2. **Backoffice de Alta Precisão Contábil (`/api/v1/admin`):** Controle rigoroso de compra, despesas de preparação/reforma por veículo, tempo de giro em pátio, comissões de vendedores e **DRE Real por Veículo** (Margem Líquida Exata).

---

## 🛠️ Stack Tecnológica

| Camada | Tecnologia | Decisão Arquitetural |
|---|---|---|
| **Linguagem** | Java 21 (LTS) | Records, Virtual Threads, Pattern Matching |
| **Framework** | Spring Boot 3.4.x | Ecossistema enterprise padrão de mercado |
| **Persistência** | Spring Data JPA / Hibernate 6 | Rich Domain Model e isolamento via `@TenantId` |
| **Banco de Dados** | PostgreSQL 16 (Docker) | Relacional robusto, índices B-Tree otimizados |
| **Migrações** | Flyway | Versionamento estrito de DDL (`ddl-auto: validate`) |
| **Identificadores** | UUID v7 (RFC 9562) | Chaves ordenadas no tempo (alta performance e anti-IDOR) |
| **Concorrência** | Optimistic Locking (`@Version`) | Prevenção de venda dupla com HTTP 409 Conflict |
| **Precisão Monetária**| `BigDecimal` | Eliminação de erros de arredondamento IEEE 754 |

---

## 📐 Decisões Arquiteturais (ADRs)

Todas as decisões técnicas de alto nível estão documentadas em [`docs/adr/`](docs/adr/):
- **[ADR 0001: Multi-tenant com Hibernate 6 `@TenantId`](docs/adr/0001-multi-tenant-architecture.md)**
- **[ADR 0002: Identificadores Primários com UUID v7](docs/adr/0002-uuidv7-primary-keys.md)**
- **[ADR 0003: Controle de Concorrência com Optimistic Locking](docs/adr/0003-optimistic-locking-version.md)**

---

## 🚀 Como Executar

### Pré-requisitos
- Java 21+ instalado
- Docker e Docker Compose

### 1. Subir o Banco de Dados
```bash
docker compose up -d
```

### 2. Executar a Aplicação
```bash
./mvnw spring-boot:run
```

A API estará disponível em `http://localhost:8080`.
