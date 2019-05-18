package common;

public class UserState {
    private int personId;

    private String stationStart;

    private String stationEnd;

    private String line;

    private double progress;

    private boolean data;

    public UserState(int personId,
                     String stationStart,
                     String stationEnd,
                     String line,
                     double progress,
                     boolean data) {
        this.personId = personId;
        this.stationStart = stationStart;
        this.stationEnd = stationEnd;
        this.line = line;
        this.progress = progress;
        this.data = data;
    }

    public int getPersonId() {
        return personId;
    }

    public void setPersonId(int personId) {
        this.personId = personId;
    }

    public String getStationStart() {
        return stationStart;
    }

    public void setStationStart(String stationStart) {
        this.stationStart = stationStart;
    }

    public String getStationEnd() {
        return stationEnd;
    }

    public void setStationEnd(String stationEnd) {
        this.stationEnd = stationEnd;
    }

    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }

    public double getProgress() {
        return progress;
    }

    public void setProgress(double progress) {
        this.progress = progress;
    }

    public boolean isData() {
        return data;
    }

    public void setData(boolean data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "UserState{" +
                "personId=" + personId +
                ", stationStart='" + stationStart + '\'' +
                ", stationEnd='" + stationEnd + '\'' +
                ", line='" + line + '\'' +
                ", progress=" + progress +
                ", data=" + data +
                '}';
    }
}
