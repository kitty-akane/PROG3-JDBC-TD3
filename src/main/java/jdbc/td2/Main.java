package jdbc.td2;
import jdbc.td2.dao.DataRetriever;
import jdbc.td2.model.*;

import java.util.List;

public class Main {

    public static void main(String[] args) {

        DataRetriever dataRetriever = new DataRetriever();

        System.out.println("========= FIND DISH BY ID =========");
        Dish dish = dataRetriever.findDishById(4);
        System.out.println(dish);

        System.out.println("\n========= INGREDIENTS =========");
        for (Ingredient ingredient : dish.getIngredients()) {
            System.out.println(
                    ingredient.getName()
                    + " | price=" + ingredient.getPrice()
                    + " | quantity=" + ingredient.getQuantity()
            );
        }

        System.out.println("\n========= DISH COST =========");
        double cost = dish.getIngredients().stream()
                .mapToDouble(i -> i.getPrice() * i.getQuantity())
                .sum();
        System.out.println("Dish cost = " + cost);

        System.out.println("\n========= GROSS MARGIN =========");
        if (dish.getPrice() != null) {
            System.out.println("Gross margin = " + (dish.getPrice() - cost));
        } else {
            System.out.println("No price defined for this dish.");
        }

        System.out.println("\n========= CREATE INGREDIENT =========");
        Ingredient fromage = new Ingredient();
        fromage.setName("Fromage");
        fromage.setCategory(CategoryEnum.DAIRY);
        fromage.setPrice(1200.0);
        fromage.setQuantity(0.2);

        List<Ingredient> created = dataRetriever.createIngredients(List.of(fromage));
        created.forEach(System.out::println);

        System.out.println("\n========= ATTACH INGREDIENT TO DISH =========");
        dish.setIngredients(List.of(created.get(0)));
        Dish updatedDish = dataRetriever.saveDish(dish);
        System.out.println(updatedDish);

        System.out.println("\n========= DONE =========");
    }
}
