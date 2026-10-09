# ADR 0002: Identificadores Primários com UUID v7 (RFC 9562)

## Status
Aprovado

## Contexto
O CarFlow possui uma vitrine pública de veículos (`/api/v1/catalogo`) e endpoints administrativos.
Chaves numéricas sequenciais (`Long` / `BIGSERIAL`) expõem vulnerabilidades de IDOR e permitem inferir métricas de negócio (quantos carros a loja cadastrou). UUIDs v4 tradicionais, por serem 100% aleatórios, degradam índices B-Tree em tabelas com alta concorrência de escrita.

## Decisão
Utilizamos **UUID v7** como tipo padrão de chave primária (`id UUID`) em todas as entidades do sistema.
Utilizamos a biblioteca `com.fasterxml.uuid:java-uuid-generator` (ou gerador nativo do Hibernate) para criação dos identificadores ordenados no tempo.

## Consequências
- **Positivas:**
  - Inserções sequenciais no PostgreSQL 16 (mesma performance de B-Tree que IDs sequenciais).
  - Segurança nativa contra raspagem e enumeração na vitrine pública.
  - Unicidade global entre instâncias e tenants.
- **Trade-offs:**
  - Ocupa 16 bytes no banco (versus 8 bytes do `BIGINT`), plenamente aceitável para o volume do domínio.
