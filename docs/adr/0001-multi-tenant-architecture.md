# ADR 0001: Arquitetura Multi-tenant com Hibernate 6 `@TenantId`

## Status
Aprovado

## Contexto
O CarFlow é um sistema projetado para atender revendas e concessionárias independentes de veículos seminovos.
Precisamos garantir isolamento estrito de dados para que uma revenda nunca acesse veículos, despesas, clientes ou métricas financeiras de outra loja.

## Decisão
Adotamos arquitetura **Multi-tenant (Shared Database, Shared Schema)** utilizando a anotação nativa `@TenantId` do Hibernate 6 (Spring Boot 3.4.x).
O identificador do tenant (`loja_id UUID`) é extraído do JWT via `OncePerRequestFilter` e registrado no contexto de execução (`TenantContext`). O Hibernate injeta automaticamente a cláusula `WHERE loja_id = ?` em todas as consultas SQL geradas e vincula o tenant em operações de escrita.

## Consequências
- **Positivas:**
  - Impossibilidade de vazamento acidental de dados por esquecimento de filtro em queries personalizadas.
  - Esquema único no PostgreSQL 16 gerenciado de forma simples e segura pelo Flyway.
  - Baixo custo operacional de infraestrutura em relação a esquemas isolados ou banco por tenant.
- **Trade-offs:**
  - Exige implementação de `CurrentTenantIdentifierResolver` no Spring Boot.
  - Consultas nativas (native SQL) fora do JPA exigem atenção redobrada ou uso de Views/RLS.
