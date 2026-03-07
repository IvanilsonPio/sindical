-- Script para inserir usuário administrador
-- Senha: admin123 (criptografada com BCrypt)

INSERT INTO usuarios (username, password, nome, status, criado_em, atualizado_em) 
VALUES ('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Administrador', 'ATIVO', NOW(), NOW());

-- Verificar se o usuário foi criado
SELECT id, username, nome, status, criado_em FROM usuarios WHERE username = 'admin';
