CREATE TYPE dish_type_enum as ENUM ('START', 'MAIN', 'DESSERT');

CREATE TABLE Dish (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    dish_type dish_type_enum NOT NULL
);

CREATE TYPE ingredient_category_enum as ENUM ('VEGETABLE', 'ANIMAL', 'MARINE', 'DAIRY', 'OTHER');

CREATE TABLE Ingredient (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    price NUMERIC(10, 2) NOT NULL,
    category ingredient_category_enum NOT NULL,
    id_dish int REFERENCES Dish(id)
);