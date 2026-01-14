INSERT INTO dish (name, dish_type) VALUES
('Salade fraîche', 'START'),
('Poulet grillé', 'MAIN'),
('Riz aux légumes', 'MAIN'),
('Gâteau au chocolat', 'DESSERT'),
('Salade de fruits', 'DESSERT');
INSERT INTO ingredient (name, price, category, id_dish) VALUES
('Laitue', 800.00, 'VEGETABLE', 1),
('Tomate', 600.00, 'VEGETABLE', 1),
('Poulet', 4500.00, 'ANIMAL', 2),
('Chocolat', 3000.00, 'OTHER', 4),
('Beurre', 2500.00, 'DAIRY', 4);
ALTER TABLE dish ADD COLUMN IF NOT EXISTS price DOUBLE;

UPDATE dish SET price = CASE name
    WHEN 'Salade fraîche' THEN 2000.0
    WHEN 'Poulet grillé' THEN 6000.0
    ELSE NULL
END;