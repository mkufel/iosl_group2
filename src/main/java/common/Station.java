package common;

public class Station {
    private long id;

    private String name;

    private GeoCoords location;

    private int population;

    public Station(long id, String name, GeoCoords location, int population) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.population = population;
    }

    public Station() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
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
}
