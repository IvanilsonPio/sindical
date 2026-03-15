-- Adiciona coluna de perfil de acesso na tabela de usuários
ALTER TABLE usuarios ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'OPERADOR';

-- Define o primeiro usuário (admin) como ADMIN
UPDATE usuarios SET role = 'ADMIN' WHERE username = 'admin';

COMMENT ON COLUMN usuarios.role IS 'Perfil de acesso: ADMIN ou OPERADOR';
