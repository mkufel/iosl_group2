package common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * State of a single agent.
 *
 * Stores position of the agent and whether the agent has the data.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserState {

    @JsonProperty("personId")
    private int personId;

    @JsonProperty("stationStart")
    private Long stationStart;

    @JsonProperty("stationEnd")
    private Long stationEnd;

    @JsonProperty("line")
    private String line;

    @JsonProperty("progress")
    private double progress;

    @JsonProperty("data")
    private boolean data;

    public UserState() {}

    public UserState(int personId,
                     Long stationStart,
                     Long stationEnd,
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

    public Long getStationStart() {
        return stationStart;
    }

    public void setStationStart(Long stationStart) {
        this.stationStart = stationStart;
    }

    public Long getStationEnd() {
        return stationEnd;
    }

    public void setStationEnd(Long stationEnd) {
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

    public boolean hasData() {
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

    public String getEdgeId() {
            return stationStart + "_" + stationEnd;
    }
}
