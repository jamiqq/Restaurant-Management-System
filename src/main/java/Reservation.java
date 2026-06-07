import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

enum ReservationType {Reservation, CelebrationReservation, PrivateReservation};

public class Reservation implements Serializable{

    public enum Occasion {Birthday, Wedding, Corporate, Custom};

    public static final int DURATION_HOURS = 2;
    public static final int MAX_DURATION_HOURS = 6;
    public static final int WAITING_PERIOD = 15;
    public static final int MAX_ACTIVE_PER_EMP = 5;
    
    private static List<Reservation> extent = new ArrayList<>();

    private String guestName;
    private String guestPhoneNumber;
    private LocalDateTime dateTimeOfReservation;
    private LocalDateTime endTime;
    private Employee assignedEmployee;
    private Restaurant.Table assignedTable;
    private Occasion occasion;
    private String decorationDescription;
    private static double minimumSpend = 800;
    private boolean securityRequired;
    private boolean active = true;

    private EnumSet<ReservationType> reservationType = EnumSet.of(ReservationType.Reservation);
    
    public static Reservation makeReservation(Employee createdBy, String guestName, String guestPhoneNumber, LocalDateTime dateTime, Employee assignedEmployee, Restaurant.Table table, boolean isCelebration, boolean isPrivate){
        if (createdBy == null || createdBy.getRole() != Employee.Role.Manager) {
            throw new IllegalArgumentException("Only a Manager may create a reservation.");
        }
        if (guestName == null || guestName.isBlank()) {
            throw new IllegalArgumentException("Guest name cannot be blank.");
        }
        if (guestPhoneNumber == null || guestPhoneNumber.isBlank()) {
            throw new IllegalArgumentException("Guest phone number cannot be blank.");
        }
        if (dateTime == null) {
            throw new IllegalArgumentException("Reservation date time cannot be null.");
        }
        Employee.requireAssignableToReservation(assignedEmployee);

        return new Reservation(guestName, guestPhoneNumber, dateTime, assignedEmployee, table, isCelebration, isPrivate);
    }

    private Reservation(String name, String number, LocalDateTime time, Employee emp, Restaurant.Table table, boolean isCelebration, boolean isPrivate){
        this.guestName = name;
        this.guestPhoneNumber = number;
        this.dateTimeOfReservation = time;
        this.endTime = dateTimeOfReservation.plusHours(DURATION_HOURS);
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
    public void cancel(){
        assertActive();
        this.active = false;
        extent.remove(this);
        assignedEmployee.removeReservation(this);
        assignedTable.removeReservation(this);
    }

    public void stopReservation(Employee emp){
        assertActive();
        assertAssignedEmployee(emp);
        this.active = false;
    }


    public void prolongReservation(Employee emp, int extraHours){
        assertActive();
        assertAssignedEmployee(emp);

        if(extraHours <= 0){
            throw new IllegalArgumentException("Extension must be at least 1 hour.");
        }

        long currentDuration = java.time.Duration.between(dateTimeOfReservation, endTime).toHours();

        if (currentDuration + extraHours > MAX_DURATION_HOURS) {
            throw new IllegalStateException("Cannot extend: total duration would exceed " + MAX_DURATION_HOURS + " hours. " + "Remaining available time: " + (MAX_DURATION_HOURS - currentDuration) + " hours.");
        }

        LocalDateTime newEndDateTime = endTime.plusHours(extraHours);

        boolean tableConflict = assignedTable.isReservedAtExcluding(endTime, this);

        if (tableConflict) {
            Restaurant.Table swapTable = assignedTable.getRestaurant().findAvailableTables(endTime)
                                         .stream()
                                        .filter(t -> !t.equals(assignedTable))
                                        .findFirst()
                                        .orElseThrow(() -> new IllegalStateException("Cannot extend: table " + assignedTable.getTableNumber() + " is already booked and no other table is available."));
            
            assignedTable.removeReservation(this);
            swapTable.addReservation(this);
            assignedTable = swapTable;

            System.out.println("Table conflict - reservation moved to table " + assignedTable.getTableNumber());
        }

        if (assignedEmployee.getActiveReservationCount() >= MAX_ACTIVE_PER_EMP) {
            Employee newEmp = assignedTable.getRestaurant()
                              .getEmpQualifier().values().stream()
                              .filter(e -> Employee.canBeAssignedToReservation(e) && !e.equals(assignedEmployee))
                            .min(Comparator.comparingLong(Employee::getActiveReservationCount))
                            .orElseThrow(() -> new IllegalStateException("Cannot extend: assigned Employee is overloaded and no other eligible employee is available."));
            
            System.out.println("Employee overloaded - reassigning from " + assignedEmployee.getName() + "[" + assignedEmployee.getPeselNumber() + "]" + " to " + newEmp.getName() + "[" + newEmp.getPeselNumber() + "].");
            
            assignedEmployee.removeReservation(this);
            newEmp.addReservation(this);
            assignedEmployee = newEmp;
        }
        this.endTime = newEndDateTime;

        System.out.println("Reservation for " + guestName + " extended to " + endTime);
    }

    public void cancelNoGuest(Employee emp, LocalDateTime now){
        assertActive();
        assertAssignedEmployee(emp);

        LocalDateTime waitingExpiry = dateTimeOfReservation.plusMinutes(WAITING_PERIOD);

        if (now.isBefore(waitingExpiry)) {
            throw new IllegalStateException("Cannot cancel for no-Guest yet. Waiting period expires at " + waitingExpiry);
        }
        this.active = false;
        extent.remove(this);
        assignedEmployee.removeReservation(this);
        assignedTable.removeReservation(this);
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

    private void assertActive(){
        if (!active) {
            throw new IllegalStateException("Reservation is already inactive.");
        }
    }

    private void assertAssignedEmployee(Employee emp){
        if (!emp.equals(assignedEmployee)) {
            throw new IllegalArgumentException("Only assigned employee can act on this reservation");
        }
    }
    private void assignEmployee(Employee emp){
        if(emp == null){
            throw new IllegalArgumentException("Assigned employee can't be null");
        }
        Employee.requireAssignableToReservation(emp);
        if (emp.getActiveReservationCount() >= MAX_ACTIVE_PER_EMP) {
            throw new IllegalStateException("An employee has reached the max amount of active reservations. Select another employee.");
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

    //EXTENT LOGIC
    public static void showExtent()  { extent.forEach(System.out::println); }
    public static void clearExtent() { extent.clear(); }
    public static List<Reservation> getExtent() { return extent; }

    public static void writeExtent(ObjectOutputStream stream) throws IOException {
        stream.writeObject(extent);
    }

    public static void readExtent(ObjectInputStream stream)
            throws IOException, ClassNotFoundException {
        Object object = stream.readObject();
        if (object instanceof List<?>) {
            extent = new ArrayList<>((List<Reservation>) object);
        } else throw new IOException("Unable to read from extent.");
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
        Employee.requireAssignableToReservation(assignedEmployee);
        this.assignedEmployee = assignedEmployee;
    }

    public Restaurant.Table getAssignedTable() {
        return assignedTable;
    }

    public void setAssignedTable(Restaurant.Table assignedTable) {
        this.assignedTable = assignedTable;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public boolean isActive(){
        return active;
    }

    public String getShortInfo(){
        return getGuestName() + ", at " + getDateTimeOfReservation() + " at Table number: " + getAssignedTable().getTableNumber() + ". Employee -> " + getAssignedEmployee().getName() + " " + getAssignedEmployee().getSurname();
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
