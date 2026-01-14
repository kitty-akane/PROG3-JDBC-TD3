package jdbc.td2.model;

public class Ingredient {

    static double getCost(Ingredient ingredient) {
        return ingredient.getPrice();
    }

    private int id;
    private String name;
    private Double price;
    private CategoryEnum category;
    private Dish dish;

    public Ingredient(int id, String name, Double price, CategoryEnum category) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.category = category;
    }

    public String getDishName() {
        return dish != null ? dish.getName() : null;
    }

    public void setDish(Dish dish) {
        this.dish = dish;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Double getPrice() {
        return price;
    }

    public CategoryEnum getCategory() {
        return category;
    }

    public Dish getDish() {
        return dish;
    }
}
