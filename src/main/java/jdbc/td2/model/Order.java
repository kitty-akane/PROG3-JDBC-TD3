package jdbc.td2.model;

import java.time.Instant;
import java.util.List;

public class Order {

    private Integer id;
    private String reference;
    private Instant creationDatetime;
    private List<DishOrder> dishOrders;
    private TableOrder tableOrder;

    public Double getTotalAmountWithoutVAT() {
        return dishOrders.stream()
                .filter(dishOrder -> dishOrder.getDish().getPrice() != null) // Skip dishes without price
                .mapToDouble(dishOrder
                        -> dishOrder.getDish().getPrice() * dishOrder.getQuantity()
                )
                .sum();
    }

    public Double getTotalAmountWithVAT() {
        double vatRate = 0.20; // 20%
        return getTotalAmountWithoutVAT() * (1 + vatRate);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public Instant getCreationDatetime() {
        return creationDatetime;
    }

    public void setCreationDatetime(Instant creationDatetime) {
        this.creationDatetime = creationDatetime;
    }

    public List<DishOrder> getDishOrders() {
        return dishOrders;
    }

    public void setDishOrders(List<DishOrder> dishOrders) {
        this.dishOrders = dishOrders;
    }
    public TableOrder getTableOrder() {
        return tableOrder;
    }
    public void setTableOrder(TableOrder tableOrder) {
        this.tableOrder = tableOrder;
    }
}
