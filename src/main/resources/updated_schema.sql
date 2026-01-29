CREATE TYPE unit_type_enum AS ENUM ('KG', 'G', 'L', 'ML', 'PIECE');
CREATE TYPE dish_type_enum AS ENUM ('START', 'MAIN', 'DESSERT');
CREATE TYPE ingredient_category_enum AS ENUM ('VEGETABLE', 'ANIMAL', 'MARINE', 'DAIRY', 'OTHER');
CREATE TYPE movement_type AS ENUM ('IN', 'OUT');

CREATE TABLE Dish (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    dish_type dish_type_enum NOT NULL,
    selling_price NUMERIC(10, 2)
);

CREATE TABLE Ingredient (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    price NUMERIC(10, 2) NOT NULL,
    category ingredient_category_enum NOT NULL
);

CREATE TABLE DishIngredient (
    id SERIAL PRIMARY KEY,
    id_dish INTEGER NOT NULL REFERENCES Dish(id) ON DELETE CASCADE,
    id_ingredient INTEGER NOT NULL REFERENCES Ingredient(id) ON DELETE CASCADE,
    quantity_required NUMERIC(10, 2) NOT NULL,
    unit unit_type_enum NOT NULL,
    UNIQUE(id_dish, id_ingredient)
);

CREATE TABLE stock_movement (
    id SERIAL PRIMARY KEY,
    id_ingredient INT NOT NULL,
    quantity NUMERIC NOT NULL,
    unit unit_type_enum NOT NULL,
    movement_date TIMESTAMP NOT NULL,
    FOREIGN KEY (id_ingredient) REFERENCES Ingredient(id)
);
CREATE TABLE "Order" (
    id SERIAL PRIMARY KEY,
    reference VARCHAR NOT NULL UNIQUE,
    creation_datetime TIMESTAMP NOT NULL
);

CREATE TABLE DishOrder (
    id SERIAL PRIMARY KEY,
    id_order INTEGER NOT NULL REFERENCES "Order"(id) ON DELETE CASCADE,
    id_dish INTEGER NOT NULL REFERENCES Dish(id),
    quantity INTEGER NOT NULL,
    UNIQUE (id_order, id_dish)
);

INSERT INTO Dish (id, name, dish_type, selling_price) VALUES
(1, 'Salade fraîche', 'START', 3500.00),
(2, 'Poulet grillé', 'MAIN', 12000.00),
(3, 'Riz aux légumes', 'MAIN', NULL),
(4, 'Gâteau au chocolat', 'DESSERT', 8000.00),
(5, 'Salade de fruits', 'DESSERT', NULL);

INSERT INTO Ingredient (id, name, category, price) VALUES
(1, 'Laitue', 'VEGETABLE', 800.0),
(2, 'Tomate', 'VEGETABLE', 600.0),
(3, 'Poulet', 'ANIMAL', 4500.0),
(4, 'Chocolat', 'OTHER', 3000.0),
(5, 'Beurre', 'DAIRY', 2500.0);

SELECT setval('dish_id_seq', (SELECT COALESCE(MAX(id), 0) FROM Dish), true);
SELECT setval('ingredient_id_seq', (SELECT COALESCE(MAX(id), 0) FROM Ingredient), true);
SELECT setval('dishingredient_id_seq', (SELECT COALESCE(MAX(id), 0) FROM DishIngredient), true);
SELECT setval('stock_movement_id_seq', (SELECT COALESCE(MAX(id), 0) FROM stock_movement), true);
SELECT setval('"Order_id_seq"', (SELECT COALESCE(MAX(id), 0) FROM "Order"), true);
SELECT setval('dishorder_id_seq', (SELECT COALESCE(MAX(id), 0) FROM DishOrder), true);

