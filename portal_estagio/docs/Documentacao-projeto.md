# Portal de Estágios — Entrega 2

Aplicação web acadêmica para conectar estudantes a oportunidades de estágio, com três perfis: `ESTUDANTE`, `EMPRESA` e `ADMINISTRADOR`.

A Entrega 2 evolui a base da Entrega 1 para **Spring Boot + API REST + Spring Security**, preservando Java, PostgreSQL, JDBC, DAOs, Services, Models e regras de negócio existentes. A aplicação utiliza **sessão HTTP e proteção CSRF**; JWT não é utilizado.

> **Pasta correta para executar:** `portal_estagio/` — é nela que ficam `pom.xml`, `src/`, `sql/` e o `.env` local. Todos os comandos Maven deste documento devem ser executados nessa pasta.

## 1. Tecnologias

- Java 17.
- Maven.
- Spring Boot 3.5.7: Spring Web, Spring Security, Spring JDBC e Bean Validation.
- PostgreSQL. O ambiente de desenvolvimento da equipe utiliza Supabase (PostgreSQL hospedado) com Session Pooler.
- JDBC e padrão DAO; não foi feita migração para JPA.
- HTML, CSS e JavaScript sem framework, servidos pelo próprio Spring Boot.
- JUnit 5, Spring Boot Test, MockMvc e Spring Security Test.

## 2. O que foi reaproveitado e o que mudou desde a Entrega 1

| Reaproveitado da Entrega 1 | Evolução/adição na Entrega 2 |
|---|---|
| Models (`Usuario`, `Area`, `Vaga`, `Candidatura`) e enums de domínio. | Aplicação inicializada pelo Spring Boot e organizada por configuração/injeção de dependências. |
| Interfaces DAO e implementação JDBC com PostgreSQL. | `DataSource` gerenciado pelo Spring e DAOs JDBC conectados por construtor. |
| Services e regras de negócio. | Controllers REST que delegam as regras aos Services, sem duplicá-las. |
| Exceções personalizadas e validações de domínio. | DTOs de entrada/saída, Bean Validation e `@RestControllerAdvice`. |
| Esquema relacional e persistência PostgreSQL. | Coluna de hash de senha, autenticação por sessão, CSRF e autorização Spring Security. |
| Separação Model / DAO / Service. | Interface web HTML/CSS/JavaScript e endpoints REST. |

A decisão arquitetural foi **manter JDBC/DAO**. Isso evita uma migração desnecessária para JPA e preserva a implementação e as regras já desenvolvidas. O perfil `memoria` é usado em testes; o perfil padrão usa PostgreSQL.

## 3. Estrutura principal

```text
portal_estagio/
├── pom.xml
├── .env.example          # modelo sem credenciais reais
├── README.md
├── Registro_IA.md
├── Modelo_de_dominio.md
├── sql/
│   ├── 01_schema.sql
│   ├── 02_dados.sql
│   └── 03_migracao_entrega2.sql
├── docs/
│   ├── arquitetura.md
│   ├── camadas.md
│   ├── modelo-dominio.md
│   ├── esquema-tabelas.md
│   ├── entrega-2-implementacao.md
│   ├── roteiro-verificacao.md
│   └── RELATORIO-ENTREGA-2.md
└── src/
    ├── main/java/br/portal/
    │   ├── config/       # beans JDBC/memória e admin inicial
    │   ├── controller/  # API REST e tratamento global de erros
    │   ├── dao/         # interfaces e implementações JDBC/memória
    │   ├── dto/         # requisições e respostas
    │   ├── excecao/     # exceções da aplicação
    │   ├── model/       # entidades e enums do domínio
    │   ├── security/    # configuração de segurança e UserDetailsService
    │   └── service/     # regras de negócio
    ├── main/resources/
    │   ├── application.properties
    │   └── static/      # index.html, app.js e estilo.css
    └── test/java/       # testes automatizados
```

## 4. Pré-requisitos

- JDK **17** (ou superior) instalado e selecionado no `JAVA_HOME`/`PATH`. O projeto é compilado com `--release 17`.
- Maven 3.6.3 ou superior.
- Acesso à internet para baixar dependências Maven na primeira execução.
- Projeto PostgreSQL acessível. No ambiente da equipe, o banco é hospedado no Supabase.
- Navegador. VS Code ou IntelliJ IDEA são recomendados para edição e demonstração.

Confira as versões no terminal:

```powershell
java -version
mvn -version
```

Na saída de `mvn -version`, confirme a versão do Java usada pelo Maven.

## 5. Banco PostgreSQL no Supabase

### 5.1 Obter os dados de conexão

No painel do Supabase, abra o projeto e selecione **Connect → Session Pooler**. Copie os campos exibidos para essa opção: host, porta, database, usuário e senha. Não use a chave `anon` nem a `service_role`: elas não são credenciais JDBC do PostgreSQL.

A URL usada pelo JDBC tem este formato (substitua os valores pelos exibidos no seu projeto):

```text
jdbc:postgresql://SEU_HOST_DO_POOLER:5432/postgres?sslmode=require
```

A porta `5432`, o database `postgres` e o usuário devem ser confirmados na tela de conexão do projeto. O usuário do Session Pooler pode incluir o identificador do projeto. Copie-o integralmente; não o monte manualmente.

### 5.2 Criar ou atualizar as tabelas

Os scripts ficam em `sql/` e devem ser executados no **SQL Editor** do Supabase, conectado ao database do projeto. Abra o arquivo local, copie o conteúdo e execute no SQL Editor.

Escolha somente um dos caminhos:

**Projeto Supabase novo / banco sem tabelas do Portal**

1. Execute `sql/01_schema.sql` para criar as tabelas.
2. Opcionalmente, execute `sql/02_dados.sql` para inserir áreas, vagas e usuários fictícios de demonstração.

**Banco da Entrega 1 com dados que precisam ser preservados**

1. Faça backup antes de qualquer alteração.
2. Execute uma única vez `sql/03_migracao_entrega2.sql`, que adiciona a coluna `senha` à tabela `usuario`.
3. Não execute `01_schema.sql` nesse banco existente: ele remove e recria as tabelas e apaga dados.

`02_dados.sql` insere usuários fictícios sem senha (`senha = NULL`). Eles não conseguem fazer login. Para testar autenticação, cadastre estudante e empresa pela interface ou API.

Para conferir se as tabelas existem, execute no SQL Editor:

```sql
SELECT table_name
FROM information_schema.tables
WHERE table_schema = 'public'
  AND table_type = 'BASE TABLE'
ORDER BY table_name;
```

O Portal utiliza as tabelas `usuario`, `area`, `vaga` e `candidatura`.

## 6. Configurar as variáveis de ambiente

### 6.1 Localização obrigatória do `.env`

O arquivo `.env` precisa ficar **dentro de `portal_estagio/`, ao lado do `pom.xml`**, porque o Spring importa `optional:file:.env[.properties]` a partir da pasta de execução. Não basta deixar o arquivo somente na pasta externa `Projeto-portal-estagios/`.

No Git Bash, a partir da pasta raiz do repositório:

```bash
cp portal_estagio/.env.example portal_estagio/.env
cd portal_estagio
notepad .env
```

Se o `.env` já estiver na pasta externa e ainda não existir na pasta interna, copie-o com:

```bash
cp .env portal_estagio/.env
cd portal_estagio
```

Depois de entrar em `portal_estagio`, confira:

```bash
ls -la .env
```

### 6.2 Exemplo para Supabase

Edite o `.env` e substitua os valores de exemplo pelos dados reais de **Connect → Session Pooler**. Não copie credenciais para este README:

```properties
# PostgreSQL remoto - Supabase Session Pooler
PORTAL_DB_URL=jdbc:postgresql://SEU_HOST_DO_POOLER:5432/postgres?sslmode=require
PORTAL_DB_USER=SEU_USUARIO_DO_POOLER
PORTAL_DB_PASSWORD=SUA_SENHA_DO_BANCO

# Conta administrativa de demonstração (opcional)
PORTAL_ADMIN_EMAIL=admin@portal.local
PORTAL_ADMIN_PASSWORD=DefinaUmaSenhaForte123
```

Para PostgreSQL instalado localmente, o formato de URL padrão é `jdbc:postgresql://localhost:5432/portal_estagios`, usuário `postgres` e a senha definida na instalação.

### 6.3 Variáveis disponíveis

| Variável | Finalidade |
|---|---|
| `PORTAL_DB_URL` | URL JDBC do PostgreSQL. |
| `PORTAL_DB_USER` | Usuário PostgreSQL. Para Supabase, use o usuário exibido em Session Pooler. |
| `PORTAL_DB_PASSWORD` | Senha do banco PostgreSQL. |
| `PORTAL_ADMIN_EMAIL` | E-mail da conta administrativa de demonstração (opcional). |
| `PORTAL_ADMIN_PASSWORD` | Senha da conta administrativa (opcional; use senha forte). |

`application.properties` importa o `.env` e mapeia as variáveis para o `DataSource` do Spring. `ConexaoFactory` recebe esse `DataSource`; na aplicação web, não é necessário inserir URL/senha em classes Java.

O `.env` está listado no `.gitignore` e não deve ser versionado nem enviado. Somente o `.env.example`, sem credenciais reais, faz parte do repositório.

## 7. Executar no Windows (VS Code, IntelliJ ou Git Bash)

Abra o terminal na raiz do repositório e entre na pasta que contém o `pom.xml`:

```bash
cd portal_estagio
```

Confirme a pasta e os arquivos:

```bash
ls
# esperado: pom.xml, src, sql, README.md, .env.example
ls -la .env
```

Execute os testes:

```bash
mvn clean test
```

Em seguida, inicie a aplicação:

```bash
mvn spring-boot:run
```

Mantenha esse terminal aberto enquanto estiver usando o portal. Acesse no navegador:

```text
http://localhost:8080
```

Para encerrar, volte ao terminal do servidor e pressione `Ctrl + C`.

Os comandos Maven devem ser executados em `portal_estagio/`, a pasta que contém o `pom.xml`.

### Verificar a rede para o Supabase

No PowerShell, use o host real do Session Pooler:

```powershell
Test-NetConnection SEU_HOST_DO_POOLER -Port 5432
```

`TcpTestSucceeded : True` confirma apenas que a porta está acessível. A validação de usuário e senha ocorre quando o Spring Boot inicia. No perfil padrão, a aplicação valida a conexão PostgreSQL na inicialização; se o banco não estiver acessível, o início falha.

### Perfil de memória (somente testes/demonstração)

```powershell
mvn spring-boot:run "-Dspring-boot.run.profiles=memoria"
```

Esse perfil usa DAOs em memória; os dados desaparecem ao encerrar. Ele não representa a integração com o PostgreSQL nem a persistência.

## 8. Interface web

A interface está em `src/main/resources/static/` e é servida pela mesma origem da API (`http://localhost:8080`). Por isso, o navegador usa a sessão e os cookies do próprio servidor; não há necessidade de configurar CORS para o fluxo padrão.

- **Visitante:** consulta vagas abertas, aplica filtros por área/modalidade/palavra-chave, vê detalhes e acessa cadastro/login.
- **Estudante:** consulta vagas, candidata-se, consulta as próprias candidaturas e encerra a sessão.
- **Empresa:** publica vagas, consulta as próprias vagas, edita, encerra e consulta candidatos das próprias vagas.
- **Administrador:** administra áreas e consulta relatórios por Postman/Insomnia. Uma tela administrativa não é exigida pelo enunciado.

## 9. API REST — endpoints e respostas

A documentação completa dos endpoints está nesta tabela. Os exemplos abaixo usam `http://localhost:8080` como base URL.

| Método | Endpoint | Acesso | Sucesso | Erros comuns |
|---|---|---|---:|---|
| `GET` | `/api/auth/csrf` | Público | 200 | — |
| `POST` | `/api/auth/cadastro/estudante` | Público | 201 | 400, 409 |
| `POST` | `/api/auth/cadastro/empresa` | Público | 201 | 400, 409 |
| `POST` | `/api/auth/login` | Público | 200 | 400, 401, 403 (CSRF) |
| `POST` | `/api/auth/logout` | Autenticado | 204 | 401, 403 (CSRF) |
| `GET` | `/api/auth/me` | Autenticado | 200 | 401 |
| `GET` | `/api/vagas?areaId=&modalidade=&q=` | Público | 200 | 400 |
| `GET` | `/api/vagas/{id}` | Público | 200 | 400, 404 |
| `POST` | `/api/vagas` | `EMPRESA` | 201 | 400, 401, 403 |
| `GET` | `/api/vagas/minhas` | `EMPRESA` | 200 | 401, 403 |
| `PUT` | `/api/vagas/{id}` | Empresa proprietária | 200 | 400, 403, 404, 409 |
| `PATCH` | `/api/vagas/{id}/encerrar` | Empresa proprietária | 200 | 403, 404, 409 |
| `POST` | `/api/vagas/{id}/candidaturas` | `ESTUDANTE` | 201 | 400, 401, 403, 404, 409 |
| `GET` | `/api/candidaturas/minhas` | `ESTUDANTE` | 200 | 401, 403 |
| `GET` | `/api/vagas/{id}/candidatos` | Empresa proprietária | 200 | 401, 403, 404 |
| `GET` | `/api/areas` | Público | 200 | — |
| `GET` | `/api/admin/areas` | `ADMINISTRADOR` | 200 | 401, 403 |
| `POST` | `/api/admin/areas` | `ADMINISTRADOR` | 201 | 400, 403, 409 |
| `PUT` | `/api/admin/areas/{id}` | `ADMINISTRADOR` | 200 | 400, 403, 404, 409 |
| `DELETE` | `/api/admin/areas/{id}` | `ADMINISTRADOR` | 204 | 403, 404, 409 |
| `GET` | `/api/admin/relatorios` | `ADMINISTRADOR` | 200 | 401, 403 |

### Exemplos de requisição e resposta

**Cadastro de estudante** — `POST /api/auth/cadastro/estudante` (com CSRF):

```json
{
  "nome": "Ana Souza",
  "email": "ana.exemplo@aluno.com",
  "curso": "Ciência da Computação",
  "senha": "SenhaForte123"
}
```

Resposta esperada: `201 Created`:

```json
{"mensagem":"Estudante cadastrado."}
```

**Login** — `POST /api/auth/login` (com CSRF):

```json
{"email":"ana.exemplo@aluno.com","senha":"SenhaForte123"}
```

Resposta esperada: `200 OK` com dados públicos do usuário (sem senha ou hash):

```json
{"nome":"Ana Souza","email":"ana.exemplo@aluno.com","perfil":"ESTUDANTE"}
```

**Publicar vaga** — `POST /api/vagas` como empresa autenticada (com CSRF):

```json
{
  "titulo": "Estágio em Java",
  "descricao": "Apoio ao desenvolvimento de APIs",
  "areaId": 1,
  "localizacao": "São Paulo",
  "modalidade": "HIBRIDA",
  "cargaHorariaSemanal": 30
}
```

Resposta esperada: `201 Created`, contendo os dados públicos da vaga.

**Candidatura duplicada:** `409 Conflict`, com mensagem de regra de negócio. **Sem autenticação:** `401 Unauthorized`. **Perfil ou propriedade incorretos:** `403 Forbidden`. **Identificador inexistente:** `404 Not Found`. **Entrada inválida:** `400 Bad Request`.

Todas as falhas são tratadas por `ApiExceptionHandler` (`@RestControllerAdvice`). As respostas seguem formato JSON, por exemplo `{"erro":"Dados inválidos.","detalhes":[...]}`. Não são enviados stack traces, SQL, senhas ou hashes ao cliente.

## 10. Autenticação por sessão e CSRF

O login cria uma **sessão HTTP** e o navegador mantém o cookie `JSESSIONID`. JWT não é usado. A proteção CSRF permanece ativa: operações de escrita (`POST`, `PUT`, `PATCH`, `DELETE`) exigem o token no cabeçalho `X-XSRF-TOKEN`.

1. Faça `GET /api/auth/csrf`.
2. O servidor devolve o token no corpo e grava o cookie `XSRF-TOKEN`.
3. Envie o valor do campo `token` no cabeçalho `X-XSRF-TOKEN` e mantenha os cookies da sessão.
4. Depois do login ou logout, busque um token novo com `GET /api/auth/csrf` antes de outra operação de escrita.

**Postman/Insomnia:** habilite o cookie jar, faça primeiro o GET de CSRF e use o valor do token no header `X-XSRF-TOKEN` em todas as operações de escrita. Depois do login, faça novamente o GET de CSRF. Mantenha o cookie `JSESSIONID` entre as requisições.

**Exemplo com curl no PowerShell** (use `curl.exe`; os valores de token são exemplos e devem ser substituídos pelos recebidos):

```powershell
curl.exe -c cookies.txt http://localhost:8080/api/auth/csrf

curl.exe -b cookies.txt -c cookies.txt -X POST http://localhost:8080/api/auth/cadastro/estudante `
  -H "X-XSRF-TOKEN: TOKEN_RECEBIDO" -H "Content-Type: application/json" `
  -d '{"nome":"Ana","email":"ana.exemplo@aluno.com","curso":"Computação","senha":"Senha1234"}'

curl.exe -b cookies.txt -c cookies.txt -X POST http://localhost:8080/api/auth/login `
  -H "X-XSRF-TOKEN: TOKEN_ATUAL" -H "Content-Type: application/json" `
  -d '{"email":"ana.exemplo@aluno.com","senha":"Senha1234"}'

# Após o login, obtenha um novo token CSRF antes da próxima escrita.
curl.exe -b cookies.txt -c cookies.txt http://localhost:8080/api/auth/csrf
curl.exe -b cookies.txt http://localhost:8080/api/candidaturas/minhas
```

A identidade do usuário é obtida de `Authentication`/Spring Security. O frontend não escolhe o proprietário enviando `usuarioId`, `empresaId` ou `estudanteId` como identidade da operação.

## 11. Segurança e autorização

- Senhas são armazenadas com `PasswordEncoder` BCrypt; nunca em texto puro.
- `UserDetailsService` carrega o usuário para o Spring Security.
- Sessão HTTP e CSRF por cookie/cabeçalho estão habilitados.
- Rotas são protegidas por perfil no `SecurityConfig`.
- Regras de propriedade são verificadas nos Services: empresa só altera/encerra vagas próprias e consulta candidatos das próprias vagas; estudante consulta as próprias candidaturas.
- Cadastro público cria apenas estudante ou empresa; não há endpoint de cadastro de administrador nem campo público para elevar perfil.
- DTOs separam entrada/saída do modelo e evitam expor senha/hash.
- Erros internos e de persistência não revelam stack trace nem detalhes SQL ao cliente.
- A interface e a API são servidas pela mesma origem; CORS não foi habilitado porque não é necessário nesse arranjo.

## 12. Administrador de demonstração

A conta administrativa é criada por `AdminInitializer` **na inicialização controlada**, somente quando `PORTAL_ADMIN_EMAIL` e `PORTAL_ADMIN_PASSWORD` estão definidos e o e-mail ainda não existe. A senha é armazenada com BCrypt. Cadastro público de administrador não existe.

Configure as duas variáveis no `.env` ou ambiente do processo antes de iniciar. Se o e-mail já existir, o inicializador não recria nem altera a senha. A conta é apenas para demonstração: use senha forte e não inclua credenciais no repositório, no ZIP ou no README. Autentique via `POST /api/auth/login` e demonstre os endpoints `/api/admin/areas` e `/api/admin/relatorios` pelo Postman/Insomnia.

## 13. Testes automatizados

Execute na pasta `portal_estagio`:

```powershell
mvn clean test
```

| Classe | Cobertura principal |
|---|---|
| `ApiSegurancaTest` | API, sessão, CSRF, perfis, propriedade, validações, códigos HTTP e dados sensíveis. |
| `AutenticacaoServiceTest` | Política de senha, BCrypt e e-mail duplicado. |
| `RegrasDeNegocioTest` | Candidatura duplicada/encerrada, propriedade, filtros, áreas e relatórios. |
| `ConexaoFactoryTest` | Validação de conexão por `DataSource` simulado. |
| `PortalEstagiosApplicationTests` | Inicialização do contexto Spring no perfil de teste. |

Resultado da execução de 15/11/2026: `Tests run: 32, Failures: 0, Errors: 0, Skipped: 0` — `BUILD SUCCESS`.

Os testes de API e de regras usam o perfil `memoria` ou `DataSource` simulado. A integração dos DAOs JDBC com o Supabase é exercitada pela aplicação em execução.

## 14. Verificação funcional

O arquivo `docs/roteiro-verificacao.md` contém o roteiro detalhado. A tabela resume os cenários da Entrega 2.

| Cenário | Resultado esperado | Como verificar |
|---|---|---|
| Cadastro válido de estudante e empresa | 201 | Tela de cadastro ou resposta da API. |
| E-mail repetido / login com senha incorreta | 409 / 401 | Respostas da API. |
| Login e logout | 200 / 204 | Sessão criada e invalidada. |
| Consulta de vagas e filtros | 200 | Tela do navegador. |
| Publicar e editar vaga | 201 / 200; 400 para entrada inválida | Tela ou Postman. |
| Carga horária zero ou campos obrigatórios ausentes | 400 | Resposta JSON de validação. |
| Candidatura válida / duplicada / vaga encerrada | 201 / 409 / 409 | Tela e respostas da API. |
| Estudante consulta candidaturas próprias | 200, sem dados de outro estudante | Resposta da API. |
| Empresa consulta/edita vaga própria | 200 | Tela ou Postman. |
| Empresa acessa vaga de outra empresa | 403 | Resposta da API com duas empresas. |
| Estudante publica vaga; empresa se candidata | 403 | Respostas da API. |
| Exclusão de área em uso | 409 | Resposta da API. |
| Usuário sem login em endpoint privado | 401 | Resposta da API. |
| Cadastro público tenta criar administrador | Não existe rota nem campo para isso | `ApiSegurancaTest`; DTOs de cadastro. |
| Respostas públicas de usuário | Sem senha nem hash | JSON de cadastro e login. |
| Persistência após reiniciar o servidor | Vaga e usuário continuam no PostgreSQL | Consulta no Supabase antes e depois. |
| Operações administrativas | Acesso somente ao administrador | Coleção Postman/Insomnia. |
| CSRF ausente ou incorreto | Escrita bloqueada (403) | Resposta da API. |

## 15. Diagramas e documentação complementar

- [`docs/arquitetura.md`](docs/arquitetura.md): arquitetura, fluxo de candidatura e responsabilidades.
- [`docs/camadas.md`](docs/camadas.md): responsabilidades das camadas.
- [`docs/modelo-dominio.md`](docs/modelo-dominio.md): diagrama de domínio.
- [`docs/esquema-tabelas.md`](docs/esquema-tabelas.md): esquema relacional.
- [`docs/entrega-2-implementacao.md`](docs/entrega-2-implementacao.md): decisões e implementação da Entrega 2.
- [`docs/roteiro-verificacao.md`](docs/roteiro-verificacao.md): roteiro de verificação.
- [`docs/RELATORIO-ENTREGA-2.md`](docs/RELATORIO-ENTREGA-2.md): relatório técnico.
- [`Registro_IA.md`](Registro_IA.md): registro de interações com IA.

## 16. Empacotamento e versionamento

**O pacote contém:** código-fonte e `pom.xml`; `.env.example` sem credenciais; scripts SQL; interface estática; testes automatizados; README, diagramas e documentação.

**Fora do pacote e do repositório:** `.env`, credenciais, tokens, `.git`, `target/`, arquivos `.class` e dados pessoais reais.

```bash
git status --short
git add README.md portal_estagio/README.md portal_estagio/Registro_IA.md portal_estagio/docs
git commit -m "docs: documentacao da entrega 2"
git tag -a entrega-2 -m "Portal de Estagios - Entrega 2"
git push origin main
git push origin entrega-2
```

## 17. Escopo implementado

API REST com DTOs e validação, tratamento global de erros, sessão HTTP com CSRF, BCrypt, autorização por perfil e por propriedade, interface web para visitante, estudante e empresa, persistência JDBC/PostgreSQL, conta administrativa criada por inicialização controlada, testes automatizados e diagramas.
