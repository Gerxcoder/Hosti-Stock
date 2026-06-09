-- Migración Flyway que crea TODAS las tablas del sistema Hosti-Stock.
CREATE TABLE bar (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_bar_email UNIQUE (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE ingrediente (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    bar_id BIGINT NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    unidad VARCHAR(20) NOT NULL COMMENT 'GRAMOS | MILILITROS | UNIDADES',
    stock_actual DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    stock_minimo DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_ingrediente_bar FOREIGN KEY (bar_id) REFERENCES bar(id) ON DELETE CASCADE,
    CONSTRAINT uk_ingrediente_bar_nombre UNIQUE (bar_id, nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE plato (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    bar_id BIGINT NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    categoria VARCHAR(50) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_plato_bar FOREIGN KEY (bar_id) REFERENCES bar(id) ON DELETE CASCADE,
    CONSTRAINT uk_plato_bar_nombre UNIQUE (bar_id, nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE plato_ingrediente (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    plato_id BIGINT NOT NULL,
    ingrediente_id BIGINT NOT NULL,
    cantidad DECIMAL(12,2) NOT NULL COMMENT 'Cantidad del ingrediente por unidad de plato',

    CONSTRAINT fk_pi_plato FOREIGN KEY (plato_id) REFERENCES plato(id) ON DELETE CASCADE,
    CONSTRAINT fk_pi_ingrediente FOREIGN KEY (ingrediente_id) REFERENCES ingrediente(id) ON DELETE RESTRICT,
    CONSTRAINT uk_plato_ingrediente UNIQUE (plato_id, ingrediente_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE movimiento_stock (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    bar_id BIGINT NOT NULL,
    ingrediente_id BIGINT NOT NULL,
    tipo VARCHAR(20) NOT NULL COMMENT 'ENTRADA | SALIDA | CONSUMO',
    cantidad DECIMAL(12,2) NOT NULL,
    descripcion VARCHAR(255) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_mov_bar FOREIGN KEY (bar_id) REFERENCES bar(id) ON DELETE CASCADE,
    CONSTRAINT fk_mov_ingrediente FOREIGN KEY (ingrediente_id) REFERENCES ingrediente(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE prediccion (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    bar_id BIGINT NOT NULL,
    ingrediente_id BIGINT NOT NULL,
    fecha_prediccion DATE NOT NULL,
    consumo_previsto DECIMAL(12,2) NOT NULL,
    stock_estimado DECIMAL(12,2) NULL,
    recomendacion_compra DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_pred_bar FOREIGN KEY (bar_id) REFERENCES bar(id) ON DELETE CASCADE,
    CONSTRAINT fk_pred_ingrediente FOREIGN KEY (ingrediente_id) REFERENCES ingrediente(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_ingrediente_bar ON ingrediente(bar_id);
CREATE INDEX idx_plato_bar ON plato(bar_id);
CREATE INDEX idx_movimiento_bar ON movimiento_stock(bar_id);
CREATE INDEX idx_movimiento_ingrediente ON movimiento_stock(ingrediente_id);
CREATE INDEX idx_movimiento_created ON movimiento_stock(created_at);
CREATE INDEX idx_prediccion_bar ON prediccion(bar_id);
CREATE INDEX idx_prediccion_fecha ON prediccion(fecha_prediccion);