import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;


public class Restaurant implements Serializable{

    private static List<Restaurant> extent = new ArrayList<>();

    private String streetName;
    private int buildingNumber;

    private List<Storage> storages = new ArrayList<>();

    private Set<Table> tables = new HashSet<>();

    private Map<String, Employee> empQualifier = new TreeMap<>();

    public Restaurant(String streetName, int buildingNumber, Storage storage){
        this.streetName = streetName;
        this.buildingNumber = buildingNumber;
        this.addStorage(storage);
        extent.add(this);
    }

    public void addEmpQualifier(Employee emp){
        if (!empQualifier.containsKey(emp.getPeselNumber())) {
            empQualifier.put(emp.getPeselNumber(), emp);
        
            emp.assignToRestaurant(List.of(this));
        }
    }

    public void removerEmpQualifier(Employee emp){
        if (empQualifier.containsKey(emp.getPeselNumber())) {
            empQualifier.remove(emp.getPeselNumber());
        
            empQualifier.remove(emp.getPeselNumber());
            emp.reassignFromRestaurant(this);
        }
    }

    public Employee findEmployeeByPeselNumber(String pesel){
        if(!empQualifier.containsKey(pesel)){
            throw new IllegalArgumentException("Unable to find an Employee...");
        }
        return empQualifier.get(pesel);
    }

    public void addStorage(Storage newStorage){
        if(!storages.contains(newStorage)){
            storages.add(newStorage);
        
            newStorage.addRestaurant(this);
        }
    }

    public void removeStorage(Storage storage){
        if(storages.contains(storage)){
            if (storages.size() <= 1) {
                throw new IllegalArgumentException("There should be at least one storage"); 
            }else {
                storages.remove(storage);
                storage.removeRestaurant(this);
            }
        }
    }
    public static void showExtent(){
        extent.forEach(System.out::println);
    }

    public static void clearExtent(){
        extent.clear();
    }
    
    @Override
    public String toString() {
        String info = "Restaurant [streetName=" + streetName + ", buildingNumber=" + buildingNumber + ", " + "employees=" + empQualifier.keySet() + " storages=";
        if (storages.isEmpty()) {
            info += "[]]";
        }else{
            for(Storage s : storages){
                info += s.getStreetName() + "," + s.getBuildingNumber() + "; ";
            }
            info += ", tables=";
        }
        if(tables.isEmpty()){
            info += "[]]";
        }else{
            for(Restaurant.Table t : tables){
                info += t.getTableNumber() + ", ";
            }
            info += "]";
        }
        return info;
    }

    public static List<Restaurant> getExtent() {
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

    public List<Storage> getStorages() {
        return storages;
    }

    public void setStorages(List<Storage> storages) {
        this.storages = storages;
    }

    public Map<String, Employee> getEmpQualifier() {
        return empQualifier;
    }

    public Set<Table> getTables() {
        return tables;
    }

    public BarTable addBarTable(int tableNumber, String tableType, Table.Location location, Table.Section section, boolean hasSunshade, int distance) throws Exception{
        if (location == Table.Location.Inside) {
            BarTable table = new BarTable(tableNumber, tableType, location, section,  false, distance);
            tables.add(table);
            return table;
        }else if (location == Table.Location.Outside) {
            BarTable table = new Restaurant.BarTable(tableNumber, tableType, location, null, hasSunshade, distance);
            tables.add(table);
            return table;
        }else throw new Exception("Something went wrong...");
    }

    public FamilyTable addFamilyTable(int tableNumber, String tableType, Table.Location location, Table.Section section, boolean hasSunshade, boolean isExtendable, boolean hasKidChair) throws Exception{
        if (location == Table.Location.Inside) {
            FamilyTable table = new FamilyTable(tableNumber, tableType, location, section, false, isExtendable, hasKidChair);
            tables.add(table);
            return table;
        }else if(location == Table.Location.Outside){
            FamilyTable table = new FamilyTable(tableNumber, tableType, location, null, hasSunshade, isExtendable, hasKidChair);
            tables.add(table);
            return table;
        }else throw new Exception("Something went wrong");
    }

    public FamilyBarTable addFamilyBarTable(int tableNumber, String tableType, Table.Location location, Table.Section section, boolean hasSunshade, int distance, boolean isExtendable, boolean hasKidChair) throws Exception{
        if (location == Table.Location.Inside) {
            FamilyBarTable table = new FamilyBarTable(tableNumber, tableType, location, section, hasSunshade, distance, isExtendable, hasKidChair);
            tables.add(table);
            return table;
        }else if (location == Table.Location.Outside) {
            FamilyBarTable table = new FamilyBarTable(tableNumber, tableType, location, section, hasSunshade, distance, isExtendable, hasKidChair);
            tables.add(table);
            return table;
        }else throw new Exception("Something went wrong");
    }
    
    public static void removeRestaurant(Restaurant restaurant){
        if(!extent.contains(restaurant)){
            throw new IllegalArgumentException("Unable to find a restaurant...");
        }
        restaurant.removeAllTables();
        extent.remove(restaurant);
    }
    
    public void removeTable(Restaurant.Table table){
        if (!tables.contains(table)) {
            throw new IllegalArgumentException("Unable to find a table...");
        }
        tables.remove(table);
        Restaurant.Table.extent.remove(table);
    }

    public void removeAllTables(){
        tables.clear();
        Restaurant.Table.clearExtent();
    }

    public static void writeExtent(ObjectOutputStream stream) throws IOException{
        stream.writeObject(extent);
    }
    public static void readExtent(ObjectInputStream stream) throws IOException, ClassNotFoundException{
        Object object = stream.readObject();
        if (object instanceof List<?>) {
            extent = new ArrayList<>((List<Restaurant>) object);
        }else throw new IOException("Unable to read from extent");
    }

    public abstract class Table implements Serializable{
        
        public enum Location {Inside, Outside}

        public enum Section {Family, Modern, Retro}

        private static List<Table> extent = new ArrayList<>();

        private int tableNumber;
        private String tableType;

        private List<Reservation> reservations = new ArrayList<>();

        private final Location location;

        private Section section;

        private boolean hasSunshade;

        private Table(int tableNumber, String tableType, Location location, Section section, boolean hasSunshade){
        this.tableNumber = tableNumber;
        this.tableType = tableType;

        this.location = location;
        if (location == Location.Inside) {
            this.section = section;
        }else this.section = null;
        if (location == Location.Outside) {
            this.hasSunshade = hasSunshade;
        }else this.hasSunshade = false;

        extent.add(this);
        }

        public Location getLocation(){
            return this.location;
        }

        public Section getSection() throws Exception{
            if (this.location == Location.Inside) {
                return this.section;
            }
            throw new Exception("The table is not inside");
        }

        public void setSection(Section section) throws Exception{
            if (this.location == Location.Inside) {
                this.section = section;
            }else throw new Exception("The table is not inside");
        }

        public boolean getHasSunshade() throws Exception{
            if (this.location == Location.Outside) {
                return this.hasSunshade;
            }
            throw new Exception("The table is not outside");
        }

        public void setHasSunshade(boolean sunshade) throws Exception{
            if (this.location == Location.Outside) {
                this.hasSunshade = sunshade;
            }else throw new Exception("The table is not outside");
        }
        
        public Restaurant getRestaurant(){
            return Restaurant.this;
        }
        
        public void addReservation(Reservation newReservation){
            if(!reservations.contains(newReservation) && newReservation.getAssignedTable() == this){
                reservations.add(newReservation);
        
                newReservation.assignTable(this);
            }
            
        }

        public void cancelReservation(Employee emp, Reservation reservation){
            if(reservations.contains(reservation)){
                reservations.remove(reservation);

                emp.cancelReservation(reservation, this);
                reservation.cancelReservation(emp, this);
            }
        }

        public static void showExtent(){
            extent.forEach(System.out::println);
        }

        public static void clearExtent(){
           extent.clear();
        }

        public static List<Table> getExtent() {
            return extent;
        }

        public int getTableNumber() {
            return tableNumber;
        }

        public void setTableNumber(int tableNumber) {
            this.tableNumber = tableNumber;
        }

        public String getTableType() {
            return tableType;
        }

        public void setTableType(String tableType) {
            this.tableType = tableType;
        }

        public List<Reservation> getReservations() {
            return reservations;
        }


        @Override
        public String toString() {
            return "Table [tableNumber=" + tableNumber + ", tableType=" + tableType + ", reservations=" + reservations
                    + "]";
        }

        public static void writeExtent(ObjectOutputStream stream) throws IOException{
            stream.writeObject(extent);
        }

        public static void readExtent(ObjectInputStream stream) throws IOException, ClassNotFoundException{
            Object object = stream.readObject();
            if (object instanceof List<?>) {
                extent = new ArrayList<>((List<Table>) object);
            }else throw new IOException("Unable to read from extent");
        }
    }

    public class BarTable extends Table{

        private static List<BarTable> extent = new ArrayList<>();

        private int distanceToBar;

        private BarTable(int tableNumber, String tableType, Location location, Section section, boolean hasSunshade, int distanceToBar){
        super(tableNumber, tableType, location, section, hasSunshade);

        this.distanceToBar = distanceToBar;

        extent.add(this);
        }

        public int getDistanceToBar(){
            return distanceToBar;
        }

        public void setDistanceToBar(int distance){
            this.distanceToBar = distance;
        }

        public static List<BarTable> getBarTableExtent(){
            return List.copyOf(extent);
        }

        public static void showBarTableExtent(){
            extent.forEach(System.out::println);
        }

        public static void clearBarTableExtent(){
            extent.clear();
        }

        @Override
        public String toString() {
            return "BarTable [ " + super.toString() + ", distanceToBar=" + distanceToBar + "]";
        }

    }
    
    public class FamilyTable extends Table implements IFamilyTable{

        private static List<FamilyTable> extent = new ArrayList<>();

        private boolean isExtendable;

        private boolean hasKidChair;

        private FamilyTable(int tableNumber, String tableType, Location location, Section section, boolean hasSunshade, boolean isExtendable, boolean hasKidChair){
        super(tableNumber, tableType, location, section, hasSunshade);

        this.isExtendable = isExtendable;
        this.hasKidChair = hasKidChair;

        extent.add(this);
        }

        @Override
        public boolean getIsExtendable(){
            return isExtendable;
        }
        @Override
        public void setIsExtendable(boolean isExtendable) {
            this.isExtendable = isExtendable;
        }

        public static List<FamilyTable> getFamilyTableExtent() {
            return List.copyOf(extent);
        }

        public static void showFamilyTableExtent(){
            extent.forEach(System.out::println);
        }

        public static void clearFamilyTableExtent(){
            extent.clear();
        }

        @Override
        public boolean getHasKidChair() {
            return hasKidChair;
        }

        @Override
        public void setHasKidChair(boolean hasKidChair) {
            this.hasKidChair = hasKidChair;
        }

        @Override
        public double calculateKidDiscount(int numOfKids){
            if (numOfKids == 0) {
                return 0.0;
            }
            switch (numOfKids) {
                case 1:
                    return 3.0;
                case 2:
                    return 5.0;
                case 3:
                    return 7.0;
                default:
                    return 9.0;
            }
        }

        @Override
        public String toString() {
            return "FamilyTable [" + super.toString() + ", isExtendable=" + isExtendable + ", hasKidChair=" + hasKidChair + "]";
        }
    }

    public class FamilyBarTable extends BarTable implements IFamilyTable{

        private static List<FamilyBarTable> extent = new ArrayList<>();

        private boolean isExtendable;
        private boolean hasKidChair;

        public FamilyBarTable(int tableNumber, String tableType, Location location, Section section, boolean hasSunshade, int distance, boolean isExtendable, boolean hasKidChair){
            super(tableNumber, tableType, location, section, hasSunshade, distance);
            this.isExtendable = isExtendable;
            this.hasKidChair = hasKidChair;

            extent.add(this);
        }

        public static List<FamilyBarTable> getFamilyBarTableExtent() {
            return List.copyOf(extent);
        }

        @Override
        public boolean getIsExtendable() {
            return isExtendable;
        }

        @Override
        public void setIsExtendable(boolean isExtendable) {
            this.isExtendable = isExtendable;
        }

        @Override
        public boolean getHasKidChair() {
            return hasKidChair;
        }
        
        @Override
        public void setHasKidChair(boolean hasKidChair) {
            this.hasKidChair = hasKidChair;
        }

        @Override
        public double calculateKidDiscount(int numOfKids){
            if (numOfKids == 0) {
                return 0.0;
            }
            switch (numOfKids) {
                case 1:
                    return 3.0;
                case 2:
                    return 5.0;
                case 3:
                    return 7.0;
                default:
                    return 9.0;
            }
        }

        @Override
        public String toString() {
            return "FamilyBarTable [" + super.toString() + ", isExtendable=" + isExtendable + ", hasKidChair=" + hasKidChair + "]";
        }
    }
}