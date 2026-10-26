# Sprint 2: Autenticação, Spring Security 6 & JWT

## 🎯 Objetivo da Sprint
Construir uma camada de segurança robusta, stateless e multi-tenant utilizando Spring Security 6 e tokens JWT. Garantir que cada requisição autenticada extraia o `loja_id` do token e popule o `TenantContext` do Hibernate sem risco de vazamento de dados entre revendas.

---

## 📦 Tarefas Detalhadas

### 1. Spring Security 6 & Criptografia
- [x] Configurar `SecurityFilterChain` com `SessionCreationPolicy.STATELESS`.
- [x] Configurar `BCryptPasswordEncoder` com salt para armazenamento seguro de senhas.
- [x] Definir regras de acesso por rota (rotas públicas `/api/v1/auth/**`, rotas autenticadas `/api/v1/**`).

### 2. JWT (JSON Web Token)
- [x] Implementar serviço de geração e validação de tokens JWT (assinatura HMAC-SHA256 ou RSA).
- [x] Claims do Token:
  - `sub`: ID do usuário.
  - `loja_id`: ID da revenda (tenant).
  - `role`: Perfil de acesso (`DONO`, `VENDEDOR`, etc.).
  - `exp`: Expiração curta (ex: 15 a 30 minutos).
- [x] Estratégia de Refresh Token para renovação segura.

### 3. Filtro Multi-tenant (`TenantContextFilter`)
- [x] Criar o `TenantContext` baseado em `ThreadLocal` para guardar o `loja_id` da requisição atual.
- [x] Criar o `JwtAuthenticationFilter` que intercepta cada requisição HTTP:
  - Valida o Bearer Token.
  - Popula o `SecurityContext` do Spring.
  - Configura o `TenantContext` para o Hibernate aplicar o filtro `@TenantId` em todas as queries.
  - Garante a limpeza do `ThreadLocal` no bloco `finally` para evitar contaminação de threads de requisição.

### 4. Endpoints de Autenticação
- [x] `POST /api/v1/auth/login` (recebe e-mail, senha e devolve access_token + refresh_token).
- [x] `POST /api/v1/auth/refresh` (renova o access_token sem exigir login novamente).

---

## 🏆 Critérios de Aceite da Sprint 2
1. Tentativa de acessar `/api/v1/veiculos` sem token retorna HTTP 401 Unauthorized.
2. Login com credenciais válidas retorna JWT assinado contendo o `loja_id`.
3. Testes unitários com Mockito cobrindo o serviço de autenticação e validação do token.
4. Commit: `feat: add spring security 6, jwt stateless auth and tenant context filter`.
