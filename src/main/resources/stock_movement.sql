CREATE TYPE movement_type AS ENUM ('IN', 'OUT');

CREATE TABLE stock_movement (
    id SERIAL PRIMARY KEY,
    ingredient_id INT NOT NULL,
    quantity NUMERIC NOT NULL,
    unit unit_type_enum NOT NULL,
    movement_type movement_type NOT NULL,
    creation_datetime TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (ingredient_id) REFERENCES ingredient(id)
);

INSERT INTO stock_movement (id, ingredient_id, quantity, unit, movement_type, creation_datetime) VALUES
(1, 1, 5.0, 'KG', 'IN', '2024-01-05 08:00:00'),
(2, 1, 0.2, 'KG', 'OUT', '2024-01-06 12:00:00'),
(3, 2, 4.0, 'KG', 'IN', '2024-01-05 08:00:00'),
(4, 2, 0.15, 'KG', 'OUT', '2024-01-06 12:00:00'),
(5, 3, 10.0, 'KG', 'IN', '2024-01-04 09:00:00');
INSERT INTO stock_movement (id, ingredient_id, quantity, unit, movement_type, creation_datetime) VALUES
(6, 3, 1.0, 'KG', 'OUT', '2024-01-06 13:00:00'),
(7, 4, 3.0, 'KG', 'IN', '2024-01-06 12:00:00'),
(8, 4, 0.3, 'KG', 'OUT', '2024-01-06 14:00:00'),
(9, 5, 2.5, 'KG', 'IN', '2024-01-05 10:00:00'),
(10, 5, 0.2, 'KG', 'IN', '2024-01-06 14:00:00');