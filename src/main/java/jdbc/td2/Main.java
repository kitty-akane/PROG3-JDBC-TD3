package jdbc.td2;

import java.sql.SQLException;
import jdbc.td2.dao.DataRetriever;
import jdbc.td2.model.Dish;
import jdbc.td2.model.Ingredient;

public class Main {
    public static void main(String[] args) {
        DataRetriever dataRetriever = new DataRetriever();
        try{
            Dish dish = dataRetriever.findDishById(1);
            if(dish != null){
                System.out.println("Dish: " + dish.getName());
                System.out.println("Ingredients:");
                for(Ingredient ingredient : dish.getIngredients()){
                    System.out.println("- " + ingredient.getName() + " (Price: " + ingredient.getPrice() + ")");
                }
                System.out.println("Dish Cost: " + dish.getDishCost());
                try {
                    System.out.println("Gross Margin: " + dish.grossMargin());
                } catch (IllegalStateException e) {
                    System.out.println("Cannot calculate gross margin: " + e.getMessage());
                }
            } else {
                System.out.println("Dish not found.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
