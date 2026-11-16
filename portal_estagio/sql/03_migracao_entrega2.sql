-- Execute UMA VEZ no banco da Entrega 1 antes de iniciar o Spring Boot.
ALTER TABLE usuario ADD COLUMN IF NOT EXISTS senha VARCHAR(255);
-- Usuários existentes da Entrega 1 não possuem senha. Cada um deve redefinir/cadastrar
-- uma nova conta com outro e-mail ou receber senha por procedimento administrativo seguro.
