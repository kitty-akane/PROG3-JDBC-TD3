package jdbc.td2.model;

public class DishIngredient {

    private Integer id;
    private Dish dish;
    private Ingredient ingredient;
    private Double quantityRequired;
    private UnitEnum unit;

    public DishIngredient() {
    }

    public DishIngredient(Integer id, Dish dish, Ingredient ingredient, Double quantityRequired, UnitEnum unit) {
        this.id = id;
        this.dish = dish;
        this.ingredient = ingredient;
        this.quantityRequired = quantityRequired;
        this.unit = unit;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Dish getDish() {
        return dish;
    }

    public void setDish(Dish dish) {
        this.dish = dish;
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    public void setIngredient(Ingredient ingredient) {
        this.ingredient = ingredient;
    }

    public Double getQuantity() {
        return quantityRequired;
    }

    public void setQuantity(Double quantityRequired) {
        this.quantityRequired = quantityRequired;
    }

    public UnitEnum getUnit() {
        return unit;
    }

    public void setUnit(UnitEnum unit) {
        this.unit = unit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DishIngredient that = (DishIngredient) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "DishIngredient{"
                + "id=" + id
                + ", dish=" + dish
                + ", ingredient=" + ingredient
                + ", quantityRequired=" + quantityRequired
                + ", unit=" + unit
                + '}';
    }
}
