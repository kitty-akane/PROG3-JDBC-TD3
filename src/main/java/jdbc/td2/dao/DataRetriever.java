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
                VALUES (?, ?, ?, ?)
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
                VALUES (?, ?, ?, ?)
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
        List<Ingredient> ingredients = new ArrayList<>();
        try {
            Connection con = DBConnection.getConnection();
            try (PreparedStatement stmt = con.prepareStatement("SELECT * FROM ingredient LIMIT ? OFFSET ?")) {
                stmt.setInt(1, size);
                stmt.setInt(2, (page - 1) * size);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        ingredients.add(new Ingredient(

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
    // TD4 — SAVE INGREDIENT + STOCK MOVEMENTS
    // =========================

    public Ingredient saveIngredient(Ingredient ingredient) {
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);

            // Upsert ingredient
            PreparedStatement ps = con.prepareStatement("""
            INSERT INTO Ingredient (id, name, category, price)
            VALUES (?, ?, ?::ingredient_category_enum, ?)
            ON CONFLICT (id) DO UPDATE
            SET name = EXCLUDED.name,
                category = EXCLUDED.category,
                price = EXCLUDED.price
            RETURNING id
        """);

            if (ingredient.getId() != null) {
                ps.setInt(1, ingredient.getId());
            } else {
                ps.setNull(1, Types.INTEGER);
            }

            ps.setString(2, ingredient.getName());
            ps.setString(3, ingredient.getCategory().name());
            ps.setDouble(4, ingredient.getPrice());

            ResultSet rs = ps.executeQuery();
            rs.next();
            int ingredientId = rs.getInt(1);
            ingredient.setId(ingredientId);

            // Insert stock movements (APPEND ONLY)
            if (ingredient.getStockMovementList() != null) {
                for (StockMovement sm : ingredient.getStockMovementList()) {
                    PreparedStatement smPs = con.prepareStatement("""
                    INSERT INTO StockMovement
                    (id, id_ingredient, quantity, unit, movement_date)
                    VALUES (?, ?, ?, ?::unit_type_enum, ?)
                    ON CONFLICT (id) DO NOTHING
                """);

                    if (sm.getId() != null) {
                        smPs.setInt(1, sm.getId());
                    } else {
                        smPs.setNull(1, Types.INTEGER);
                    }

                    smPs.setInt(2, ingredientId);
                    smPs.setDouble(3, sm.getQuantity());
                    smPs.setString(4, sm.getUnit().name());
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
    // TD4 — FIND STOCK MOVEMENTS
    // =========================

    public List<StockMovement> findStockMovementsByIngredientId(int ingredientId) {
        List<StockMovement> movements = new ArrayList<>();

        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement("""
            SELECT id, quantity, unit, movement_date
            FROM StockMovement
            WHERE id_ingredient = ?
        """);

            ps.setInt(1, ingredientId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                StockMovement sm = new StockMovement();
                sm.setId(rs.getInt("id"));
                sm.setQuantity(rs.getDouble("quantity"));
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

            ing.setStockMovementList(findStockMovementsByIngredientId(id));
            return ing;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
