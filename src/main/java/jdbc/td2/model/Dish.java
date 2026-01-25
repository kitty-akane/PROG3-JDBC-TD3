package jdbc.td2.model;

import java.util.List;
import java.util.Objects;

public class Dish {

    private Integer id;
    private String name;
    private DishTypeEnum dishType;
    private Double sellingPrice;
    private List<DishIngredient> dishIngredients;

    public Dish() {
    }

    public Dish(Integer id, String name, DishTypeEnum dishType) {
        this.id = id;
        this.name = name;
        this.dishType = dishType;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DishTypeEnum getDishType() {
        return dishType;
    }

    public void setDishType(DishTypeEnum dishType) {
        this.dishType = dishType;
    }

    public Double getPrice() {
        return sellingPrice;
    }

    public void setPrice(Double sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public List<DishIngredient> getIngredients() {
        return dishIngredients;
    }

    public void setIngredients(List<DishIngredient> dishIngredients) {
        this.dishIngredients = dishIngredients;
    }

    public Double getDishCost() {
        double total = 0;
        for (DishIngredient di : dishIngredients) {
            total += di.getIngredient().getPrice() * di.getQuantity();
        }
        return total;
    }

    public Double getGrossMargin() {
        if (sellingPrice == null) {
            return null;
        }
        return sellingPrice - getDishCost();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Dish dish = (Dish) o;
        return Objects.equals(id, dish.id) && Objects.equals(name, dish.name) && dishType == dish.dishType && Objects.equals(sellingPrice, dish.sellingPrice) && Objects.equals(dishIngredients, dish.dishIngredients);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, dishType, sellingPrice, dishIngredients);
    }

    @Override
    public String toString() {
        return "Dish{"
                + "id=" + id
                + ", name='" + name + '\''
                + ", dishType=" + dishType
                + ", sellingPrice=" + sellingPrice
                + ", dishIngredients=" + dishIngredients
                + '}';
    }
}