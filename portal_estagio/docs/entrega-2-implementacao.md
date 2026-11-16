# Entrega 2 — resumo da implementação

Veja o [README](../README.md) para configuração, endpoints e testes, e [arquitetura.md](arquitetura.md) para os diagramas.

## Principais decisões
- **JDBC + DAOs mantidos** (sem JPA): nenhum problema técnico justificou trocar. `ConexaoFactory` ganhou um construtor que recebe
  o `DataSource` do Spring; os DAOs continuam chamando `conexoes.abrir()`.
- **Services continuam Java puro** e são criados em `PortalConfig` por injeção pelo construtor.
- **Hash da senha fora do modelo**: guardado só pelo DAO (`salvar(usuario, senhaHash)` e `buscarCredencialPorEmail`).
- **`AutenticacaoService`**: valida a senha, gera o BCrypt e reaproveita as regras do `UsuarioService`. O controller não tem SQL nem regra.
- **Perfil `memoria`**: troca só os DAOs por versões em memória; permite testar a API com Spring Security real sem PostgreSQL.
- **CSRF com `CsrfTokenRequestAttributeHandler`** (valor do cookie = valor do cabeçalho): simplifica o uso no Postman/Insomnia.
- **Códigos HTTP**: 400 validação · 401 sem login/credencial inválida · 403 perfil ou propriedade · 404 não encontrado · 409 regra de negócio.
