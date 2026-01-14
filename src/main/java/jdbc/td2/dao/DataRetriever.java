package jdbc.td2.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import jdbc.td2.model.CategoryEnum;
import jdbc.td2.model.Dish;
import jdbc.td2.model.DishTypeEnum;
import jdbc.td2.model.Ingredient;

public class DataRetriever {

    private final DBConnection dbConnection = new DBConnection();

    //  a) findDishById 
    public Dish findDishById(int id) throws SQLException {

        String sql = "SELECT id, name, dish_type, price FROM dish WHERE id = ?";

        try (
                Connection connection = DBConnection.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                double price = rs.getObject("price") == null ? 0 : rs.getDouble("price");
                return new Dish(
                    rs.getInt("id"),
                    rs.getString("name"),
                    DishTypeEnum.valueOf(rs.getString("dish_type")),
                    price,
                    getIngredientsByDishId(id, connection), price
                );
            }
        }
        return null;
    }

    private List<Ingredient> getIngredientsByDishId(int dishId, Connection connection)
            throws SQLException {

        String sql = """
            SELECT id, name, price, category
            FROM ingredient
            WHERE id_dish = ?
            """;

        List<Ingredient> ingredients = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, dishId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                ingredients.add(new Ingredient(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        CategoryEnum.valueOf(rs.getString("category"))
                ));
            }
        }
        return ingredients;
    }

    public List<Ingredient> findIngredients(int page, int size) throws SQLException {

        String sql = """
            SELECT id, name, price, category
            FROM ingredient
            ORDER BY id
            LIMIT ? OFFSET ?
            """;

        List<Ingredient> ingredients = new ArrayList<>();

        try (
                Connection connection = DBConnection.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, size);
            ps.setInt(2, page * size);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                ingredients.add(new Ingredient(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        CategoryEnum.valueOf(rs.getString("category"))
                ));
            }
        }
        return ingredients;
    }

    public List<Ingredient> createIngredients(List<Ingredient> newIngredients) {

        String query = """
            INSERT INTO ingredient(name, price, category, id_dish)
            VALUES (?, ?, ?, ?)
            """;

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(query)) {
                for (Ingredient i : newIngredients) {
                    ps.setString(1, i.getName());
                    ps.setDouble(2, i.getPrice());
                    ps.setString(3, i.getCategory().name());

                    if (i.getDish() != null) {
                        ps.setInt(4, i.getDish().getId());
                    } else {
                        ps.setNull(4, Types.INTEGER);
                    }

                    ps.executeUpdate();
                }
                conn.commit();
                return newIngredients;

            } catch (Exception e) {
                conn.rollback();
                throw new RuntimeException("Insertion annulée", e);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    //  d) saveDish 
    public Dish saveDish(Dish dishToSave) throws SQLException {

        String insertDishSql = """
            INSERT INTO dish(name, dish_type, price)
            VALUES (?, ?, ?)
            RETURNING id
            """;

        String updateDishSql = """
            UPDATE dish
            SET name = ?, dish_type = ?, price = ?
            WHERE id = ?
            """;

        String clearIngredientsSql = """
            UPDATE ingredient
            SET id_dish = NULL
            WHERE id_dish = ?
            """;

        String attachIngredientSql = """
            UPDATE ingredient
            SET id_dish = ?
            WHERE id = ?
            """;

        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);

            if (dishToSave.getId() == 0) {
                try (PreparedStatement ps = connection.prepareStatement(insertDishSql)) {
                    ps.setString(1, dishToSave.getName());
                    ps.setString(2, dishToSave.getDishType().name());

                    if (dishToSave.getDishCost() != null) {
                        ps.setDouble(3, dishToSave.getDishCost());
                    } else {
                        ps.setNull(3, Types.NUMERIC);
                    }

                    ResultSet rs = ps.executeQuery();
                    rs.next();

                    int generatedId = rs.getInt("id");
                    dishToSave = new Dish(generatedId, dishToSave.getName(), dishToSave.getDishType(), null, dishToSave.getIngredients(), dishToSave.getDishCost());
                }
            } else {
                try (PreparedStatement ps = connection.prepareStatement(updateDishSql)) {
                    ps.setString(1, dishToSave.getName());
                    ps.setString(2, dishToSave.getDishType().name());
                    ps.setInt(3, dishToSave.getId());
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = connection.prepareStatement(clearIngredientsSql)) {
                    ps.setInt(1, dishToSave.getId());
                    ps.executeUpdate();
                }
            }

            if (dishToSave.getIngredients() != null) {
                try (PreparedStatement ps = connection.prepareStatement(attachIngredientSql)) {
                    for (Ingredient ingredient : dishToSave.getIngredients()) {
                        ps.setInt(1, dishToSave.getId());
                        ps.setInt(2, ingredient.getId());
                        ps.executeUpdate();
                    }
                }
            }

            connection.commit();
            return dishToSave;
        }
    }

    //  e) findDishsByIngredientName 
    public List<Dish> findDishsByIngredientName(String ingredientName) throws SQLException {

        String sql = """
            SELECT DISTINCT d.id, d.name, d.dish_type
            FROM dish d
            JOIN ingredient i ON d.id = i.id_dish
            WHERE LOWER(i.name) LIKE LOWER(?)
            """;

        List<Dish> dishes = new ArrayList<>();

        try (
                Connection connection = DBConnection.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, "%" + ingredientName + "%");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                dishes.add(new Dish(
                        rs.getInt("id"),
                        rs.getString("name"),
                        DishTypeEnum.valueOf(rs.getString("dish_type")),
                        null,
                        getIngredientsByDishId(rs.getInt("id"), connection),
                        null
                ));
            }
            return dishes;
        }
    }

    // f) findIngredientsByCriteria 
    public List<Ingredient> findIngredientsByCriteria(
            String ingredientName,
            CategoryEnum category,
            String dishName,
            int page,
            int size) throws SQLException {

        StringBuilder sql = new StringBuilder("""
            SELECT i.id, i.name, i.price, i.category
            FROM ingredient i
            LEFT JOIN dish d ON i.id_dish = d.id
            WHERE 1=1
            """);

        List<Object> params = new ArrayList<>();

        if (ingredientName != null) {
            sql.append(" AND LOWER(i.name) LIKE LOWER(?)");
            params.add("%" + ingredientName + "%");
        }

        if (category != null) {
            sql.append(" AND i.category = ?");
            params.add(category.name());
        }

        if (dishName != null) {
            sql.append(" AND LOWER(d.name) LIKE LOWER(?)");
            params.add("%" + dishName + "%");
        }

        sql.append(" ORDER BY i.id LIMIT ? OFFSET ?");
        params.add(size);
        params.add(page * size);

        List<Ingredient> ingredients = new ArrayList<>();

        try (
                Connection connection = DBConnection.getConnection(); PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                ingredients.add(new Ingredient(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        CategoryEnum.valueOf(rs.getString("category"))
                ));
            }
        }
        return ingredients;
    }

    public DBConnection getDbConnection() {
        return dbConnection;
    }
}
