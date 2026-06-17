ALTER TABLE usuarios ADD COLUMN email VARCHAR(150);
ALTER TABLE usuarios ADD COLUMN reset_token VARCHAR(100);
ALTER TABLE usuarios ADD COLUMN reset_token_expiry TIMESTAMP;

CREATE INDEX idx_usuarios_email ON usuarios(email);
CREATE INDEX idx_usuarios_reset_token ON usuarios(reset_token);
