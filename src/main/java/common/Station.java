package common;

public class Station {
    private int id;

    private String name;

    private GeoCoords location;

    private int population;

    public Station(int id, String name, GeoCoords location, int population) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.population = population;
    }

    public Station() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
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
