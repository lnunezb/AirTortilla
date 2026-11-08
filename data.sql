-- =======================================================
-- DATOS INICIALES (SEEDS) - AIRTORTILLA (ENTREGA 2)
-- =======================================================

-- Inserción de roles básicos
INSERT OR IGNORE INTO roles (id, nombre) VALUES (1, 'ADMINISTRADOR');
INSERT OR IGNORE INTO roles (id, nombre) VALUES (2, 'EMPLEADO');

-- Usuario administrador inicial:
-- Nombre de usuario: admin
-- Contraseña en texto plano: admin123
-- Hash SHA-256 almacenado: 240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9
INSERT OR IGNORE INTO usuarios (id, nombre_usuario, contrasena, rol_id, activo)
VALUES (
    1,
    'admin',
    '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9',
    1,
    1
);

