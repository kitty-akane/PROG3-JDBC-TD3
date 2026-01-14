package jdbc.td2;

import java.sql.Connection;

import jdbc.td2.dao.DBConnection;

public class ConnectionTest {
    public static void main(String[] args) {
        try (Connection conn = DBConnection.getConnection()) {
            System.out.println("Connected to database!");
            System.out.println("DB name: " + conn.getCatalog());
        } catch (Exception e) {
            System.out.println(" Connection failed");
            System.out.println("Error: " + e.getMessage());
        }
    }
}
