-- Portal de Estágios - esquema do banco (PostgreSQL)
-- Pode ser executado várias vezes: recria as tabelas do zero (APAGA OS DADOS).
-- O banco em si deve ser criado antes:  CREATE DATABASE portal_estagios;

DROP TABLE IF EXISTS candidatura CASCADE;
DROP TABLE IF EXISTS vaga        CASCADE;
DROP TABLE IF EXISTS area        CASCADE;
DROP TABLE IF EXISTS usuario     CASCADE;

-- Usuário: um único cadastro para os três perfis (curso só para estudante,
-- nome_empresa só para empresa). A senha será acrescentada na Entrega 2.
CREATE TABLE usuario (
    id           BIGSERIAL    PRIMARY KEY,
    nome         VARCHAR(120) NOT NULL,
    email        VARCHAR(160) NOT NULL,
    perfil       VARCHAR(20)  NOT NULL,
    curso        VARCHAR(120),
    nome_empresa VARCHAR(120),
    CONSTRAINT uq_usuario_email UNIQUE (email),
    CONSTRAINT ck_usuario_email_minusculo CHECK (email = LOWER(email)),
    CONSTRAINT ck_usuario_perfil CHECK (perfil IN ('ESTUDANTE', 'EMPRESA', 'ADMINISTRADOR')),
    CONSTRAINT ck_usuario_detalhe CHECK (
        (perfil = 'ESTUDANTE'     AND curso IS NOT NULL AND nome_empresa IS NULL) OR
        (perfil = 'EMPRESA'       AND nome_empresa IS NOT NULL AND curso IS NULL) OR
        (perfil = 'ADMINISTRADOR' AND curso IS NULL AND nome_empresa IS NULL)
    )
);

-- Área: nome único, sem diferenciar maiúsculas de minúsculas.
CREATE TABLE area (
    id   BIGSERIAL   PRIMARY KEY,
    nome VARCHAR(80) NOT NULL
);
CREATE UNIQUE INDEX uq_area_nome ON area (LOWER(nome));

-- Vaga: pertence a uma empresa e a uma área.
-- A FK para area NÃO tem ON DELETE CASCADE: o banco também impede excluir área em uso.
CREATE TABLE vaga (
    id                    BIGSERIAL    PRIMARY KEY,
    empresa_id            BIGINT       NOT NULL REFERENCES usuario (id),
    area_id               BIGINT       NOT NULL REFERENCES area (id),
    titulo                VARCHAR(150) NOT NULL,
    descricao             TEXT         NOT NULL,
    localizacao           VARCHAR(120) NOT NULL,
    modalidade            VARCHAR(20)  NOT NULL,
    carga_horaria_semanal INTEGER      NOT NULL,
    situacao              VARCHAR(20)  NOT NULL DEFAULT 'ABERTA',
    CONSTRAINT ck_vaga_modalidade CHECK (modalidade IN ('PRESENCIAL', 'REMOTA', 'HIBRIDA')),
    CONSTRAINT ck_vaga_carga      CHECK (carga_horaria_semanal > 0),
    CONSTRAINT ck_vaga_situacao   CHECK (situacao IN ('ABERTA', 'ENCERRADA'))
);
CREATE INDEX ix_vaga_empresa ON vaga (empresa_id);
CREATE INDEX ix_vaga_area    ON vaga (area_id);

-- Candidatura: associação N:N entre estudante e vaga.
-- UNIQUE (estudante_id, vaga_id) impede candidatura repetida.
CREATE TABLE candidatura (
    id             BIGSERIAL PRIMARY KEY,
    estudante_id   BIGINT    NOT NULL REFERENCES usuario (id),
    vaga_id        BIGINT    NOT NULL REFERENCES vaga (id),
    data_inscricao DATE      NOT NULL DEFAULT CURRENT_DATE,
    CONSTRAINT uq_candidatura_estudante_vaga UNIQUE (estudante_id, vaga_id)
);
CREATE INDEX ix_candidatura_vaga ON candidatura (vaga_id);
