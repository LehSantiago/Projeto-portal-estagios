
# Roteiro de verificação (Entrega 1 — perfis simulados)

**Preparação:** recrie o banco com `sql/01_schema.sql` e `sql/02_dados.sql` antes de começar.

Dados de demonstração: usuários 1 Admin, 2 Carla (TechCorp), 3 Paulo (DesignHub), 4 Ana, 5 Bruno; áreas 1 Tecnologia, 2 Administração, 3 Design; vagas 1 e 2 (TechCorp, abertas), 3 (DesignHub, encerrada); candidatura de Bruno na vaga 3.

| # | Cenário | Perfil | Entrada | Esperado | Observado |
|---|---|---|---|---|---|
| 1 | Cadastro válido de estudante | Menu inicial | Opção 2; "Carlos", "carlos@aluno.com", "Direito" | Estudante cadastrado com id | [x] |
| 2 | E-mail repetido | Menu inicial | Opção 2; qualquer nome, "ana@aluno.com" | "Já existe um usuário com o e-mail…" | [x] |
| 3 | E-mail inválido | Menu inicial | Opção 2; e-mail "abc" | "E-mail em formato inválido" | [x] |
| 4 | Vaga válida | Empresa 2 | Opção 1; título, descrição, área 1, local, HIBRIDA, 20 | Vaga publicada com situação ABERTA | [x] |
| 5 | Campo ausente | Empresa 2 | Opção 1; título vazio | "O campo 'título' é obrigatório" | [x] |
| 6 | Carga horária inválida | Empresa 2 | Opção 1; carga 0 e depois -5 | "A carga horária semanal deve ser positiva" | [x] |
| 7 | Carga não numérica | Empresa 2 | Opção 1; carga "abc" | "A carga horária deve ser um número inteiro" | [x] |
| 8 | Consulta de abertas sem filtro | Estudante 4 | Opção 1; Enter em tudo | Vagas 1 e 2 (a 3 não aparece) | [x] |
| 9 | Filtro por área e modalidade | Estudante 4 | Opção 1; área 1; REMOTA | Somente a vaga 2 | [x] |
| 10 | Busca por palavra-chave (lambda) | Estudante 4 | Opção 1; palavra "suporte" | Somente a vaga 2 | [x] |
| 11 | Candidatura válida | Estudante 4 | Opção 2; vaga 1 | Candidatura registrada com data | [x] |
| 12 | Candidatura duplicada | Estudante 4 | Opção 2; vaga 1 novamente | "Você já se candidatou a esta vaga" | [x] |
| 13 | Candidatura em vaga encerrada | Estudante 4 | Opção 2; vaga 3 | "A vaga … está encerrada" | [x] |
| 14 | Empresa não se candidata | Empresa 2 | Tentar via serviço/teste | "Somente estudante pode…" | [x] |
| 15 | Editar vaga de outra empresa | Empresa 3 | Opção 3; vaga 1 | "Você só pode editar vagas da sua própria empresa" | [x] |
| 16 | Encerrar vaga de outra empresa | Empresa 3 | Opção 4; vaga 1 | "Você só pode encerrar vagas da sua própria empresa" | [x] |
| 17 | Candidatos de vaga de outra empresa | Empresa 3 | Opção 5; vaga 1 | "Você só pode consultar candidatos de vagas da sua própria empresa" | [x] |
| 18 | Candidatos da própria vaga | Empresa 2 | Opção 5; vaga 1 (após cenário 11) | Lista com Ana | [x] |
| 19 | Encerrar vaga própria preserva candidaturas | Empresa 2 | Opção 4; vaga 1; depois opção 5; vaga 1 | Vaga ENCERRADA e candidatura de Ana ainda listada | [x] |
| 20 | Estudante vê só as suas | Estudante 5 | Opção 3 | Somente a candidatura na vaga 3 (nenhuma da Ana) | [x] |
| 21 | Excluir área usada | Admin 1 | Opção 4; área 1 | "…é usada por vagas e não pode ser excluída" | [x] |
| 22 | Excluir área livre | Admin 1 | Opção 2 (cadastrar "Teste"); opção 4 nessa área | "Área excluída" | [x] |
| 23 | Área duplicada | Admin 1 | Opção 2; "tecnologia" | "Já existe uma área chamada…" | [x] |
| 24 | Editar área | Admin 1 | Opção 3; área 2; "Gestão" | Área atualizada | [x] |
| 25 | Área por não administrador | Empresa 2 | Menu não oferece; validar no serviço/teste | "Somente administrador pode…" | [x] |
| 26 | Contagens | Admin 1 | Opção 5 | Estudantes, empresas, vagas abertas/encerradas e candidaturas coerentes com os dados | [x] |
| 27 | Persistência | Qualquer perfil | Fechar a aplicação e abrir de novo; consultar candidaturas/vagas | Dados mantidos | [x] |
| 28 | Entrada inválida no menu | Qualquer perfil | Digitar "abc" e "99" nas opções | "Opção inválida"; aplicação continua | [x] |
| 29 | Falha do banco | Qualquer perfil | Parar o PostgreSQL com a aplicação aberta e fazer uma consulta | "[Falha no armazenamento]"; aplicação continua | [x] |
| 30 | Banco indisponível na inicialização | — | Senha errada em `PORTAL_DB_PASSWORD` | Mensagem clara e saída com código 1 | [x] |

## Testes automatizados

Executar:

```bash
mvn test
```

Classe: `RegrasDeNegocioTest` (10 testes).

- [x] `candidaturaDuplicadaEhRejeitada`
- [x] `candidaturaEmVagaEncerradaEhRejeitada`
- [x] `empresaNaoAcessaVagaDeOutraEmpresa`

Os testes usam os DAOs em memória, portanto não dependem do banco de dados.

## Restrições do banco de dados (verificação direta no psql)

- [x] E-mail repetido — `uq_usuario_email`
- [x] Candidatura repetida — `uq_candidatura_estudante_vaga`
- [x] Carga horária igual a zero — `ck_vaga_carga`
- [x] Excluir área em uso — `vaga_area_id_fkey`
- [x] Área duplicada — `uq_area_nome`

## Registro final

- **Data da execução:** 28/10/2026
- **Responsável pelos testes:** Letícia Santiago da Silva
- **Quantidade de cenários executados:** 30 / 30
- **Quantidade de cenários aprovados:** 30 / 30
- **Testes automatizados:** [x] Aprovados
- **Restrições do banco:** [x] Aprovadas
