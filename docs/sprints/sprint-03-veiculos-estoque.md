# Sprint 3: Domínio de Veículos, Estoque & Concorrência

## 🎯 Objetivo da Sprint
Implementar o coração operacional do CarFlow: a gestão de estoque de veículos com controle de ciclo de vida (máquina de estados), concorrência com Optimistic Locking (`@Version`), isolamento automático por revenda e exposição da vitrine pública.

---

## 📦 Tarefas Detalhadas

### 1. Migração Flyway (`V2__create_veiculos_table.sql`)
- [ ] Tabela `veiculos`:
  - `id` (UUID v7, PK)
  - `loja_id` (UUID v7, FK -> lojas.id, `@TenantId`)
  - `marca`, `modelo`, `versao` (VARCHAR)
  - `ano_fabricacao`, `ano_modelo` (INT)
  - `placa` (VARCHAR 10)
  - `chassi` (VARCHAR 17)
  - `cor` (VARCHAR 50)
  - `km_atual` (INT)
  - `combustivel` (Enum: `GASOLINA`, `ETANOL`, `FLEX`, `DIESEL`, `ELETRICO`, `HIBRIDO`)
  - `cambio` (Enum: `MANUAL`, `AUTOMATICO`)
  - `preco_compra` (NUMERIC(12,2) - `BigDecimal`)
  - `preco_anuncio` (NUMERIC(12,2) - `BigDecimal`)
  - `status` (Enum: `EM_PREPARACAO`, `DISPONIVEL`, `RESERVADO`, `VENDIDO`)
  - `version` (BIGINT NOT NULL DEFAULT 0 - Optimistic Locking)
  - Chave composta única: `UNIQUE(loja_id, placa)` e `UNIQUE(loja_id, chassi)`.

### 2. Máquina de Estados do Ciclo de Vida
- [ ] Regras de transição de status no domínio:
  - `EM_PREPARACAO` -> pode ir para `DISPONIVEL` após inspeção.
  - `DISPONIVEL` -> pode ir para `RESERVADO` ou `VENDIDO`.
  - `VENDIDO` -> estado terminal (não pode ser reeditado).
- [ ] Impedir que veículos em `EM_PREPARACAO` apareçam no catálogo público.

### 3. Endpoints REST (Package `com.isaccosta.carflow.veiculo`)
- [ ] `POST /api/v1/veiculos` (Cadastrar veículo no estoque da revenda autenticada).
- [ ] `GET /api/v1/veiculos` (Listagem paginada e filtrada por status do tenant).
- [ ] `GET /api/v1/veiculos/{id}` (Detalhes com verificação de tenant).
- [ ] `PATCH /api/v1/veiculos/{id}/status` (Mudança controlada de status).
- [ ] `GET /api/v1/catalogo/{lojaId}` (Endpoint público aberto para clientes da vitrine).

### 4. Teste de Concorrência
- [ ] Escrever teste automatizado simulando duas requisições paralelas tentando alterar o mesmo veículo: validar que a segunda requisição recebe `OptimisticLockingFailureException` (HTTP 409 Conflict).

---

## 🏆 Critérios de Aceite da Sprint 3
1. CRUD de veículos funcionando com filtro de tenant automático (Loja A não consegue enxergar nem alterar veículos da Loja B).
2. Validações com Bean Validation (`@NotNull`, `@NotBlank`, `@Positive`).
3. Concorrência com `@Version` impedindo updates concorrentes inconsistentes.
4. Commit: `feat: implement veiculos domain, lifecycle state machine and optimistic locking`.
