# Projeto Portal de Estágios
## Integrante
- Letícia Santiago da Silva RA: 10420655

## Descrição

Portal que conecta estudantes a vagas de estágio. 
Perfis: estudante, empresa e administrador.
Nesta etapa (14/10) o projeto contém apenas o modelo de domínio e uma demonstração simples no console.

## Requisito de execução

O projeto requer JDK 25 (LTS) e Maven para compilação e execução.

<img width="992" height="877" alt="image" src="https://github.com/user-attachments/assets/331c2a6a-3418-497e-a87f-eed9a26cab52" />

código plantuml 
@startuml
title Portal de Estágios - Modelo de Domínio

skinparam classAttributeIconSize 0

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
    - curso : String
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
    - cargaHorariaSemanal : int  {> 0}
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

' Enums usados como atributos
Usuario --> Perfil : - perfil
Vaga --> Modalidade : - modalidade
Vaga --> SituacaoVaga : - situacao

' Agregações
Usuario "1  empresa" o-- "0..*" Vaga : publica >
Area "1" o-- "0..*" Vaga : classifica >
Usuario "1  estudante" o-- "0..*" Candidatura : realiza >

' Composição
Vaga "1" *-- "0..*" Candidatura : recebe >

note bottom of Candidatura
  Associação entre estudante e vaga.
  Único par (estudante, vaga).
end note

note right of Usuario
  curso: só para ESTUDANTE
  nomeEmpresa: só para EMPRESA
end note

@enduml
