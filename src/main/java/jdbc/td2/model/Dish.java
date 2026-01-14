package jdbc.td2.model;

import java.util.List;

public class Dish {

    private final int id;
    private final String name;
    private final DishTypeEnum dishType;
    private final Double price;
    private final List<Ingredient> ingredients;

    public Dish(int id, String name, DishTypeEnum dishType, Double price, List<Ingredient> ingredients, Double double1) {
        this.id = id;
        this.name = name;
        this.dishType = dishType;
        this.price = price;
        this.ingredients = ingredients;
    }

    public Double getDishCost() {
        return ingredients.stream()
                .mapToDouble(Ingredient::getPrice)
                .sum();
    }

    public Double grossMargin() {
        if(price == null){
            throw new IllegalStateException("Price is not defined for this dish.");
        }
        return price - getDishCost();
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public DishTypeEnum getDishType() {
        return dishType;
    }

    public List<Ingredient> getIngredients() {

        return ingredients;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Dish other = (Dish) obj;
        if (id != other.id) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (dishType != other.dishType) {
            return false;
        }
        if (ingredients == null) {
            if (other.ingredients != null) {
                return false;
            }
        } else if (!ingredients.equals(other.ingredients)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((dishType == null) ? 0 : dishType.hashCode());
        result = prime * result + ((ingredients == null) ? 0 : ingredients.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "Dish [id=" + id + ", name=" + name + ", dishType=" + dishType + ", price=" + price + ", ingredients=" + ingredients
                + ", getId()=" + getId() + ", getName()=" + getName()
                + ", getDishType()=" + getDishType() + ", getIngredients()=" + getIngredients() + "]";
    }
}
