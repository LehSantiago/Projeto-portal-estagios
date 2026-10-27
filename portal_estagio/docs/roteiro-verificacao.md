# Roteiro de verificação (Entrega 1 — perfis simulados)

**Preparação:** recrie o banco com `sql/01_schema.sql` e `sql/02_dados.sql` antes de começar.
Dados de demonstração: usuários 1 Admin, 2 Carla (TechCorp), 3 Paulo (DesignHub), 4 Ana, 5 Bruno;
áreas 1 Tecnologia, 2 Administração, 3 Design; vagas 1 e 2 (TechCorp, abertas), 3 (DesignHub, **encerrada**);
candidatura de Bruno na vaga 3.

Preencha a coluna **Observado** executando cada cenário e anexe prints/saída do console.

| # | Cenário | Perfil | Entrada | Esperado | Observado |
|---|---|---|---|---|---|
| 1 | Cadastro válido de estudante | (menu inicial) | opção 2; "Carlos", "carlos@aluno.com", "Direito" | Estudante cadastrado com id | |
| 2 | E-mail repetido | (menu inicial) | opção 2; qualquer nome, "ana@aluno.com" | "Já existe um usuário com o e-mail…" | |
| 3 | E-mail inválido | (menu inicial) | opção 2; e-mail "abc" | "E-mail em formato inválido" | |
| 4 | Vaga válida | Empresa 2 | opção 1; título, descrição, área 1, local, HIBRIDA, 20 | Vaga publicada com situação ABERTA | |
| 5 | Campo ausente | Empresa 2 | opção 1; título vazio | "O campo 'título' é obrigatório" | |
| 6 | Carga horária inválida | Empresa 2 | opção 1; carga 0 e depois -5 | "A carga horária semanal deve ser positiva" | |
| 7 | Carga não numérica | Empresa 2 | opção 1; carga "abc" | "A carga horária deve ser um número inteiro" | |
| 8 | Consulta de abertas sem filtro | Estudante 4 | opção 1; Enter em tudo | Vagas 1 e 2 (a 3 não aparece) | |
| 9 | Filtro por área e modalidade | Estudante 4 | opção 1; área 1; REMOTA | Somente a vaga 2 | |
| 10 | Busca por palavra-chave (lambda) | Estudante 4 | opção 1; palavra "suporte" | Somente a vaga 2 | |
| 11 | Candidatura válida | Estudante 4 | opção 2; vaga 1 | Candidatura registrada com data | |
| 12 | Candidatura duplicada | Estudante 4 | opção 2; vaga 1 de novo | "Você já se candidatou a esta vaga" | |
| 13 | Candidatura em vaga encerrada | Estudante 4 | opção 2; vaga 3 | "A vaga … está encerrada" | |
| 14 | Empresa não se candidata | Empresa 2 | (tentar via serviço/teste) | "Somente estudante pode…" | |
| 15 | Editar vaga de outra empresa | Empresa 3 | opção 3; vaga 1 | "Você só pode editar vagas da sua própria empresa" | |
| 16 | Encerrar vaga de outra empresa | Empresa 3 | opção 4; vaga 1 | "Você só pode encerrar vagas da sua própria empresa" | |
| 17 | Candidatos de vaga de outra empresa | Empresa 3 | opção 5; vaga 1 | "Você só pode consultar candidatos de vagas da sua própria empresa" | |
| 18 | Candidatos da própria vaga | Empresa 2 | opção 5; vaga 1 (após cenário 11) | Lista com Ana | |
| 19 | Encerrar vaga própria preserva candidaturas | Empresa 2 | opção 4; vaga 1; depois opção 5; vaga 1 | Vaga ENCERRADA e candidatura de Ana ainda listada | |
| 20 | Estudante vê só as suas | Estudante 5 | opção 3 | Somente a candidatura na vaga 3 (nenhuma da Ana) | |
| 21 | Excluir área usada | Admin 1 | opção 4; área 1 | "…é usada por vagas e não pode ser excluída" | |
| 22 | Excluir área livre | Admin 1 | opção 2 (cadastrar "Teste"), opção 4 nessa área | "Área excluída" | |
| 23 | Área duplicada | Admin 1 | opção 2; "tecnologia" | "Já existe uma área chamada…" | |
| 24 | Editar área | Admin 1 | opção 3; área 2; "Gestão" | Área atualizada | |
| 25 | Área por não-admin | Empresa 2 | (menu não oferece; validado no serviço/teste) | "Somente administrador pode…" | |
| 26 | Contagens | Admin 1 | opção 5 | Estudantes, empresas, vagas abertas/encerradas e candidaturas coerentes com os dados | |
| 27 | Persistência | qualquer | fechar a aplicação e abrir de novo; consultar candidaturas/vagas | Dados mantidos | |
| 28 | Entrada inválida no menu | qualquer | digitar "abc" e "99" nas opções | "Opção inválida"; aplicação continua | |
| 29 | Falha do banco | qualquer | parar o PostgreSQL com a aplicação aberta e fazer uma consulta | "[Falha no armazenamento]"; aplicação continua | |
| 30 | Banco indisponível na inicialização | — | senha errada em `PORTAL_DB_PASSWORD` | Mensagem clara e saída com código 1 | |

## Testes automatizados
`mvn test` — `RegrasDeNegocioTest` (10 testes). Os três obrigatórios:
`candidaturaDuplicadaEhRejeitada`, `candidaturaEmVagaEncerradaEhRejeitada`, `empresaNaoAcessaVagaDeOutraEmpresa`.
Usam os DAOs em memória, então não dependem do banco.

## Restrições do banco (verificação direta no psql)
Tentar inserir e-mail repetido, candidatura repetida, carga 0, excluir área em uso e área duplicada
deve gerar erro de restrição (`uq_usuario_email`, `uq_candidatura_estudante_vaga`, `ck_vaga_carga`,
`vaga_area_id_fkey`, `uq_area_nome`).
