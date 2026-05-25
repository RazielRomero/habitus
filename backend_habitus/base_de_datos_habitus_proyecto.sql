CREATE DATABASE IF NOT EXISTS backend
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE backend;

CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,        
    gender ENUM('Masculino', 'Femenino', 'Otro') NOT NULL,   
    birth_date DATE NOT NULL,
    height_cm INT NOT NULL,
    weight_kg DECIMAL(5,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE energy_balance (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    record_date DATE NOT NULL,
    bmi DECIMAL(5,2) NOT NULL,
    calories_in INT NOT NULL,
    calories_out INT NOT NULL,
    net_balance INT NOT NULL,
    energy_status ENUM('DEFICIENTE','BALANCEADO','EXCESIVO') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_energy_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE foods (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    calories_per_unit INT NOT NULL,   -- kcal por unidad
    unit VARCHAR(50) NOT NULL         -- ejemplo: "pieza", "taza", "porción"
) ENGINE=InnoDB;

CREATE TABLE activities (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    calories_per_minute INT NOT NULL  -- kcal por minuto
) ENGINE=InnoDB;

CREATE TABLE food_logs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    record_date DATE NOT NULL,
    food_id INT NOT NULL,
    quantity DECIMAL(5,2) NOT NULL,   -- cuántas unidades/porciones
    CONSTRAINT fk_foodlog_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_foodlog_food
        FOREIGN KEY (food_id) REFERENCES foods(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE activity_logs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    record_date DATE NOT NULL,
    activity_id INT NOT NULL,
    minutes INT NOT NULL,
    CONSTRAINT fk_actlog_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_actlog_activity
        FOREIGN KEY (activity_id) REFERENCES activities(id) ON DELETE CASCADE
) ENGINE=InnoDB;



INSERT INTO foods (name, calories_per_unit, unit) VALUES
    -- Básicos iniciales
    ('Tortilla de maíz',           65,  'pieza'),
    ('Arroz',                     130,  'taza'),
    ('Pollo ',                    200,  'porción'),
    ('Manzana',                    80,  'pieza'),
    ('Refresco',                  140,  'vaso (250 ml)'),

    -- Desayunos / lácteos
    ('Huevos',          160,  'porción'),
    ('Pan integral',               70,  'rebanada'),
    ('Avena',                     150,  'taza'),
    ('Yogurt natural',            120,  'vaso'),
    ('Queso ',                     90,  'rebanada'),

    -- Comidas / guarniciones
    ('Frijoles ',                 110,  '1/2 taza'),
    ('Carne de res asada',        250,  'porción'),
    ('Pescado ',                  180,  'porción'),
    ('Ensalada mixta',             80,  'taza'),
   

    -- Snacks / bebidas
    ('Plátano',                   100,  'pieza'),
    ('Nueces mixtas',             170,  '30 g'),
    ('Galletas saladas',           60,  'pieza'),
    ('Jugo de naranja',           110,  'vaso (250 ml)'),
    ('Agua',                0,  'vaso (250 ml)'),
	('Leche',                150,  'vaso (250 ml)');



INSERT INTO activities (name, calories_per_minute) VALUES
    -- Básicas iniciales
    ('Caminar suave',                  4),
	('Caminar rápido',                 6),
    ('Correr ligero',                  9),
    ('Correr intenso',                12),
    ('Bicicleta',                      8),
    ('Natación',                       9),
    ('Subir escaleras',                8),
    ('Yoga',                     3),
    ('Entrenamiento de fuerza',        7),
    ('Saltar la cuerda',              10);