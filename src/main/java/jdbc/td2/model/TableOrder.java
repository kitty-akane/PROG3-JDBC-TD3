package jdbc.td2.model;

import java.time.Instant;

public class TableOrder {
    private Table table;
    private Instant arrivalDatetime;
    private Instant departureDatetime;
    
    public Table getTable() {
        return table;
    }
        public void setTable(Table table) {
                this.table = table;
        }
        public Instant getArrivalDatetime() {
                return arrivalDatetime;
        }
        public void setArrivalDatetime(Instant arrivalDatetime) {
                this.arrivalDatetime = arrivalDatetime;
        }
        public Instant getDepartureDatetime() {
                return departureDatetime;
        }
        public void setDepartureDatetime(Instant departureDatetime) {
                this.departureDatetime = departureDatetime;
        }
}

