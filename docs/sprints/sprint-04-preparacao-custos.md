# Sprint 4: Preparação, Oficina & Rastreamento de Custos

## 🎯 Objetivo da Sprint
Implementar o módulo financeiro de preparação de veículos. Permitir que a loja lance notas e despesas de oficina, funilaria, pneus, documentação e estética por veículo, calculando em tempo real o **Custo Total Real** do carro antes de definir o preço final de venda.

---

## 📦 Tarefas Detalhadas

### 1. Migração Flyway (`V3__create_custos_preparacao_table.sql`)
- [ ] Tabela `custos_preparacao`:
  - `id` (UUID v7, PK)
  - `loja_id` (UUID v7, FK -> lojas.id, `@TenantId`)
  - `veiculo_id` (UUID v7, FK -> veiculos.id)
  - `categoria` (Enum: `MECANICA`, `FUNILARIA_PINTURA`, `PNEUS`, `ESTETICA_HIGIENIZACAO`, `DOCUMENTACAO_IPVA`, `OUTROS`)
  - `descricao` (VARCHAR 255)
  - `valor` (NUMERIC(12,2) NOT NULL)
  - `data_realizacao` (DATE)
  - `fornecedor` (VARCHAR 100)
  - `created_at` (TIMESTAMPTZ)
- [ ] Índices por `(loja_id, veiculo_id)`.

### 2. Regras de Negócio e Cálculos Financeiros
- [ ] Criação do pacote `com.isaccosta.carflow.preparacao`.
- [ ] Regra de bloqueio: só é permitido lançar custos se o veículo estiver em status `EM_PREPARACAO` ou `DISPONIVEL` (veículos já `VENDIDO` têm custos congelados).
- [ ] Cálculo do Custo Consolidado:
  $$\text{Custo Real} = \text{Preço de Compra} + \sum(\text{Custos de Preparação})$$
- [ ] Cálculo da Margem Projetada:
  $$\text{Margem Projetada} = \text{Preço Anúncio} - \text{Custo Real}$$
- [ ] Uso estrito de `BigDecimal` com `RoundingMode.HALF_EVEN` (arredondamento bancário).

### 3. Endpoints REST
- [ ] `POST /api/v1/veiculos/{id}/custos` (Adicionar despesa a um veículo).
- [ ] `GET /api/v1/veiculos/{id}/custos` (Listar histórico de intervenções feitas no carro).
- [ ] `GET /api/v1/veiculos/{id}/financeiro-resumo` (Retorna Preço de Compra, Total de Preparação e Custo Real consolidado).

---

## 🏆 Critérios de Aceite da Sprint 4
1. Todas as despesas ficam atreladas ao veículo e isoladas pelo tenant da loja.
2. Cálculo financeiro 100% preciso com testes unitários cobrindo arredondamentos e somas.
3. Tentativa de lançar custo em carro vendido bloqueada com exceção de domínio (HTTP 422 Unprocessable Entity).
4. Commit: `feat: implement preparacao costs module and vehicle financial summary`.
