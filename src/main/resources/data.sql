-- Operadores (JdbcUserDetailsManager) - senhas em BCrypt
-- admin / admin           -> ROLE_ADMIN       (PUT /unlock/{userid})
-- manutencao / manutencao -> ROLE_MANUTENCAO  (POST / e GET /)
INSERT INTO users (username, password, enabled)
VALUES ('admin', '{bcrypt}$2a$10$Kh7nZPKv3J2kI/I3LIgf4ucKbRuQP8sCzMvrXfSuVm4cBNQWvPBsC', TRUE);

INSERT INTO users (username, password, enabled)
VALUES ('manutencao', '{bcrypt}$2a$10$a4OvqKwOCEQW0Vff2j4BjucN3d83LTITTfJH8Q1bFxnuy8yRKm.jO', TRUE);

INSERT INTO authorities (username, authority) VALUES ('admin', 'ROLE_ADMIN');
INSERT INTO authorities (username, authority) VALUES ('manutencao', 'ROLE_MANUTENCAO');

-- Usuarios do sistema (POST /login e PUT /trocarsenha/{userid})
INSERT INTO usuarios (username, senha) VALUES ('joao', '123');
INSERT INTO usuarios (username, senha) VALUES ('maria', '456');
