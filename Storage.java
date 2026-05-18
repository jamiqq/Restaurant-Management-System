import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Storage implements Serializable{

    private static List<Storage> extent = new ArrayList<>();
    private String streetName;
    private int buildingNumber;
    private int capacity;
    private double temperature;

    private List<Restaurant> restaurants = new ArrayList<>();

    public Storage(String streetName, int buildingNumber, int capacity, double temperature) {
        this.streetName = streetName;
        this.buildingNumber = buildingNumber;
        this.capacity = capacity;
        this.temperature = temperature;

        extent.add(this);
    }

    public void addRestaurant(Restaurant newRestaurant){
        if(!restaurants.contains(newRestaurant)){
            restaurants.add(newRestaurant);
        
            newRestaurant.addStorage(this);
        }
    }

    public void removeRestaurant(Restaurant restaurant){
        if (restaurants.contains(restaurant)) {
            restaurants.remove(restaurant);
        
            restaurant.removeStorage(this);
        }
    }

    public static void showExtent(){
        extent.forEach(System.out::println);
    }
    
    public static void clearExtent(){
        extent.clear();
    }

     public static List<Storage> getExtent() {
        return extent;
    }

    public String getStreetName() {
        return streetName;
    }

    public void setStreetName(String streetName) {
        this.streetName = streetName;
    }

    public int getBuildingNumber() {
        return buildingNumber;
    }

    public void setBuildingNumber(int buildingNumber) {
        this.buildingNumber = buildingNumber;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public List<Restaurant> getRestaurants() {
        return restaurants;
    }

    public void setRestaurants(List<Restaurant> restaurants) {
        this.restaurants = restaurants;
    }

    @Override
    public String toString() {
        String info = "Storage [streetName=" + streetName + ", buildingNumber=" + buildingNumber + ", capacity="
                + capacity
                + ", temperature=" + temperature + ", restaurants=";
        if (restaurants.isEmpty()) {
            info += "[]]";
        } else {
            for (Restaurant r : restaurants) {
                info += r.getStreetName() + "," + r.getBuildingNumber();
            }
            info += "]";
        }
        return info;
    }
}
