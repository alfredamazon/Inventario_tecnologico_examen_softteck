-- ============================================================
-- inventario_db.sql
-- Esquema base para el módulo de Inventario de Activos Tecnológicos
-- Compatible con MariaDB
--
-- IMPORTANTE:
-- 1. Este script NO elimina tablas ni registros existentes.
-- 2. Revise los nombres de columnas contra las entidades Java antes
--    de ejecutarlo en una base que ya tenga datos.
-- 3. Los usuarios/roles de la aplicación suelen administrarse en el
--    backend; este script no crea usuarios JWT ni contraseñas.
-- ============================================================

CREATE DATABASE IF NOT EXISTS inventario_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE inventario_db;

CREATE TABLE IF NOT EXISTS categorias (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL,
    codigo_prefijo VARCHAR(10) NOT NULL,
    descripcion VARCHAR(255) NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_categorias_nombre UNIQUE (nombre),
    CONSTRAINT uq_categorias_codigo_prefijo UNIQUE (codigo_prefijo)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS activos (
    id CHAR(36) NOT NULL,
    folio_inventario VARCHAR(30) NOT NULL,
    numero_serie VARCHAR(100) NOT NULL,
    marca_modelo VARCHAR(200) NOT NULL,
    estado ENUM(
        'DISPONIBLE',
        'ASIGNADO',
        'EN_MANTENIMIENTO',
        'BAJA'
    ) NOT NULL DEFAULT 'DISPONIBLE',
    costo_adquisicion DECIMAL(12,2) NOT NULL,
    fecha_ingreso DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    categoria_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_activos_folio UNIQUE (folio_inventario),
    CONSTRAINT uq_activos_numero_serie UNIQUE (numero_serie),
    CONSTRAINT fk_activos_categoria
        FOREIGN KEY (categoria_id) REFERENCES categorias(id)
        ON UPDATE RESTRICT
        ON DELETE RESTRICT,
    CONSTRAINT chk_activos_costo CHECK (costo_adquisicion >= 0),
    INDEX idx_activos_estado (estado),
    INDEX idx_activos_categoria (categoria_id),
    INDEX idx_activos_costo (costo_adquisicion),
    INDEX idx_activos_marca_modelo (marca_modelo)
) ENGINE=InnoDB;

-- Categorías iniciales requeridas por el examen.
-- INSERT IGNORE evita duplicarlas si el script se ejecuta más de una vez.
INSERT IGNORE INTO categorias (nombre, codigo_prefijo, descripcion) VALUES
    ('Laptop', 'LAP', 'Equipo portátil de cómputo'),
    ('Monitor', 'MON', 'Monitor o pantalla'),
    ('Celular', 'CEL', 'Teléfono celular institucional');

-- Consultas de verificación:
-- SHOW TABLES;
-- DESCRIBE categorias;
-- DESCRIBE activos;
-- SELECT * FROM categorias;
