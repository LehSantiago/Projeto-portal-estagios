# Registro de uso de IA — Entrega 1

Ferramenta usada em todas as interações: **Claude (Anthropic)**, pelo chat. Usei apenas dados fictícios; a única credencial
real envolvida (senha do banco) foi tratada na Interação 3.

---

## Interação 1 — Estrutura inicial do projeto, sem banco

- **Problema:** precisava de uma base funcional da Entrega 1 (modelo, regras, menu) antes de ter o banco de dados criado.
- **Prompt:** colei o enunciado do projeto e pedi: "faça o código em java até a entrega do dia 28/10, mas por enquanto sem a ligação do banco de dados, porque vou ter que criar ainda".
- **Proposta da IA:** pacotes `app`, `model`, `service`, `dao` e `excecao`; interfaces DAO com implementações **em memória** temporárias;
  serviços que recebem o usuário que executa a ação e validam perfil e propriedade dos registros; menu com identidade simulada;
  uso de lambda (`Predicate` e `Comparator`); testes JUnit das regras.
- **Minha decisão:** aceitei a separação em camadas com DAO por interface, porque permite trocar a memória por JDBC sem alterar os
  serviços. Aceitei também um único `Usuario` com enum `Perfil`, em vez de herança, porque as diferenças entre perfis são só dois
  campos opcionais. [CONFIRME e, se quiser, explique com suas palavras por que concorda.]
- **Mudanças realizadas:** criei o projeto Maven com as classes geradas; li as classes de `service` e o fluxo de uma candidatura
  para conseguir explicá-los.
- **Verificação:** rodei `mvn test` (10 testes, incluindo candidatura duplicada, vaga encerrada e vaga de outra empresa) e executei
  o menu escolhendo usuários de perfis diferentes. [CONFIRME que você rodou.]

---

## Interação 2 — Persistência em PostgreSQL com JDBC e DAO

- **Problema:** implementar a persistência exigida na seção 5 (PostgreSQL, JDBC, DAO, chaves estrangeiras e unicidade de e-mail e do par estudante–vaga).
- **Prompt:** colei a seção 5 do enunciado e perguntei: "como eu faço agora a parte que está faltando do banco de dados?".
- **Proposta da IA:** `sql/01_schema.sql` e `sql/02_dados.sql`; pacote `dao.jdbc` com `ConexaoFactory`, `Mapeadores` e quatro DAOs com `PreparedStatement`
  e *try-with-resources*; tradução de `SQLException` em exceções do domínio (unicidade, chave estrangeira e check viram regra de negócio);
  `Contexto.criarComJdbc`. As regras ficam nos serviços **e** nas restrições do banco (defesa em profundidade).
- **Minha decisão:** usar o **Supabase** (PostgreSQL hospedado) com o *Session pooler*, para não precisar instalar o banco. [CONFIRME.]
  Mantive as restrições no esquema além das validações nos serviços.
- **Mudanças realizadas:** executei os scripts no SQL Editor do Supabase, configurei a conexão e passei o `Main` a usar o JDBC.
- **Verificação:** executei os cenários do `docs/roteiro-verificacao.md`: candidatura duplicada, vaga encerrada, área em uso, vaga de outra
  empresa e persistência dos dados depois de reiniciar a aplicação. [CONFIRME quais você rodou.]

---

## Interação 3 — Senha do banco no código (correção de um erro meu)

- **Problema:** a senha real do banco estava escrita como valor padrão em `ConexaoFactory`, e o enunciado proíbe credenciais reais na entrega.
- **Prompt:** colei a classe `ConexaoFactory` com a senha dentro e perguntei se eu não precisava mudar nada no código, só criar um `env`.
- **Proposta da IA:** apontou que isso violava a regra de entrega e que a senha ficaria no histórico do Git; recomendou **redefinir a senha**,
  voltar o padrão para vazio e ler as credenciais de variáveis de ambiente. Depois explicou que Java não lê `.env` sozinho
  e ajustou o código para ler o arquivo (variável de ambiente, depois `.env`, depois padrão). Também ajustou o parser para ignorar o caractere
  invisível (BOM) que o Bloco de Notas pode gravar.
- **Minha decisão:** usar um arquivo `.env` ignorado pelo Git, com um `.env.example` sem senha no repositório, e redefinir a senha do banco.
  [CONFIRME que você redefiniu a senha.]
- **Mudanças realizadas:** `ConexaoFactory` passou a ler o `.env`; criei `.env.example`; incluí `.env` no `.gitignore`.
- **Verificação:** conferi que o `.env` não aparece no `git status` nem no ZIP; confirmei que a aplicação conecta lendo o `.env`; testei com o arquivo
  ausente e vi que a mensagem de erro informa em qual pasta ele foi procurado. [CONFIRME.]

---

## Interação 4 — Diagrama do modelo versus código

- **Problema:** queria incluir no README o diagrama de classes que eu mesmo fiz na atividade de 14/10.
- **Prompt:** enviei a imagem do diagrama e pedi para incluí-la na documentação.
- **Proposta da IA:** incluiu a imagem em `docs/img/` (caminho relativo, para funcionar no ZIP) e apontou diferenças entre o diagrama e o código:
  nomes dos métodos (`ehEstudante()` no diagrama e `isEstudante()` no código, `estaAberta()` e `isAberta()`), `encerrar()` aparecer em `Vaga`
  enquanto a regra está em `VagaService`, e o construtor de `Usuario` ter mais parâmetros.
- **Minha decisão:** [ESCOLHA UMA: (a) atualizei o diagrama para refletir o código final; ou (b) mantive o diagrama como versão inicial do modelo (14/10) e explico as diferenças na apresentação.]
- **Mudanças realizadas:** [descreva o que você alterou, se alterou.]
- **Verificação:** comparei o diagrama com as classes de `model` e `service` e com as tabelas do banco.

---
