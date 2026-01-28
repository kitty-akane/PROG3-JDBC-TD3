package jdbc.td2.dao;

import jdbc.td2.model.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.time.Instant;

public class DataRetriever {

    // =========================
    //  FIND DISH BY ID
    // =========================
    public Dish findDishById(int dishId) {
        try (Connection con = DBConnection.getConnection()) {

            PreparedStatement dishStmt = con.prepareStatement("""
                SELECT id, name, dish_type, selling_price
                FROM Dish
                WHERE id = ?
            """);
            dishStmt.setInt(1, dishId);

            ResultSet rs = dishStmt.executeQuery();
            if (!rs.next()) {
                throw new RuntimeException("Dish not found: " + dishId);
            }

            Dish dish = new Dish();
            dish.setId(rs.getInt("id"));
            dish.setName(rs.getString("name"));
            dish.setDishType(DishTypeEnum.valueOf(rs.getString("dish_type")));
            dish.setPrice(rs.getObject("selling_price") == null
                    ? null
                    : rs.getDouble("selling_price"));

            dish.setIngredients(findDishIngredientsByDishId(dishId, con));

            return dish;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // ======================================
    //  FIND INGREDIENTS OF A DISH
    // ======================================
    public List<DishIngredient> findDishIngredientsByDishId(int dishId) {
        try (Connection con = DBConnection.getConnection()) {
            return findDishIngredientsByDishId(dishId, con);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private List<DishIngredient> findDishIngredientsByDishId(int dishId, Connection con)
            throws SQLException {

        List<DishIngredient> result = new ArrayList<>();

        PreparedStatement stmt = con.prepareStatement("""
            SELECT di.quantity_required, di.unit,
                   i.id, i.name, i.price, i.category
            FROM DishIngredient di
            JOIN Ingredient i ON di.id_ingredient = i.id
            WHERE di.id_dish = ?
        """);
        stmt.setInt(1, dishId);

        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            Ingredient ing = new Ingredient(
                    rs.getInt("id"),
                    rs.getString("name"),
                    CategoryEnum.valueOf(rs.getString("category")),
                    rs.getDouble("price")
            );

            DishIngredient di = new DishIngredient();
            di.setIngredient(ing);
            di.setQuantity(rs.getDouble("quantity_required"));
            di.setUnit(UnitEnum.valueOf(rs.getString("unit")));

            result.add(di);
        }

        return result;
    }

    // =========================
    // SAVE DISH
    // =========================
    public Dish saveDish(Dish dish) {
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);

            String sql = """
                INSERT INTO Dish (id, name, dish_type, selling_price)
                VALUES (?, ?, ?::dish_type_enum, ?)
                ON CONFLICT (id) DO UPDATE
                SET name = EXCLUDED.name,
                    dish_type = EXCLUDED.dish_type,
                    selling_price = EXCLUDED.selling_price
                RETURNING id
            """;

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                if (dish.getId() != null) {
                    ps.setInt(1, dish.getId());
                } else {
                    ps.setNull(1, Types.INTEGER);
                }

                ps.setString(2, dish.getName());
                ps.setString(3, dish.getDishType().name());

                if (dish.getPrice() != null) {
                    ps.setDouble(4, dish.getPrice());
                } else {
                    ps.setNull(4, Types.NUMERIC);
                }

                ResultSet rs = ps.executeQuery();
                rs.next();
                int id = rs.getInt(1);

                con.commit();

                return findDishById(id);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // =========================
    //  ADD INGREDIENT
    // =========================
    public void addIngredientToDish(
            int dishId,
            int ingredientId,
            double quantity,
            String unit
    ) {
        try (Connection con = DBConnection.getConnection()) {

            PreparedStatement ps = con.prepareStatement("""
                INSERT INTO DishIngredient (id_dish, id_ingredient, quantity_required, unit)
                VALUES (?, ?, ?, ?::unit_type_enum)
                ON CONFLICT (id_dish, id_ingredient)
                DO UPDATE SET quantity_required = EXCLUDED.quantity_required,
                              unit = EXCLUDED.unit
            """);

            ps.setInt(1, dishId);
            ps.setInt(2, ingredientId);
            ps.setDouble(3, quantity);
            ps.setString(4, unit);

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // =========================
    // UPDATE QUANTITY
    // =========================
    public void updateIngredientQuantity(
            int dishId,
            int ingredientId,
            double quantity
    ) {
        try (Connection con = DBConnection.getConnection()) {

            PreparedStatement ps = con.prepareStatement("""
                UPDATE DishIngredient
                SET quantity_required = ?
                WHERE id_dish = ? AND id_ingredient = ?
            """);

            ps.setDouble(1, quantity);
            ps.setInt(2, dishId);
            ps.setInt(3, ingredientId);

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // =========================
    // REMOVE INGREDIENT
    // =========================
    public void removeIngredientFromDish(
            int dishId,
            int ingredientId
    ) {
        try (Connection con = DBConnection.getConnection()) {

            PreparedStatement ps = con.prepareStatement("""
                DELETE FROM DishIngredient
                WHERE id_dish = ? AND id_ingredient = ?
            """);

            ps.setInt(1, dishId);
            ps.setInt(2, ingredientId);

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Ingredient> findIngredients(int page, int size) {
        List<Ingredient> list = new ArrayList<>();

        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement("""
                SELECT id, name, price, category
                FROM Ingredient
                LIMIT ? OFFSET ?
            """);

            ps.setInt(1, size);
            ps.setInt(2, (page - 1) * size);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Ingredient(
                        rs.getInt("id"),
                        rs.getString("name"),
                        CategoryEnum.valueOf(rs.getString("category")),
                        rs.getDouble("price")
                ));
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return list;
    }

    public List<Dish> findDishesByIngredientName(String ingredientName) {
        List<Dish> dishes = new ArrayList<>();
        try {
            Connection con = DBConnection.getConnection();
            String sql = """
                SELECT DISTINCT d.* FROM dish d 
                JOIN dishingredient di ON d.id = di.id_dish 
                JOIN ingredient i ON di.id_ingredient = i.id 
                WHERE i.name = ?
            """;

            try (PreparedStatement stmt = con.prepareStatement(sql)) {
                stmt.setString(1, ingredientName);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Dish dish = new Dish(
                                rs.getInt("id"),
                                rs.getString("name"),
                                DishTypeEnum.valueOf(rs.getString("dish_type"))
                        );
                        Double sellingPrice = rs.getObject("selling_price") != null
                                ? rs.getDouble("selling_price") : null;
                        dish.setPrice(sellingPrice);
                        dish.setIngredients(findDishIngredientsByDishId(dish.getId(), con));
                        dishes.add(dish);
                    }
                }
            }
            DBConnection.closeConnection(con);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return dishes;
    }

    public List<Ingredient> findIngredientsByCriteria(String ingredientName, CategoryEnum category, String dishName, int page, int size) {
        List<Ingredient> ingredients = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
            SELECT DISTINCT i.* FROM ingredient i 
            JOIN dishingredient di ON i.id = di.id_ingredient 
            JOIN dish d ON di.id_dish = d.id WHERE 1=1
        """);
        List<Object> params = new ArrayList<>();

        if (ingredientName != null && !ingredientName.trim().isEmpty()) {
            sql.append(" AND i.name ILIKE ?");
            params.add("%" + ingredientName + "%");
        }
        if (category != null) {
            sql.append(" AND i.category = ?::ingredient_category_enum");
            params.add(category.toString());
        }
        if (dishName != null && !dishName.trim().isEmpty()) {
            sql.append(" AND d.name ILIKE ?");
            params.add("%" + dishName + "%");
        }
        sql.append(" LIMIT ? OFFSET ?");

        try {
            Connection con = DBConnection.getConnection();
            try (PreparedStatement stmt = con.prepareStatement(sql.toString())) {
                int index = 1;
                for (Object param : params) {
                    stmt.setObject(index++, param);
                }
                stmt.setInt(index++, size);
                stmt.setInt(index, (page - 1) * size);

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        ingredients.add(new Ingredient(
                                rs.getInt("id"),
                                rs.getString("name"),
                                CategoryEnum.valueOf(rs.getString("category")),
                                rs.getDouble("price")
                        ));

                    }
                }
            }
            DBConnection.closeConnection(con);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return ingredients;
    }
    
    // =========================
    // TD4 — SAVE INGREDIENT + STOCK MOVEMENTS (WITH movement_type)
    // =========================
    public Ingredient saveIngredient(Ingredient ingredient) {
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            PreparedStatement ps;

            if (ingredient.getId() == null) {
                ps = con.prepareStatement("""
                    INSERT INTO ingredient (name, category, price)
                    VALUES (?, ?::ingredient_category_enum, ?)
                    RETURNING id
                """);
                ps.setString(1, ingredient.getName());
                ps.setString(2, ingredient.getCategory().name());
                ps.setDouble(3, ingredient.getPrice());
            } else {
                ps = con.prepareStatement("""
                    UPDATE ingredient
                    SET name = ?, category = ?::ingredient_category_enum, price = ?
                    WHERE id = ?
                    RETURNING id
                """);
                ps.setString(1, ingredient.getName());
                ps.setString(2, ingredient.getCategory().name());
                ps.setDouble(3, ingredient.getPrice());
                ps.setInt(4, ingredient.getId());
            }

            ResultSet rs = ps.executeQuery();
            rs.next();
            ingredient.setId(rs.getInt(1));
        
            if (ingredient.getStockMovementList() != null) {
                for (StockMovement sm : ingredient.getStockMovementList()) {
                    PreparedStatement smPs = con.prepareStatement("""
                        INSERT INTO stock_movement
                        (id_ingredient, quantity, unit, movement_type, movement_date)
                        VALUES (?, ?, ?::unit_type_enum, ?::movement_type, ?)
                    """);

                    smPs.setInt(1, ingredient.getId());
                    
                    // Determine movement type based on quantity sign
                    double quantity = sm.getQuantity();
                    String movementType;
                    if (quantity >= 0) {
                        movementType = "IN";
                    } else {
                        movementType = "OUT";
                        quantity = Math.abs(quantity);  
                    }
                    
                    smPs.setDouble(2, quantity);
                    smPs.setString(3, sm.getUnit().name());
                    smPs.setString(4, movementType);
                    smPs.setTimestamp(5, Timestamp.from(sm.getMovementDate()));

                    smPs.executeUpdate();
                }
            }
            
            con.commit();
            return ingredient;
            
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    
    // =========================
    // FIND INGREDIENT BY ID
    // =========================
    public Ingredient findIngredientById(int id) {
        try (Connection con = DBConnection.getConnection()) {

            PreparedStatement ps = con.prepareStatement("""
                SELECT id, name, category, price
                FROM Ingredient
                WHERE id = ?
            """);
            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                throw new RuntimeException("Ingredient not found: " + id);
            }

            Ingredient ing = new Ingredient();
            ing.setId(rs.getInt("id"));
            ing.setName(rs.getString("name"));
            ing.setCategory(CategoryEnum.valueOf(rs.getString("category")));
            ing.setPrice(rs.getDouble("price"));

            // Load stock movements
            ing.setStockMovementList(findStockMovementsByIngredientId(id));
            
            return ing;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // =========================
    // FIND STOCK MOVEMENTS BY INGREDIENT ID (WITH movement_type)
    // =========================
    public List<StockMovement> findStockMovementsByIngredientId(int ingredientId) {
        List<StockMovement> movements = new ArrayList<>();

        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement("""
                SELECT id, quantity, unit, movement_type, movement_date
                FROM stock_movement
                WHERE id_ingredient = ?
                ORDER BY movement_date
            """);

            ps.setInt(1, ingredientId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                StockMovement sm = new StockMovement();
                sm.setId(rs.getInt("id"));
                
                // Convert based on movement_type: IN = positive, OUT = negative
                double quantity = rs.getDouble("quantity");
                String movementType = rs.getString("movement_type");
                if ("OUT".equals(movementType)) {
                    quantity = -quantity;  // Make it negative for OUT
                }
                
                sm.setQuantity(quantity);
                sm.setUnit(UnitEnum.valueOf(rs.getString("unit")));
                sm.setMovementDate(rs.getTimestamp("movement_date").toInstant());
                movements.add(sm);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return movements;
    }

    // =========================
    // CHECK STOCK (HELPER METHOD)
    // =========================
    private void checkStock(Order order) {
        for (DishOrder dOrder : order.getDishOrders()) {
            Dish dish = findDishById(dOrder.getDish().getId());

            for (DishIngredient di : dish.getIngredients()) {
                Ingredient ing = findIngredientById(di.getIngredient().getId());

                double needed = di.getQuantity() * dOrder.getQuantity();
                double available = ing.getStockValueAt(java.time.Instant.now());

                if (available < needed) {
                    throw new RuntimeException(
                            "Stock insuffisant pour l'ingrédient: " + ing.getName());
                }
            }
        }
    }

    // =========================
    // SAVE ORDER (WITH movement_type for stock movements)
    // =========================
    public Order saveOrder(Order order) {
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);

            checkStock(order);

            // Generate reference
            PreparedStatement refStmt = con.prepareStatement("""
                SELECT COALESCE(MAX(id), 0) + 1 FROM "Order"
            """);
            ResultSet rs = refStmt.executeQuery();
            rs.next();
            int nextId = rs.getInt(1);
            String ref = String.format("ORD%05d", nextId);

            // Insert Order
            PreparedStatement orderStmt = con.prepareStatement("""
                INSERT INTO "Order"(reference, creation_datetime)
                VALUES (?, ?)
                RETURNING id
            """);
            orderStmt.setString(1, ref);
            orderStmt.setTimestamp(2, Timestamp.from(Instant.now()));
            rs = orderStmt.executeQuery();
            rs.next();
            int orderId = rs.getInt(1);

            // Insert DishOrder + stock movements
            for (DishOrder dOrder : order.getDishOrders()) {

                PreparedStatement doStmt = con.prepareStatement("""
                    INSERT INTO DishOrder(id_order, id_dish, quantity)
                    VALUES (?, ?, ?)
                """);
                doStmt.setInt(1, orderId);
                doStmt.setInt(2, dOrder.getDish().getId());
                doStmt.setInt(3, dOrder.getQuantity());
                doStmt.executeUpdate();

                Dish dish = findDishById(dOrder.getDish().getId());

                // Create OUT movements for each ingredient
                for (DishIngredient di : dish.getIngredients()) {
                    PreparedStatement sm = con.prepareStatement("""
                        INSERT INTO stock_movement
                        (id_ingredient, quantity, unit, movement_type, movement_date)
                        VALUES (?, ?, ?::unit_type_enum, 'OUT'::movement_type, ?)
                    """);
                    sm.setInt(1, di.getIngredient().getId());
                    sm.setDouble(2, di.getQuantity() * dOrder.getQuantity());  // POSITIVE value with OUT type
                    sm.setString(3, di.getUnit().name());
                    sm.setTimestamp(4, Timestamp.from(Instant.now()));
                    sm.executeUpdate();
                }
            }

            con.commit();

            order.setId(orderId);
            order.setReference(ref);
            return order;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // =========================
    // FIND ORDER BY REFERENCE
    // =========================
    public Order findOrderByReference(String ref) {
        try (Connection con = DBConnection.getConnection()) {

            PreparedStatement ps = con.prepareStatement("""
                SELECT id, creation_datetime
                FROM "Order"
                WHERE reference = ?
            """);
            ps.setString(1, ref);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                throw new RuntimeException("Order not found: " + ref);
            }

            Order order = new Order();
            order.setId(rs.getInt("id"));
            order.setReference(ref);
            order.setCreationDatetime(
                    rs.getTimestamp("creation_datetime").toInstant());

            PreparedStatement dps = con.prepareStatement("""
                SELECT id_dish, quantity FROM DishOrder
                WHERE id_order = ?
            """);
            dps.setInt(1, order.getId());
            rs = dps.executeQuery();

            List<DishOrder> list = new ArrayList<>();
            while (rs.next()) {
                DishOrder d = new DishOrder();
                d.setDish(findDishById(rs.getInt("id_dish")));
                d.setQuantity(rs.getInt("quantity"));
                list.add(d);
            }

            order.setDishOrders(list);
            return order;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
