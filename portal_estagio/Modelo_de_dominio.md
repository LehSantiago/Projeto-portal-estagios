# Modelo de domínio

![Diagrama de classes do modelo de domínio do Portal de Estágios](img/modelo-dominio.png)

Diagrama elaborado pelo autor. Abaixo, uma versão em Mermaid com os atributos persistidos e as multiplicidades.

## Versão em Mermaid

```mermaid
classDiagram
    class Usuario {
        Long id
        String nome
        String email
        Perfil perfil
        String curso
        String nomeEmpresa
    }
    class Area {
        Long id
        String nome
    }
    class Vaga {
        Long id
        String titulo
        String descricao
        String localizacao
        Modalidade modalidade
        int cargaHorariaSemanal
        SituacaoVaga situacao
    }
    class Candidatura {
        Long id
        LocalDate dataInscricao
    }
    class Perfil {
        <<enumeration>>
        ESTUDANTE
        EMPRESA
        ADMINISTRADOR
    }
    class Modalidade {
        <<enumeration>>
        PRESENCIAL
        REMOTA
        HIBRIDA
    }
    class SituacaoVaga {
        <<enumeration>>
        ABERTA
        ENCERRADA
    }

    Usuario "1" --> "0..*" Vaga : empresa publica
    Area "1" --> "0..*" Vaga : classifica
    Usuario "1" --> "0..*" Candidatura : estudante se candidata
    Vaga "1" --> "0..*" Candidatura : recebe
    Usuario --> Perfil
    Vaga --> Modalidade
    Vaga --> SituacaoVaga
```

## Quem é responsável por cada informação
- **Usuario**: identidade, e-mail único e perfil. `curso` só existe para estudante; `nomeEmpresa`, só para empresa.
- **Area**: nome único; só o administrador cria, edita e exclui.
- **Vaga**: pertence a uma única empresa (dona) e a uma única área; só a dona edita e encerra.
- **Candidatura**: é a associação estudante–vaga (N:N) com data de inscrição; o par (estudante, vaga) é único.

## Decisões a justificar
- Perfis em uma única classe (`Usuario` + enum `Perfil`), sem herança: as diferenças são só dois campos opcionais,
  e uma tabela única simplifica o DAO e a identidade simulada.
- `Candidatura` como classe própria (e não lista dentro de `Vaga`/`Usuario`) porque carrega dado próprio (data) e precisa de restrição de unicidade.
- Vaga encerrada continua no banco (situação `ENCERRADA`) para preservar as candidaturas anteriores.