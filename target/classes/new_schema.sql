CREATE TABLE DishIngredient (
    id SERIAL PRIMARY KEY,
    id_dish INTEGER NOT NULL REFERENCES Dish(id) ON DELETE CASCADE,
    id_ingredient INTEGER NOT NULL REFERENCES Ingredient(id) ON DELETE CASCADE,
    quantity_required NUMERIC(10, 2) NOT NULL,
    unit VARCHAR(10) NOT NULL,
    UNIQUE(id_dish, id_ingredient)
);

DROP TABLE IF EXISTS Dish CASCADE;

CREATE TABLE Dish (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    dish_type VARCHAR(255),
    selling_price NUMERIC(10, 2)
);


CREATE TYPE unit_type_enum AS ENUM ('KG', 'G', 'L', 'ML', 'PIECE');

INSERT INTO DishIngredient (id, id_dish, id_ingredient, quantity_required, unit) VALUES
(1, 1, 1, 0.20, 'KG'),
(2, 1, 2, 0.15, 'KG'),
(3, 2, 3, 1.00, 'KG'),
(4, 4, 4, 0.30, 'KG'),
(5, 4, 5, 0.20, 'KG');

INSERT INTO Dish (id, name, dish_type, selling_price) VALUES
(1, 'Salade fraîche', 'START', 3500.00),
(2, 'Poulet grillé', 'MAIN', 12000.00),
(3, 'Riz aux légumes', 'MAIN', NULL),
(4, 'Gâteau au chocolat', 'DESSERT', 8000.00),
(5, 'Salade de fruits', 'DESSERT', NULL);