package common;

/**
 * Metro station.
 *
 * Described by it's id, name and geographical position.
 */
public class Station {
    private Long id;

    private String name;

    private GeoCoords location;

    private int population;

    public Station(Long id, String name, GeoCoords location, int population) {
        this(id);

        this.name = name;
        this.location = location;
        this.population = population;
    }

    public Station(Long id) {
        this();

        this.id = id;
    }

    public Station() {}

    public Station(Long id, String name, GeoCoords location) {
        this(id);
        this.name = name;
        this.location = location;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public GeoCoords getLocation() {
        return location;
    }

    public void setLocation(GeoCoords location) {
        this.location = location;
    }

    public int getPopulation() {
        return population;
    }

    public void setPopulation(int population) {
        this.population = population;
    }

    public boolean equals(Object object) {
        if(object instanceof Station && ((Station)object).getId().equals(this.id)) {
            return true;
        } else {
            return false;
        }
    }
}
