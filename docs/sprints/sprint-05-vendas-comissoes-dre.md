# Sprint 5: Vendas, Comissões Dinâmicas & Snapshot Contábil DRE

## 🎯 Objetivo da Sprint
Implementar o módulo mais crítico para o negócio: o fechamento da venda. Garantir a transação atômica (`@Transactional`), o cálculo da comissão do vendedor, a transição para status `VENDIDO` e a gravação de um **snapshot contábil imutável** que protege o histórico financeiro da revenda contra alterações futuras.

---

## 📦 Tarefas Detalhadas

### 1. Migração Flyway (`V4__create_vendas_table.sql`)
- [ ] Tabela `clientes`:
  - `id` (UUID v7, PK), `loja_id`, `nome`, `cpf_cnpj`, `telefone`, `email`.
- [ ] Tabela `vendas`:
  - `id` (UUID v7, PK)
  - `loja_id` (UUID v7, FK -> lojas.id, `@TenantId`)
  - `veiculo_id` (UUID v7, FK -> veiculos.id UNIQUE)
  - `vendedor_id` (UUID v7, FK -> usuarios.id)
  - `cliente_id` (UUID v7, FK -> clientes.id)
  - `data_venda` (TIMESTAMPTZ)
  - `valor_venda` (NUMERIC(12,2))
  - `forma_pagamento` (Enum: `A_VISTA_PIX`, `FINANCIAMENTO`, `CARTAO`, `MISTO`)
  - **Snapshot Contábil Imutável:**
    - `custo_compra_snapshot` (NUMERIC(12,2))
    - `total_preparacao_snapshot` (NUMERIC(12,2))
    - `custo_total_snapshot` (NUMERIC(12,2))
    - `margem_bruta_snapshot` (NUMERIC(12,2))
    - `percentual_comissao_snapshot` (NUMERIC(5,2))
    - `valor_comissao_snapshot` (NUMERIC(12,2))
    - `lucro_liquido_loja_snapshot` (NUMERIC(12,2))

### 2. Transação Atômica de Fechamento de Venda
- [ ] Criar o pacote `com.isaccosta.carflow.venda`.
- [ ] Regra transacional (`@Transactional`):
  1. Carrega o veículo e valida se está `DISPONIVEL` ou `RESERVADO`.
  2. Executa lock otimista (se outro vendedor vender primeiro, dispara conflito).
  3. Soma os custos de preparação atuais e congela nos campos de snapshot.
  4. Calcula a comissão do vendedor (regra configurável: % sobre valor total ou % sobre a margem de lucro).
  5. Salva a entidade `Venda`.
  6. Atualiza o veículo para `VENDIDO`.

### 3. Endpoints REST
- [ ] `POST /api/v1/vendas` (Realizar venda do veículo).
- [ ] `GET /api/v1/vendas` (Relatório analítico de vendas por período).
- [ ] `GET /api/v1/vendas/dre` (Demonstrativo do Resultado do Exercício: faturamento bruto, custos totais, comissões pagas e lucro líquido da loja no mês).

---

## 🏆 Critérios de Aceite da Sprint 5
1. Operação de venda é atômica: ou grava tudo (carro vendido + venda + snapshot), ou faz rollback de tudo.
2. Venda dupla bloqueada via `@Version` retornando HTTP 409 Conflict.
3. Se alguém alterar o custo de uma peça amanhã, o valor histórico da venda do mês passado permanece intocado (imutabilidade do snapshot).
4. Commit: `feat: implement sales flow with immutable financial snapshot and DRE reporting`.
