# Sprint 1: Database, Multi-Tenancy & Flyway

## 🎯 Objetivo da Sprint
Implementar a fundação relacional no PostgreSQL com versionamento de banco via Flyway, definindo o modelo multi-tenant das revendas (`lojas`) e credenciais de acesso (`usuarios`), utilizando identificadores temporais UUID v7 e validando a integração via Testcontainers.

---

## 📦 Tarefas Detalhadas

### 1. Migração Flyway (`V1__init_schema.sql`)
- [x] Compreender o funcionamento do Flyway (`flyway_schema_history`) e por que o schema nunca é alterado via JPA em produção (`ddl-auto: validate`).
- [x] Escrever o DDL da tabela `lojas`:
  - `id` (UUID v7, PK)
  - `razao_social` (VARCHAR 150)
  - `nome_fantasia` (VARCHAR 150)
  - `cnpj` (VARCHAR 14 UNIQUE)
  - `ativo` (BOOLEAN DEFAULT true)
  - `created_at`, `updated_at` (TIMESTAMPTZ)
- [x] Escrever o DDL da tabela `usuarios`:
  - `id` (UUID v7, PK)
  - `loja_id` (UUID v7, FK -> lojas.id)
  - `nome` (VARCHAR 100)
  - `email` (VARCHAR 150)
  - `senha_hash` (VARCHAR 255)
  - `role` (VARCHAR 30: `DONO`, `GERENTE`, `VENDEDOR`)
  - Chave única composta: `UNIQUE(loja_id, email)` (o mesmo e-mail pode existir em lojas diferentes, mas não duplicado na mesma loja).

### 2. Entidades de Domínio JPA
- [x] Criar o pacote `com.isaccosta.carflow.tenant` (Package-by-Feature).
- [x] Criar a entidade JPA `Loja` com geração automática de UUID v7.
- [x] Criar a entidade JPA `Usuario` com `@TenantId` do Hibernate 6.
- [x] Criar os repositórios `LojaRepository` e `UsuarioRepository` estendendo `JpaRepository`.

### 3. Teste de Integração Real com Testcontainers
- [x] Configurar Testcontainers para subir um PostgreSQL 16 efêmero no teste.
- [x] Escrever o primeiro teste de integração que valida a execução das migrações do Flyway e o salvamento de uma Loja.

---

## 🏆 Critérios de Aceite da Sprint 1
1. Arquivo `src/main/resources/db/migration/V1__init_schema.sql` executado com sucesso ao subir a aplicação.
2. Isolamento multi-tenant garantido no schema com constraints e índices adequados.
3. Teste de integração com Testcontainers passando 100% verde.
4. Commit: `feat: implement V1 flyway migration, tenant domain and testcontainers setup`.
