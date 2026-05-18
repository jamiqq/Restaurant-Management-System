import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public abstract class Employee implements Serializable {

    public enum Role{
        Cleaner,
        Waiter,
        Manager,
        Intern
    }

    private static List<Employee> extent = new ArrayList<>();

    protected String name;

    protected String middleName;

    protected String surname;

    private final static double minSalary = 2000;

    protected List<String> emails = new ArrayList<>();

    protected LocalDate dateOfBirth;

    protected Role role;

    protected String peselNumber;

    private List<Reservation> reservations = new ArrayList<>();

    protected List<Restaurant> worksInRestaurants = new ArrayList<>();

    public Employee(String name, String middleName, String surname, List<String> emails, LocalDate dateOfBirth, Role role, String pesel, List<Restaurant> restaurant) {
        this.name = validateString(name, "Name");
        this.middleName = middleName;
        this.surname = validateString(surname, "Surname");
        validateEmail(emails);
        this.emails = new ArrayList<>(emails);
        this.dateOfBirth = Objects.requireNonNull(dateOfBirth, "Date of birth can't be null");
        this.role = Objects.requireNonNull(role, "Role can't be null");
        this.peselNumber = pesel;
        assignToRestaurant(restaurant);

        extent.add(this);
    }

    public void assignToRestaurant(List<Restaurant> restaurant){
        for(Restaurant r : restaurant){
            if(!worksInRestaurants.contains(r)){
                worksInRestaurants.add(r);
        
                r.addEmpQualifier(this);
            }
        }
    }
    
    public void reassignFromRestaurant(Restaurant restaurant){
        if (worksInRestaurants.contains(restaurant)) {
            worksInRestaurants.remove(restaurant);
        
            worksInRestaurants.remove(restaurant);
            restaurant.removerEmpQualifier(this);
        }
    }
    public void addReservation(Reservation newReservation){
        if (newReservation.getAssignedEmployee() != this) {
            System.err.println("An Employee has been already assigned -> " + newReservation.getShortInfo() + ". Only Manager can change assigned Employee.");
            return;
        }
        if(!reservations.contains(newReservation)){
            reservations.add(newReservation);
        
            newReservation.assignEmployee(this);
        }
    }
    public void cancelReservation(Reservation reservation, Restaurant.Table reservedTable){
        if (reservations.contains(reservation)) {
            reservations.remove(reservation);
        
            reservedTable.cancelReservation(this, reservation);
            reservation.cancelReservation(this, reservedTable);
        }
    }

    public void changeReservationAssignedEmployee(Employee emp, Reservation reservation){
        if (getRole() != Role.Manager) {
            throw new IllegalArgumentException("Only Manager can alter Reservation info.");
        }
        reservation.getAssignedEmployee().reservations.remove(reservation);
        reservation.setAssignedEmployee(emp);
        emp.reservations.add(reservation);
    }

    private static String validateString(String value, String field){
        if (value == null || value.isBlank()){
            throw new IllegalArgumentException(field + " can't be null");
        }
        return value;
    }
    private static void validateEmail(List<String> mails){
        if (mails == null || mails.isEmpty()){
            throw new IllegalArgumentException("There should be at least one email");
        }
        for(String mail : mails){
            if (mail == null || mail.isBlank()){
                throw new IllegalArgumentException("Email can't be empty");
            }
        }
    }
    public static List<Employee> getExtent() {
        return List.copyOf(extent);
    }

    public String getName() {
        return name;
    }

    public Optional<String> getMiddleName() {
        return Optional.ofNullable(middleName);
    }

    public String getSurname() {
        return surname;
    }

    public List<String> getEmails() {
        return List.copyOf(emails);
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public double getSalary(){
        return switch (role){
            case Intern -> minSalary;
            case Cleaner -> minSalary + 500;
            case Waiter -> minSalary + 1000;
            case Manager -> minSalary + 1500;
        };
    }

    public Role getRole() {
        return role;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public void addEmail(String email){
        if (emails.size() >= 3){
            throw new
                    IllegalStateException("Employee may have three emails at most");
        }
        if (email == null || email.isBlank()){
            throw new IllegalArgumentException("Email can't be empty");
        }
        emails.add(email);
    }

    public void addEmail(String... emails){
        for (String mail : emails) {
            addEmail(mail);
        }
    }
    public void removeEmail(String email){
        if (emails.size() == 1){
            throw new IllegalStateException("Employee must have at least one email");
        }
        if (email == null || email.isBlank()){
            throw new IllegalArgumentException("Email can't be empty");
        }
        emails.remove(email);
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public static List<Employee> findByRole(Role role){
        return extent.stream().filter(e -> e.role == role).toList();
    }

    public static List<Employee> findByRole(Role... roles){
        return extent.stream().filter(e -> List.of(roles).contains(e.role)).toList();
    }
    public static void writeExtent(ObjectOutputStream stream) throws IOException{
        stream.writeObject(extent);
    }

    public static void readExtent(ObjectInputStream stream) throws IOException, ClassNotFoundException{
        Object object = stream.readObject();
        if (object instanceof List<?>) {
            extent = new ArrayList<>((List<Employee>) object);
        } else throw new IOException("Something went wrong...");
    }

    public static void showExtent(){
        extent.forEach(System.out::println);
    }
    public static void clearExtent(){
        extent.clear();
    }

    public static double getMinSalary() {
        return minSalary;
    }
    public void setEmails(List<String> emails) {
        this.emails = emails;
    }
    public List<Reservation> getReservations() {
        return reservations;
    }
    public void setReservations(List<Reservation> reservations) {
        this.reservations = reservations;
    }
    
    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " [name=" + name + ", middleName=" + middleName + ", surname=" + surname + ", emails=" + emails
                + ", dateOfBirth=" + dateOfBirth + ", role=" + role + ", reservations=" + reservations + ", works in " + worksInRestaurants.size() + " restaurants" + "]";
    }

    public String getPeselNumber() {
        return peselNumber;
    }

    public void setPeselNumber(String peselNumber) {
        this.peselNumber = peselNumber;
    }

    public List<Restaurant> getWorksInRestaurants() {
        return worksInRestaurants;
    }

    public static void removeFromExtent(Employee emp){
        extent.remove(emp);
    }
}
