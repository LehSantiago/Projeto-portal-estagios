# Relatório da Entrega 2

## 1. Escopo

A Entrega 2 evolui o Portal de Estágios da Entrega 1 (console, JDBC, DAO e Services) para uma aplicação web com **Spring Boot, API REST e Spring Security**, mantendo PostgreSQL, JDBC, DAOs, Services, Models e as regras de negócio existentes.

## 2. Preservado da Entrega 1

Modelo de domínio, exceções, interfaces DAO, DAOs JDBC e em memória, **todas as regras dos Services** (candidatura duplicada, vaga encerrada, perfil, propriedade, área em uso, filtros e contagens), console da Entrega 1, scripts SQL e `RegrasDeNegocioTest`.

## 3. Desenvolvido na Entrega 2

- `ConexaoFactory(DataSource)`: os DAOs JDBC passam a usar o `DataSource` do Spring.
- Cadastro com senha por `AutenticacaoService` → `UsuarioService`, com as regras em um único lugar; hash BCrypt guardado apenas no DAO.
- `UserDetailsService` por DAO; contas sem senha não autenticam.
- DTOs de entrada com validação (tamanhos, e-mail, senha forte, `@Positive`) e DTOs de saída para todas as respostas.
- `ApiExceptionHandler` (`@RestControllerAdvice`): 400, 401, 403, 404, 405, 409 e 500 sem expor detalhes internos.
- Endpoints REST de autenticação, vagas, candidaturas, áreas e relatório administrativo, com códigos 201 e 204 onde aplicável.
- `SecurityConfig`: regras por método e perfil, respostas 401/403 em JSON, CSRF por cookie, política de conteúdo (CSP), logout 204 e troca do id da sessão no login.
- Perfil `memoria` (`MemoriaConfig`) e `JdbcConfig` (perfil padrão); `AdminInitializer` para a conta administrativa.
- Interface web (`index.html`, `app.js`, `estilo.css`): cadastro, login, vagas com filtros e detalhes, candidatura, minhas candidaturas, publicar, editar e encerrar vaga, candidatos e logout. Os dados são inseridos como texto, nunca como HTML.
- Testes automatizados, README, diagramas, `.gitignore` e `.env.example` sem credenciais.

## 4. Arquivos

**Criados:** `config/JdbcConfig`, `config/MemoriaConfig`, `service/AutenticacaoService`, `static/app.js`, `static/estilo.css`, `ApiSegurancaTest`, `AutenticacaoServiceTest`, `docs/arquitetura.md`, `docs/RELATORIO-ENTREGA-2.md`, `.gitignore` (raiz), `portal_estagio/.env.example`.

**Alterados:** `ConexaoFactory`, `UsuarioDao`, `UsuarioDaoJdbc`, `UsuarioDaoMemoria`, `UsuarioService`, `Validacao`, `PortalConfig`, `AdminInitializer`, `PortalUserDetailsService`, `SecurityConfig`, `ApiDtos`, os controllers e o `ApiExceptionHandler`, `application.properties`, `static/index.html`, `PortalEstagiosApplicationTests`, `01_schema.sql`, `02_dados.sql`, `.env.example`, `.gitignore`, `README.md` (raiz e do projeto) e `docs/entrega-2-implementacao.md`.

## 5. Testes

| Classe | Cobertura |
|---|---|
| `ApiSegurancaTest` | Cadastro, login, logout, CSRF, rotas privadas e públicas, perfis, propriedade, validações, áreas, relatórios e respostas sem dados sensíveis |
| `AutenticacaoServiceTest` | BCrypt, política de senha e e-mail duplicado |
| `RegrasDeNegocioTest` | Regras de negócio dos Services |
| `ConexaoFactoryTest` | Validação de conexão por `DataSource` simulado |
| `PortalEstagiosApplicationTests` | Inicialização do contexto Spring |

Execução de 15/11/2026 (`mvn clean test`): **32 testes, 0 falhas, 0 erros, 0 ignorados — BUILD SUCCESS**.

Os testes usam o perfil `memoria`, sem necessidade de PostgreSQL. A integração com o Supabase é demonstrada pela aplicação em execução (registros de vagas e candidaturas criados pela interface aparecem nas tabelas).

## 6. Como executar no Windows (IntelliJ ou VS Code)

1. Instale JDK 17 (ou superior), Maven e, se usar banco local, PostgreSQL. Para o Supabase, copie os dados de *Connect > Session pooler*.
2. Execute `sql/01_schema.sql` no banco (SQL Editor do Supabase ou `psql`).
3. Copie `.env.example` para `.env`, dentro de `portal_estagio/`, e preencha os valores.
4. Na pasta `portal_estagio`: `mvn clean test` e depois `mvn spring-boot:run`.
5. **IntelliJ:** abra a pasta `portal_estagio` (pom.xml) e execute `PortalEstagiosApplication`. **VS Code:** extensões Java e Spring Boot Dashboard, ou `mvn spring-boot:run` no terminal.
6. Abra http://localhost:8080.

## 7. Roteiro de verificação funcional

**Navegador** (http://localhost:8080):

| # | Passo | Esperado |
|---|---|---|
| 1 | Cadastrar-se como empresa (senha `Senha1234`) | "Cadastro realizado" e tela de login |
| 2 | Repetir o mesmo e-mail | "Já existe um usuário com o e-mail…" |
| 3 | Senha `12345678` (sem letras) | erro de validação |
| 4 | Entrar como empresa | menu "Minhas vagas / Publicar vaga" |
| 5 | Publicar vaga (as áreas são criadas pelo administrador, veja o roteiro Postman) | vaga em "Minhas vagas" |
| 6 | Editar e encerrar a vaga | vaga atualizada; encerrada sem as ações de edição |
| 7 | Sair; cadastrar e entrar como estudante | menu "Minhas candidaturas" |
| 8 | Filtrar por área e modalidade; Ver detalhes; Candidatar-me | "Candidatura realizada" |
| 9 | Candidatar de novo | "Você já se candidatou…" |
| 10 | Abrir vaga encerrada | "Vaga encerrada", sem botão de candidatura |
| 11 | Minhas candidaturas | lista apenas as do estudante |
| 12 | Empresa: Ver candidatos | lista o estudante |
| 13 | Sair e abrir `/api/candidaturas/minhas` | 401 `{"erro":"Autenticação necessária."}` |

**Postman/Insomnia** (cookie jar ativo; cabeçalho `X-XSRF-TOKEN` conforme o README):

1. `GET /api/auth/csrf`.
2. `POST /api/auth/login` com o administrador (`PORTAL_ADMIN_EMAIL` / `PORTAL_ADMIN_PASSWORD`).
3. `POST /api/admin/areas` `{"nome":"Tecnologia"}` → 201.
4. `GET /api/admin/areas` → 200.
5. `PUT /api/admin/areas/1` → 200.
6. `DELETE /api/admin/areas/1`: 409 com vaga na área; 204 sem vaga.
7. `GET /api/admin/relatorios` → 200.
8. Como estudante, repetir o passo 3 → 403.
9. `POST` sem o cabeçalho CSRF → 403.

## 8. Observações

- As funções administrativas (áreas e relatório) são operadas pela API, com Postman ou Insomnia.
- Os usuários de `02_dados.sql` não têm senha; para autenticar, as contas são cadastradas pela interface ou pela API.
- A interface é uma página única com telas alternadas.
- As credenciais ficam no `.env` local, fora do Git.
