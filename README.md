# Projeto Portal de Estágios
## Integrante
- Letícia Santiago da Silva RA: 10420655

## Descrição

Portal que conecta estudantes a vagas de estágio. 
Perfis: estudante, empresa e administrador.
Nesta etapa (14/10) o projeto contém apenas o modelo de domínio e uma demonstração simples no console.

<img width="879" height="815" alt="image" src="https://github.com/user-attachments/assets/58418880-36e5-4bc3-a35c-1f170a543012" />

código plantuml 
@startuml
title Portal de Estágios - Modelo de Domínio

skinparam classAttributeIconSize 0
skinparam linetype ortho

package model {

  enum Perfil {
    ESTUDANTE
    EMPRESA
    ADMINISTRADOR
  }

  enum Modalidade {
    PRESENCIAL
    REMOTA
    HIBRIDA
  }

  enum SituacaoVaga {
    ABERTA
    ENCERRADA
  }

  class Usuario {
    - id : Long
    - nome : String
    - email : String  {unique}
    - perfil : Perfil
    - curso : String
    - senha : String
    - nomeEmpresa : String
    --
    + Usuario(id, nome, email, perfil)
    + ehEstudante() : boolean
    + ehEmpresa() : boolean
    + ehAdministrador() : boolean
  }

  class Area {
    - id : Long
    - nome : String  {unique}
    --
    + Area(id, nome)
  }

  class Vaga {
    - id : Long
    - titulo : String
    - descricao : String
    - localizacao : String
    - modalidade : Modalidade
    - cargaHorariaSemanal : int  {> 0}
    - situacao : SituacaoVaga
    --
    + Vaga(id, empresa, titulo, descricao, area,\n localizacao, modalidade, cargaHoraria)
    + estaAberta() : boolean
    + encerrar() : void
  }

  class Candidatura {
    - id : Long
    - dataInscricao : LocalDate
    --
    + Candidatura(id, estudante, vaga, dataInscricao)
  }
}

Usuario --> Perfil
Vaga --> Modalidade
Vaga --> SituacaoVaga

Usuario "1\nempresa" -- "0..*" Vaga : publica >
Area "1" -- "0..*" Vaga : classifica >
Usuario "1\nestudante" -- "0..*" Candidatura : realiza >
Vaga "1" -- "0..*" Candidatura : recebe >

note bottom of Candidatura
  Associação entre estudante e vaga.
  Único par (estudante, vaga).
end note

note right of Usuario
  curso: só para ESTUDANTE
  nomeEmpresa: só para EMPRESA
end note

@enduml
