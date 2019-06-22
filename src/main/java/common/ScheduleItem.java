package common;

public class ScheduleItem {

    String line_name;
    String departure_time;
    Long nextStop_id;

    public ScheduleItem(String line_name, String departure_time, Long nextStop_id) {
        this.line_name = line_name;
        this.departure_time = departure_time;
        this.nextStop_id = nextStop_id;
    }

    public String getLine_name() {
        return line_name;
    }

    public String getDeparture_time() {
        return departure_time;
    }

    public Long getNextStop_id() {
        return nextStop_id;
    }
}
