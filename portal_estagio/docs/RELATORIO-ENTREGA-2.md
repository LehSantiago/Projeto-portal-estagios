# Relatório da Entrega 2

## 1. O que foi encontrado no projeto recebido
O projeto **já tinha** uma camada Spring (Controllers, DTOs, Security, configuração), mas **não compilava** e tinha falhas de segurança.

| Problema | Onde | Gravidade |
|---|---|---|
| Não compilava: `\n` literais dentro do código e construtor `ConexaoFactory(DataSource)` inexistente (o campo `dataSource` nunca era atribuído) | `ConexaoFactory.java` | bloqueante |
| `01_schema.sql` com `\n` literal no meio do `CREATE TABLE`: o script falhava | `sql/01_schema.sql` | bloqueante |
| Senha real do banco (Supabase) no `.env.example`, **já commitada e enviada ao GitHub** (commit `9c62f77`) | `.env.example` | **crítica** |
| Cadastro gravava direto no banco pelo `AuthController` (SQL no controller), ignorando as regras do `UsuarioService` | `AuthController` | alta |
| Credencial errada no login caía no handler genérico e devolvia **500** em vez de 401 | `ApiExceptionHandler` | alta |
| `ValidacaoException` devolvia 409 (deveria ser 400); mensagem de `DuplicateKeyException` (detalhe de SQL) era devolvida ao cliente | `ApiExceptionHandler` | alta |
| Interface montava HTML com `innerHTML` e dados do usuário (XSS armazenado) | `index.html` | alta |
| Senha só exigia 8 caracteres; sem limite superior (BCrypt trunca em 72 bytes) | DTOs | média |
| Criação devolvia 200 (não 201); DELETE devolvia 200 (não 204) | Controllers | média |
| Faltava `GET /api/admin/areas` (exigido) | `AreaController` | média |
| Resposta de candidatos usava classe anônima; `Area` era devolvida como entidade | Controllers | média |
| Respostas 401/403 usavam `sendError` (corpo vazio em servidor real) | `SecurityConfig` | média |
| Nenhum teste de API/Security; `src/teste/` duplicada; `target/` no pacote | testes | média |
| A documentação afirmava coisas que o código não fazia (ex.: "ConexaoFactory aceita DataSource") | docs | baixa |

## 2. O que já estava implementado e foi preservado
Modelo de domínio, exceções, interfaces DAO, DAOs JDBC (Área, Vaga, Candidatura) e em memória, **todas as regras dos Services**
(candidatura duplicada, vaga encerrada, perfil, propriedade, área em uso, filtros, contagens), console da Entrega 1, scripts SQL,
`RegrasDeNegocioTest` (10 testes), BCrypt/`SecurityConfig` como ponto de partida, `AdminInitializer`.

## 3. O que foi desenvolvido
- Correção da compilação e do SQL; `ConexaoFactory(DataSource)`.
- Cadastro com senha passando por `AutenticacaoService` → `UsuarioService` (regras únicas), hash BCrypt guardado só no DAO.
- `UserDetailsService` via DAO (conta sem senha = não entra).
- DTOs de entrada com validação (tamanhos, e-mail, senha forte, `@Positive`) e DTOs de saída para tudo.
- `ApiExceptionHandler` completo: 400/401/403/404/405/409/500 sem vazar detalhes.
- Códigos 201/204, `GET /api/admin/areas`, `GET /api/auth/me`, `GET /api/auth/csrf` (JSON claro).
- `SecurityConfig`: regras explícitas por método, 401/403 em JSON, CSRF por cookie, CSP, logout 204; troca do id da sessão no login.
- Perfil `memoria` (`MemoriaConfig`) e `JdbcConfig` (perfil padrão).
- Interface nova (`index.html`, `app.js`, `estilo.css`): cadastro, login, vagas com filtros e detalhes, candidatura, minhas
  candidaturas, publicar/editar/encerrar vaga, candidatos, logout; sem `innerHTML`.
- Testes novos, README, diagramas, `.gitignore`, `.env.example` sem segredos.

## 4. Arquivos
**Criados:** `config/JdbcConfig`, `config/MemoriaConfig`, `service/AutenticacaoService`, `static/app.js`, `static/estilo.css`,
`test/.../ApiSegurancaTest`, `test/.../AutenticacaoServiceTest`, `docs/arquitetura.md`, `docs/RELATORIO-ENTREGA-2.md`,
`.gitignore` (raiz), `portal_estagio/.env.example`.

**Alterados:** `ConexaoFactory`, `UsuarioDao`, `UsuarioDaoJdbc`, `UsuarioDaoMemoria`, `UsuarioService`, `Validacao`, `PortalConfig`,
`AdminInitializer`, `PortalUserDetailsService`, `SecurityConfig`, `ApiDtos`, os 6 controllers + `ApiExceptionHandler`,
`application.properties`, `static/index.html`, `PortalEstagiosApplicationTests`, `01_schema.sql`, `02_dados.sql`, `.env.example`,
`.gitignore`, `README.md` (raiz e do projeto), `docs/entrega-2-implementacao.md`.

**Removidos do pacote:** `target/`, `src/teste/` (cópia duplicada do teste), `.git/`.

## 5. Testes — o que foi e o que NÃO foi executado
**Nada foi compilado nem executado.** O ambiente em que o código foi escrito não tinha JDK (só JRE), Maven nem acesso à rede.
Foi feita apenas uma verificação estática (chaves balanceadas, imports, ausência de `\n` literais). Portanto **não afirmo que o
projeto compila ou que os testes passam**. O primeiro passo ao receber o ZIP é rodar `mvn clean test`; erros de compilação
ou de teste, se houver, devem ser corrigidos a partir da mensagem do Maven.
Também **não foi testado contra um PostgreSQL real** (DAOs JDBC e `01_schema.sql` corrigido).

Testes escritos (a executar): `ApiSegurancaTest` (18 cenários), `AutenticacaoServiceTest` (3), `RegrasDeNegocioTest` (10, já existiam),
`PortalEstagiosApplicationTests` (1).

## 6. Como executar no Windows (IntelliJ ou VS Code)
1. Instale JDK 17, Maven e PostgreSQL. Crie o banco: `CREATE DATABASE portal_estagios;`
2. Na pasta `portal_estagio`: `psql -U postgres -d portal_estagios -f sql\01_schema.sql`
3. Copie `.env.example` para `.env` e preencha (senha do **seu** PostgreSQL; e-mail/senha do admin de demonstração).
4. Rode `mvn clean test`. Se compilar e passar, siga; senão, corrija pelo erro mostrado.
5. **IntelliJ:** abra a pasta `portal_estagio` (pom.xml) → rode `PortalEstagiosApplication`. **VS Code:** extensões Java + Spring Boot
   Dashboard → *Run*; ou `mvn spring-boot:run` no terminal.
6. Abra http://localhost:8080.

## 7. Roteiro de testes manuais
**Navegador** (http://localhost:8080):
| # | Passo | Esperado |
|---|---|---|
| 1 | Cadastrar-se → empresa (senha `Senha1234`) | "Cadastro realizado" e tela de login |
| 2 | Repetir o mesmo e-mail | erro "Já existe um usuário com o e-mail…" |
| 3 | Senha `12345678` (sem letras) | erro de validação |
| 4 | Entrar como empresa | menu "Minhas vagas / Publicar vaga" |
| 5 | Publicar vaga (área exige que o admin tenha criado áreas, veja Postman abaixo) | vaga em "Minhas vagas" |
| 6 | Editar e Encerrar a vaga | atualiza; encerrada some das edições |
| 7 | Sair; cadastrar e entrar como estudante | menu "Minhas candidaturas" |
| 8 | Filtrar por área/modalidade; Ver detalhes; Candidatar-me | "Candidatura realizada" |
| 9 | Candidatar de novo | erro "Você já se candidatou…" |
| 10 | Candidatar em vaga encerrada | botão não aparece ("Vaga encerrada") |
| 11 | "Minhas candidaturas" | lista só as suas |
| 12 | Empresa → "Ver candidatos" | lista o estudante |
| 13 | Sair e tentar abrir `/api/candidaturas/minhas` no navegador | 401 `{"erro":"Autenticação necessária."}` |

**Postman/Insomnia** (guarde cookies; envie `X-XSRF-TOKEN` conforme o README):
1. `GET /api/auth/csrf`. 2. `POST /api/auth/login` com o administrador (`PORTAL_ADMIN_EMAIL/PASSWORD`).
3. `POST /api/admin/areas` `{"nome":"Tecnologia"}` → 201. 4. `GET /api/admin/areas` → 200.
5. `PUT /api/admin/areas/1` → 200. 6. `DELETE /api/admin/areas/1` com vaga nessa área → 409; sem vaga → 204.
7. `GET /api/admin/relatorios` → 200. 8. Como estudante, repetir o 3 → 403. 9. `POST` sem o cabeçalho CSRF → 403.

## 8. Pendências / pontos de atenção
1. **Rotacione a senha do banco Supabase agora.** Ela está no histórico do Git (commit `9c62f77`, já no GitHub). Apagar o arquivo não
   basta; se quiser limpar o histórico, use `git filter-repo`, mas a troca da senha é o que realmente resolve.
2. Compilar e rodar os testes (item 5): nada foi executado.
3. Testar os DAOs JDBC e o `01_schema.sql` em PostgreSQL real.
4. Os usuários de `02_dados.sql` não têm senha; a demonstração usa contas cadastradas pela interface.
5. Rate limiting/bloqueio por tentativas de login não foi implementado (fora do enunciado).
6. A interface é uma página única com telas alternadas (não páginas HTML separadas).
