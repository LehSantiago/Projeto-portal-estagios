# Camadas

```
Menu (console) → Service → interface DAO → implementação JDBC → PostgreSQL
```

| Camada | Pacote | Responsabilidade | Não faz |
|---|---|---|---|
| Apresentação | `app` | Lê entradas, chama serviços, imprime resultados e erros | regra de negócio, SQL |
| Modelo | `model` | Representa Usuário, Área, Vaga, Candidatura e suas associações | acesso a dados |
| Serviço | `service` | Valida entradas, aplica regras e autorização por perfil e propriedade | SQL, `java.sql` |
| DAO (interfaces) | `dao` | Contrato de persistência | regra de negócio |
| DAO (JDBC) | `dao.jdbc` | SQL parametrizado, mapeia ResultSet → objetos, traduz `SQLException` | regra de negócio |
| Exceções | `excecao` | Erros do domínio (validação, regra, acesso negado, persistência) | — |

`app.Contexto` é o único lugar que escolhe as implementações de DAO (JDBC ou memória) e as entrega aos serviços pelo construtor.

## Caminho de uma candidatura
1. **Menu** (estudante, opção 2) lê o id da vaga e chama `CandidaturaService.candidatar(usuarioAtual, vagaId)`.
2. **CandidaturaService** confirma que o ator é ESTUDANTE, busca a vaga (`VagaService`), exige vaga aberta e consulta `CandidaturaDao.existePorEstudanteEVaga`.
3. Se tudo passa, cria `Candidatura` com a data de hoje e chama `CandidaturaDao.salvar`.
4. **CandidaturaDaoJdbc** executa `INSERT ... RETURNING id` com `PreparedStatement` e devolve a candidatura com o id.
5. Se o banco rejeitar (ex.: UNIQUE), `ConexaoFactory.traduzir` converte a `SQLException` em exceção do domínio; o **Menu** captura e mostra a mensagem sem encerrar.

## Interface funcional / lambda (finalidade)
- `VagaService.consultarAbertas(..., palavraChave)`: `Predicate<Vaga>` combinados com `or` para buscar no título ou na descrição sem criar um método por combinação.
- `CandidaturaService.listarCandidatos`: lambda em `Comparator.comparing` para ordenar candidatos por nome.
- `UsuarioDaoJdbc.Parametros`: interface funcional própria que isola como preencher o `PreparedStatement`, evitando repetir o bloco try/catch nas buscas.
