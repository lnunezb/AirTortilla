-- =======================================================
-- ESQUEMA DE BASE DE DATOS - AIRTORTILLA (ENTREGA 2)
-- Compatible con SQLite, MySQL y PostgreSQL
-- =======================================================

-- 1. Tabla de Roles del sistema
CREATE TABLE IF NOT EXISTS roles (
    id INTEGER PRIMARY KEY,
    nombre TEXT NOT NULL UNIQUE
);

-- 2. Tabla de Usuarios (empleados y administradores)
CREATE TABLE IF NOT EXISTS usuarios (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre_usuario TEXT NOT NULL UNIQUE,
    contrasena TEXT NOT NULL,
    rol_id INTEGER NOT NULL,
    activo INTEGER NOT NULL DEFAULT 1,
    FOREIGN KEY (rol_id) REFERENCES roles(id)
);

-- 3. Tabla de Sesiones (registro de logins y logouts)
CREATE TABLE IF NOT EXISTS sesiones (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    usuario_id INTEGER NOT NULL,
    fecha_inicio TEXT NOT NULL,
    fecha_fin TEXT,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);

-- 4. Tabla de Auditoría (trazabilidad de acciones del sistema)
CREATE TABLE IF NOT EXISTS auditoria (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    usuario_id INTEGER NOT NULL,
    accion TEXT NOT NULL,
    recurso TEXT,
    fecha_hora TEXT NOT NULL,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);

-- 5. Tabla de Reservas (datos migrados de los CSV)
CREATE TABLE IF NOT EXISTS reservas (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    localizador TEXT NOT NULL UNIQUE,
    origen TEXT NOT NULL,
    destino TEXT NOT NULL,
    vuelo TEXT NOT NULL,
    pasajero TEXT NOT NULL,
    fecha TEXT NOT NULL
);

