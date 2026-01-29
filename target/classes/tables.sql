CREATE TABLE restaurant_table (
    id SERIAL PRIMARY KEY,
    number INT NOT NULL UNIQUE
);

CREATE TABLE table_order (
    id SERIAL PRIMARY KEY,
    id_table INT NOT NULL REFERENCES restaurant_table(id),
    id_order INT NOT NULL REFERENCES "Order"(id),
    arrival_datetime TIMESTAMP NOT NULL,
    departure_datetime TIMESTAMP NOT NULL
);
INSERT INTO restaurant_table (number) VALUES (1), (2), (3);
