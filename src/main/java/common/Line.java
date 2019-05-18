package common;

import lombok.Data;

import java.util.List;

@Data
public class Line {
    private String name;

    private List<Station> stations;
}
