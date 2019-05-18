package common;

import java.util.Collection;

public class Map {
    private Collection<Line> lines;

    public Map(Collection<Line> lines) {
        this.lines = lines;
    }

    public Map() {
    }

    public Collection<Line> getLines() {
        return lines;
    }

    public void setLines(Collection<Line> lines) {
        this.lines = lines;
    }
}
