package jdbc.td2.model;

import java.time.Instant;

public class StockMovement {
 private Integer id;
    private Double quantity;
    private UnitEnum unit;
    private Instant movementDate;

    public double getSignedQuantity() {
        return quantity;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Double getQuantity() { return quantity; }
    public void setQuantity(Double quantity) { this.quantity = quantity; }

    public UnitEnum getUnit() { return unit; }
    public void setUnit(UnitEnum unit) { this.unit = unit; }

    public Instant getMovementDate() { return movementDate; }
    public void setMovementDate(Instant movementDate) {
        this.movementDate = movementDate;
    }
}
