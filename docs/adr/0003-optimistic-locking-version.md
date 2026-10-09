# ADR 0003: Controle de Concorrência com Optimistic Locking

## Status
Aprovado

## Contexto
Veículos são ativos físicos únicos em estoque. Em horários de pico, múltiplos vendedores podem tentar fechar a venda do mesmo veículo simultaneamente.

## Decisão
Implementamos **Optimistic Locking** através da anotação JPA `@Version` na entidade `Veiculo`.
Ao tentar concretizar a venda de um carro que acabou de ser alterado ou vendido, o Hibernate dispara uma `OptimisticLockException`. O backend intercepta essa exceção em `@RestControllerAdvice` e responde com HTTP `409 Conflict` padronizado via RFC 7807 (`ProblemDetail`).

## Consequências
- **Positivas:**
  - Zero bloqueio físico no PostgreSQL (`SELECT FOR UPDATE`), mantendo o banco ágil e livre de contenções.
  - Tratamento determinístico de concorrência com feedback imediato ao usuário.
- **Trade-offs:**
  - O usuário que perder a corrida concorrente tem a transação rejeitada e deve ser informado pela interface.
