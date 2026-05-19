import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

enum ReservationType {Reservation, CelebrationReservation, PrivateReservation};

public class Reservation implements Serializable{

    public enum Occasion {Birthday, Wedding, Corporate, Custom};

    protected static final int durationHours = 2;
    
    private static List<Reservation> extent = new ArrayList<>();

    private String guestName;
    private String guestPhoneNumber;
    private LocalDateTime dateTimeOfReservation;
    private Employee assignedEmployee;
    private Restaurant.Table assignedTable;
    private Occasion occasion;
    private String decorationDescription;
    private static double minimumSpend = 800;
    private boolean securityRequired;
    private boolean active = true;

    private EnumSet<ReservationType> reservationType = EnumSet.of(ReservationType.Reservation);

    public Reservation(String name, String number, LocalDateTime time, Employee emp, Restaurant.Table table, boolean isCelebration, boolean isPrivate){
        this.guestName = name;
        this.guestPhoneNumber = number;
        this.dateTimeOfReservation = time;
        assignEmployee(emp);
        assignTable(table);

        if (isCelebration) {
            reservationType.add(ReservationType.CelebrationReservation);
        }
        if (isPrivate) {
            reservationType.add(ReservationType.PrivateReservation);
        }

        extent.add(this);
    }


    // RESERVATION LIFECYCLE LOGIC 
    public void cancel(Employee emp){
        if (!active) {
            throw new IllegalStateException("Reservation is already inactive.");
        }
        if (!emp.equals(assignedEmployee)) {
            throw new IllegalArgumentException("Only the assigned employee can cancel the reservation.");
        }
        this.active = false;
        extent.remove(this);
        assignedEmployee.removeReservation(this);
        assignedTable.removeReservation(this);
    }

    public void stopReservation(Employee emp){
        if (!active) {
            throw new IllegalStateException("Reservation is already inactive.");
        }
        if (!emp.equals(assignedEmployee)) {
            throw new IllegalArgumentException("Only the assigned employee can stop the reservation.");
        }
        this.active = false;
    }

    // OVERLAPPING FIELDS GETTERS AND SETTERS
    public Occasion hasOccasion() throws Exception{
        if (reservationType.contains(ReservationType.CelebrationReservation)) {
            return occasion;
        }
        throw new Exception("The reservation is not Celebration");
    }

    public void setOccasion(Occasion occasion) throws Exception{
        if (reservationType.contains(ReservationType.CelebrationReservation)) {
            this.occasion = occasion;
        }else throw new Exception("The reservation is not Celebration"); 
    }

    public String hasDecorationDescription() throws Exception{
        if (reservationType.contains(ReservationType.CelebrationReservation)) {
            return decorationDescription;
        }
        throw new Exception("The reservation is not Celebration");
    }

    public void setDecorationDescription(String desc) throws Exception{
        if(reservationType.contains(ReservationType.CelebrationReservation)){
            this.decorationDescription = desc;
        }else throw new Exception("The reservation is not Celebration");
    }

    public double hasMinimumSpend() throws Exception{
        if (reservationType.contains(ReservationType.PrivateReservation)) {
            return minimumSpend;
        }
        throw new Exception("The reservation is not Private");
    }

    public boolean hasSecurityRequired() throws Exception{
        if (reservationType.contains(ReservationType.PrivateReservation)) {
            return securityRequired;
        }
        throw new Exception("The reservation is not Private");
    }

    public void setSecurityRequired(boolean sec) throws Exception{
        if (reservationType.contains(ReservationType.PrivateReservation)) {
            this.securityRequired = sec;
        }else throw new Exception("The reservation is not Private");
    }

    
    // AUXILIARY FUNCTIONS 
    private void assignEmployee(Employee emp){
        if(emp == null){
            throw new IllegalArgumentException("Assigned employee can't be null");
        }
        if (emp.role != Employee.Role.Waiter && emp.role != Employee.Role.Intern) {
            throw new IllegalArgumentException("Assigned employee must be a Waiter or an Intern.");
        }
        assignedEmployee = emp;
        assignedEmployee.addReservation(this);
    }

    private void assignTable(Restaurant.Table table){
        if(table == null){
            throw new IllegalArgumentException("Assigned table can't be null");
        }
        assignedTable = table;
        assignedTable.addReservation(this);
    }

    public LocalDateTime getEndTime(){
        return dateTimeOfReservation.plusHours(durationHours);
    }

    public boolean isActive(){
        return active;
    }

    public String getShortInfo(){
        return getGuestName() + ", at " + getDateTimeOfReservation() + " at Table number: " + getAssignedTable().getTableNumber() + ". Employee -> " + getAssignedEmployee().getName() + " " + getAssignedEmployee().getSurname();
    }

    
    // EXTENT LOGIC

    public static void readExtent(ObjectInputStream stream) throws IOException, ClassNotFoundException{
        Object object = stream.readObject();
        if (object instanceof List<?>) {
            extent = new ArrayList<>((List<Reservation>) object);
        }else throw new IOException("Unable to read from extent");
    }

    public static void writeExtent(ObjectOutputStream stream) throws IOException{
        stream.writeObject(extent);
    }

    public static void showExtent(){
        extent.forEach(System.out::println);
    }

    public static void clearExtent(){
        extent.clear();
    }

    public static List<Reservation> getExtent() {
        return extent;
    }


    // GETTERS AND SETTERS

    public String getGuestName() {
        return guestName;
    }

    public void setGuestName(String guestName) {
        this.guestName = guestName;
    }

    public String getGuestPhoneNumber() {
        return guestPhoneNumber;
    }

    public void setGuestPhoneNumber(String guestPhoneNumber) {
        this.guestPhoneNumber = guestPhoneNumber;
    }

    public LocalDateTime getDateTimeOfReservation() {
        return dateTimeOfReservation;
    }

    public void setDateTimeOfReservation(LocalDateTime dateTimeOfReservation) {
        this.dateTimeOfReservation = dateTimeOfReservation;
    }

    public Employee getAssignedEmployee() {
        return assignedEmployee;
    }

    public void setAssignedEmployee(Employee assignedEmployee) {
        this.assignedEmployee = assignedEmployee;
    }

    public Restaurant.Table getAssignedTable() {
        return assignedTable;
    }

    public void setAssignedTable(Restaurant.Table assignedTable) {
        this.assignedTable = assignedTable;
    }

    @Override
    public String toString() {
        return "Reservation " + reservationType
                + " [guestName=" + guestName 
                + ", guestPhoneNumber=" + guestPhoneNumber
                + ", dateTimeOfReservation=" + dateTimeOfReservation 
                + ", assignedEmployee=" + assignedEmployee.getName()
                + ", assignedTable=" + assignedTable.getTableNumber() + "]";
    }
}
