package jdbc.td2.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class DBConnection {

    public static Connection getConnection() throws SQLException {
        final String URL = "jdbc:postgresql://localhost:5432/mini_dish_db";
        final String USER = "mini_dish_db_manager";
        final String PASSWORD = "mini_dish_pass";
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
      public void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
