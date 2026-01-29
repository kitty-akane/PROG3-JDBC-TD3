package jdbc.td2.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Table {

    private Integer id;
    private Integer number;
    private List<TableOrder> orders = new ArrayList<>();

    public boolean isAvailable(Instant arrival, Instant departure) {
        for (TableOrder to : orders) {
            boolean overlap
                    = !departure.isBefore(to.getArrivalDatetime())
                    && !arrival.isAfter(to.getDepartureDatetime());
            if (overlap) {
                return false;
            }
        }
        return true;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public List<TableOrder> getOrders() {
        return orders;
    }

    public void setOrders(List<TableOrder> orders) {
        this.orders = orders;
    }
}
