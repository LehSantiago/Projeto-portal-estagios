# Portal de Estágios — Entrega 2

Aplicação web acadêmica para conectar estudantes a oportunidades de estágio, com três perfis: `ESTUDANTE`, `EMPRESA` e `ADMINISTRADOR`.

A Entrega 2 evolui a base da Entrega 1 para **Spring Boot + API REST + Spring Security**, preservando Java, PostgreSQL, JDBC, DAOs, Services, Models e regras de negócio existentes. A aplicação utiliza **sessão HTTP e proteção CSRF**; JWT não foi adotado porque a sessão é a abordagem de referência do enunciado.

> **Pasta correta para executar:** `portal_estagio/` — é nela que ficam `pom.xml`, `src/`, `sql/` e o `.env` local. Todos os comandos Maven deste documento devem ser executados nessa pasta.

## 1. Tecnologias

- Java 17 (versão exigida para a entrega).
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
│   ├── checklist-entrega-2.md
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

- JDK **17** instalado e selecionado no `JAVA_HOME`/`PATH`.
- Maven 3.6.3 ou superior.
- Acesso à internet para baixar dependências Maven na primeira execução.
- Projeto PostgreSQL acessível. No ambiente da equipe, o banco é hospedado no Supabase.
- Navegador. VS Code ou IntelliJ IDEA são recomendados para edição e demonstração.

Confira as versões no terminal:

```powershell
java -version
mvn -version
```

Na saída de `mvn -version`, confirme que o Maven está usando Java 17. Um log anterior da equipe mostrou Java 25.0.5, embora o Maven compilasse com `--release 17`; para a avaliação, faça uma execução final usando JDK 17.

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

O `.env` está listado no `.gitignore` e **não pode ser enviado ao GitHub nem ao Moodle**. Inclua apenas `.env.example`, sem credenciais reais. Se uma senha real foi compartilhada em conversa ou publicada em commit, redefina-a no Supabase/provedor e atualize seu `.env`.

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

Se a suíte terminar com `BUILD SUCCESS`, inicie a aplicação:

```bash
mvn spring-boot:run
```

Mantenha esse terminal aberto enquanto estiver usando o portal. Acesse no navegador:

```text
http://localhost:8080
```

Para encerrar, volte ao terminal do servidor e pressione `Ctrl + C`.

**Importante:** executar Maven na pasta externa `Projeto-portal-estagios/` resulta em “there is no POM in this directory” ou falha ao localizar o plugin `spring-boot`. Execute os comandos sempre em `portal_estagio/`.

### Verificar a rede para o Supabase

No PowerShell, use o host real do Session Pooler:

```powershell
Test-NetConnection SEU_HOST_DO_POOLER -Port 5432
```

`TcpTestSucceeded : True` confirma apenas que a porta está acessível. A validação completa depende de iniciar o Spring Boot com usuário/senha corretos e conseguir realizar uma consulta. No perfil padrão, a aplicação valida a conexão PostgreSQL na inicialização; se o banco não estiver acessível, o início deve falhar em vez de aparentar estar pronto sem persistência.

### Perfil de memória (somente testes/demonstração)

```powershell
mvn spring-boot:run "-Dspring-boot.run.profiles=memoria"
```

Esse perfil usa DAOs em memória; os dados desaparecem ao encerrar. Não use esse perfil para comprovar integração PostgreSQL ou persistência.

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

Configure as duas variáveis no `.env` ou ambiente do processo antes de iniciar. Se o e-mail já existir, o inicializador não recria nem altera a senha. Para a apresentação, use uma conta de demonstração com senha forte e não inclua credenciais no repositório, ZIP ou README. Autentique via `POST /api/auth/login` e demonstre os endpoints `/api/admin/areas` e `/api/admin/relatorios` pelo Postman/Insomnia.

## 13. Testes automatizados e evidência

Execute na pasta `portal_estagio`:

```powershell
mvn clean test
```

A versão atual do código contém os seguintes métodos de teste:

| Classe | Quantidade de métodos `@Test` | Cobertura principal |
|---|---:|---|
| `ApiSegurancaTest` | 20 | API, sessão, CSRF, perfis, propriedade, validações, códigos HTTP e dados sensíveis. |
| `AutenticacaoServiceTest` | 3 | Política de senha, BCrypt e e-mail duplicado. |
| `RegrasDeNegocioTest` | 10 | Candidatura duplicada/encerrada, propriedade, filtros, áreas e relatórios. |
| `ConexaoFactoryTest` | 2 | Validação de conexão por `DataSource` simulado. |
| `PortalEstagiosApplicationTests` | 1 | Inicialização do contexto Spring no perfil de teste. |
| **Total declarado nos fontes atuais** | **36** | — |

**Atenção à evidência de execução:** o log local compartilhado pela equipe em 15/11/2026 registrou `32 tests run, 0 failures, 0 errors, 0 skipped` e `BUILD SUCCESS`. Naquele log, o Maven mostrou 18 testes em `ApiSegurancaTest`, além de 3 de autenticação, 10 de regras e 1 de contexto. O log não mostrou a classe `ConexaoFactoryTest`. Portanto, não se deve afirmar que os 36 métodos dos fontes atuais foram executados naquela execução. Repita `mvn clean test` na versão final e guarde a saída completa/Surefire reports para registrar o número efetivamente executado.

O log de 15/11 também indicou runtime Java 25.0.5, embora o compilador Maven usasse `--release 17`. Faça a verificação final com JDK 17 selecionado.

Os testes automatizados usam perfil `memoria`/dependências simuladas quando indicado e não substituem os testes manuais dos DAOs JDBC contra o Supabase real.

## 14. Roteiro de teste manual para a avaliação

Registre para cada cenário: dados de entrada, resultado esperado, resultado observado, data e evidência (captura de tela ou resposta do Postman). O arquivo `docs/roteiro-verificacao.md` contém o roteiro detalhado.

| Cenário | Resultado esperado | Evidência recomendada |
|---|---|---|
| Cadastro válido de estudante e empresa | 201 | Tela ou resposta da API. |
| E-mail repetido / login com senha incorreta | 409 / 401 | Respostas da API. |
| Login e logout | 200 / 204 | Sessão criada e invalidada. |
| Consulta de vagas e filtros | 200 | Tela do navegador. |
| Publicar e editar vaga com dados inválidos/válidos | 201 / 200 e 400 para entrada inválida | Tela ou Postman. |
| Carga horária zero ou campos obrigatórios ausentes | 400 | Resposta JSON de validação. |
| Candidatura válida / duplicada / vaga encerrada | 201 / 409 / 409 | Tela e respostas da API. |
| Estudante consulta candidaturas próprias | 200, sem dados de outro estudante | Resposta da API. |
| Empresa consulta/edita vaga própria | 200 | Tela ou Postman. |
| Empresa tenta acessar vaga de outra empresa | 403 | Resposta da API com duas empresas de teste. |
| Estudante tenta publicar vaga; empresa tenta candidatar-se | 403 | Respostas da API. |
| Exclusão de área em uso | 409 | Resposta da API. |
| Usuário sem login acessa endpoint privado | 401 | Resposta da API. |
| Cadastro público tenta criar administrador | Não existe rota/campo público para isso | Teste de API e código do DTO/Service. |
| Respostas públicas de usuário | Sem senha/hash | JSON de cadastro/login. |
| Persistência após reiniciar o servidor | Vaga/usuário continuam no PostgreSQL | Antes/depois da reinicialização e consulta no Supabase. |
| Operações administrativas | Acesso somente ao administrador | Coleção Postman/Insomnia. |
| CSRF ausente ou incorreto | Escrita bloqueada (403) | Resposta da API. |

## 15. Diagramas e documentação complementar

- [`docs/arquitetura.md`](docs/arquitetura.md): arquitetura, fluxo de candidatura, responsabilidades e inicialização JDBC.
- [`docs/camadas.md`](docs/camadas.md): responsabilidades das camadas.
- [`docs/modelo-dominio.md`](docs/modelo-dominio.md): diagrama de domínio.
- [`docs/esquema-tabelas.md`](docs/esquema-tabelas.md): esquema relacional.
- [`docs/entrega-2-implementacao.md`](docs/entrega-2-implementacao.md): decisões e implementação da Entrega 2.
- [`docs/roteiro-verificacao.md`](docs/roteiro-verificacao.md): roteiro de testes manuais.
- [`docs/checklist-entrega-2.md`](docs/checklist-entrega-2.md): conferência dos requisitos do enunciado e itens de envio.
- [`docs/evidencias/README.md`](docs/evidencias/README.md): instruções para guardar somente evidências reais dos testes e da demonstração.
- [`docs/RELATORIO-ENTREGA-2.md`](docs/RELATORIO-ENTREGA-2.md): diagnóstico e relatório técnico.
- [`Registro_IA.md`](Registro_IA.md): registro de interações relevantes com IA. A equipe deve revisar e confirmar as decisões/experiências descritas antes de enviar.

## 16. Preparação do envio no Moodle

O enunciado solicita **link e ZIP da versão `entrega-2`**, instruções para executar aplicação e banco, diagramas atualizados, documentação da API, dados fictícios, registro de IA e evidências de verificação.

Antes do envio:

1. Faça uma execução final com JDK 17: `mvn clean test`; salve o resultado real.
2. Inicie usando o `.env` local e o Supabase; teste consulta de vagas, cadastro/login e persistência após reiniciar.
3. Execute e registre os cenários manuais do roteiro, incluindo autorização por propriedade, CSRF e administrador.
4. Revise se os diagramas representam a versão efetiva do código e se o `Registro_IA.md` foi confirmado pelo grupo.
5. Confira que `.env`, senha, tokens, arquivos `target/`, `.class` e dados reais não aparecem no ZIP nem no GitHub.
6. Faça commit da documentação/correções e marque a versão no Git, por exemplo:

```bash
git status --short
git add README.md portal_estagio/README.md portal_estagio/Registro_IA.md portal_estagio/docs
 git commit -m "docs: finalizar documentacao da entrega 2"
git tag -a entrega-2 -m "Portal de Estagios - Entrega 2"
git push origin main
git push origin entrega-2
```

7. Gere o ZIP limpo da versão `entrega-2` e envie junto com o link/tag do GitHub, documentação e evidências no Moodle.

**Não inclua** `.env`, credenciais, tokens, `.git`, `target/`, arquivos `.class` ou dados pessoais/reais. O `.env.example` deve permanecer com placeholders.

## 17. Situação conhecida e pendências finais

O código e a documentação cobrem os fluxos da Entrega 2: API REST, DTOs/validação, erros globais, sessão/CSRF, BCrypt, autorização por perfil e propriedade, interface mínima, JDBC/PostgreSQL, conta administrativa controlada, testes e diagramas.

A equipe relatou que o portal passou a funcionar com Supabase depois de colocar o `.env` dentro de `portal_estagio/`. Para que isso se transforme em evidência de entrega, ainda é necessário guardar um log/captura da versão final conectada e concluir a conferência abaixo:

- [ ] `mvn clean test` executado na versão final usando JDK 17; registrar contagem real (há diferença entre os 36 métodos presentes nos fontes e o log anterior de 32 testes executados).
- [ ] A conexão e operações JDBC foram verificadas na versão final contra o Supabase.
- [ ] Persistência foi comprovada após reiniciar a aplicação.
- [ ] Fluxos de estudante, empresa, administrador, CSRF e propriedade foram demonstrados e registrados.
- [ ] Diagrama de arquitetura e de domínio foram revisados contra o código atual.
- [ ] Registro de IA tem ao menos três interações relevantes da Entrega 2 revisadas pelo grupo.
- [ ] ZIP e link/tag `entrega-2` estão prontos para o Moodle, sem segredos nem artefatos de build.

O roteiro e o checklist existem para orientar essa verificação; não substituem a execução e o registro dos resultados pela equipe.
