insert into dish (id, name, dish_type)
values (1, 'Salaide fraîche', 'STARTER'),
       (2, 'Poulet grillé', 'MAIN'),
       (3, 'Riz aux légumes', 'MAIN'),
       (4, 'Gâteau au chocolat ', 'DESSERT'),
       (5, 'Salade de fruits', 'DESSERT');

insert into ingredient (id, name, category, price)
values (1, 'Laitue', 'VEGETABLE', 800.0),
       (2, 'Tomate', 'VEGETABLE', 600.0),
       (3, 'Poulet', 'ANIMAL', 4500.0),
       (4, 'Chocolat ', 'OTHER', 3000.0),
       (5, 'Beurre', 'DAIRY', 2500.0);

insert into dish_ingredient (dish_id, ingredient_id, required_quantity)
values (1, 2, 100.0),
       (2, 3, 250.0),
       (4, 4, 200.0),
       (4, 5, 100.0);

update dish
set price = 2000.0
where id = 1;

update dish
set price = 6000.0
where id = 2;
