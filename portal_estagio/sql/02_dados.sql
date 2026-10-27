-- Dados fictícios para demonstração (identidade simulada). Execute depois de 01_schema.sql.

INSERT INTO usuario (nome, email, perfil, curso, nome_empresa) VALUES
    ('Admin Demo', 'admin@portal.com',      'ADMINISTRADOR', NULL,                    NULL),
    ('Carla RH',   'rh@techcorp.com',       'EMPRESA',       NULL,                    'TechCorp'),
    ('Paulo RH',   'rh@designhub.com',      'EMPRESA',       NULL,                    'DesignHub'),
    ('Ana Souza',  'ana@aluno.com',         'ESTUDANTE',     'Ciência da Computação', NULL),
    ('Bruno Lima', 'bruno@aluno.com',       'ESTUDANTE',     'Design Gráfico',        NULL);

INSERT INTO area (nome) VALUES ('Tecnologia'), ('Administração'), ('Design');

INSERT INTO vaga (empresa_id, area_id, titulo, descricao, localizacao, modalidade, carga_horaria_semanal, situacao) VALUES
    ((SELECT id FROM usuario WHERE email = 'rh@techcorp.com'),
     (SELECT id FROM area WHERE nome = 'Tecnologia'),
     'Estágio em Desenvolvimento Java', 'Apoio ao desenvolvimento de APIs', 'São Paulo', 'HIBRIDA', 30, 'ABERTA'),
    ((SELECT id FROM usuario WHERE email = 'rh@techcorp.com'),
     (SELECT id FROM area WHERE nome = 'Tecnologia'),
     'Estágio em Suporte', 'Atendimento a usuários internos', 'Remoto', 'REMOTA', 20, 'ABERTA'),
    ((SELECT id FROM usuario WHERE email = 'rh@designhub.com'),
     (SELECT id FROM area WHERE nome = 'Design'),
     'Estágio em UX', 'Prototipação de interfaces', 'Campinas', 'PRESENCIAL', 25, 'ENCERRADA');

INSERT INTO candidatura (estudante_id, vaga_id, data_inscricao) VALUES
    ((SELECT id FROM usuario WHERE email = 'bruno@aluno.com'),
     (SELECT id FROM vaga WHERE titulo = 'Estágio em UX'), CURRENT_DATE);
