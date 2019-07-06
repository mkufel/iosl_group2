package common;

import java.util.Collection;

/**
 * Metro map.
 */
public class Map {
    // All lines that are part of the metro system
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
