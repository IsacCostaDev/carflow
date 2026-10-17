# Sprint 6: Observabilidade, Documentação Swagger & CI/CD

## 🎯 Objetivo da Sprint
Polir o projeto para atingir padrão corporativo de alto nível: documentar todos os endpoints interativamente com OpenAPI 3 / Swagger, padronizar respostas de erro conforme a RFC 7807 (Problem Details), ativar métricas de saúde com Spring Boot Actuator e automatizar a integração contínua (CI) via GitHub Actions.

---

## 📦 Tarefas Detalhadas

### 1. Documentação OpenAPI 3 (Swagger)
- [ ] Adicionar dependência `springdoc-openapi-starter-webmvc-ui`.
- [ ] Configurar metadados da API (Título, Versão, Descrição, Licença, Contato).
- [ ] Configurar esquema de segurança global no Swagger para envio de Bearer JWT (`Authorize` button).
- [ ] Anotar endpoints com `@Operation`, `@ApiResponse` e exemplos de payload.
- [ ] Acessar `/swagger-ui.html` e testar requisições diretamente pela interface gráfica.

### 2. Padronização de Erros RFC 7807 (Problem Details)
- [ ] Ativar suporte nativo do Spring 3: `spring.mvc.problemdetails.enabled: true`.
- [ ] Criar `@RestControllerAdvice` global (`GlobalExceptionHandler`):
  - Tratamento de `OptimisticLockingFailureException` -> HTTP 409 Conflict.
  - Tratamento de `MethodArgumentNotValidException` -> HTTP 400 Bad Request com lista detalhada de campos inválidos.
  - Tratamento de `AccessDeniedException` -> HTTP 403 Forbidden.
  - Tratamento de exceções de domínio -> HTTP 422 Unprocessable Entity.
- [ ] Formato padrão retornado:
  ```json
  {
    "type": "https://api.carflow.isaccosta.com/errors/conflict",
    "title": "Conflito de Concorrência",
    "status": 409,
    "detail": "Este veículo já foi alterado ou vendido por outro usuário.",
    "instance": "/api/v1/vendas"
  }
  ```

### 3. Observabilidade com Spring Boot Actuator
- [ ] Adicionar `spring-boot-starter-actuator`.
- [ ] Expor endpoints `/actuator/health` e `/actuator/info`.
- [ ] Configurar health check detalhado para PostgreSQL e espaço em disco.

### 4. Pipeline de Integração Contínua (GitHub Actions)
- [ ] Criar `.github/workflows/ci.yml`.
- [ ] Configurar job com JDK 21 (Temurin).
- [ ] Configurar serviço Docker no runner para rodar os testes com Testcontainers.
- [ ] Executar `./mvnw clean verify` automaticamente a cada Pull Request e push na `main`.

---

## 🏆 Critérios de Aceite da Sprint 6
1. Documentação Swagger disponível e navegável em `/swagger-ui.html`.
2. Todos os erros da API retornam JSON em conformidade com a RFC 7807.
3. Pipeline do GitHub Actions executando com sucesso (badge verde no README).
4. Commit: `chore: add openapi documentation, rfc 7807 problem details and github actions ci`.
