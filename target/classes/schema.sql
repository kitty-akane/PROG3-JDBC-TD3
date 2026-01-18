create type dish_type as enum ('STARTER', 'MAIN', 'DESSERT');
create type ingredient_category as enum ('VEGETABLE', 'ANIMAL', 'MARINE', 'DAIRY', 'OTHER');

create table dish (
    id serial primary key,
    name varchar(255) not null,
    dish_type dish_type not null,
    price numeric(10,2)
);


create table ingredient (
    id serial primary key,
    name varchar(255) not null,
    price numeric(10,2) not null,
    category ingredient_category not null
);


create table dish_ingredient (
    dish_id int not null references dish(id),
    ingredient_id int not null references ingredient(id),
    required_quantity numeric(10,2) not null,
    primary key (dish_id, ingredient_id)
);
