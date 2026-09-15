import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
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

    public Employee(String name, 
                    String middleName, 
                    String surname, 
                    List<String> emails, 
                    LocalDate dateOfBirth, 
                    Role role, 
                    String pesel, 
                    List<Restaurant> restaurant) {
        setName(name);
        setMiddleName(middleName);
        setSurname(surname);
        validateEmail(emails);
        setEmails(emails);
        setDateOfBirth(dateOfBirth);
        setRole(role);
        setPeselNumber(pesel);
        assignToRestaurant(restaurant);

        extent.add(this);
    }

    //RESTAURANT LOGIC

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

    public void promoteIntern(Employee emp, Role promotedRole){
        if (role != Role.Manager) {
            throw new IllegalStateException("Only Manager may promote Employees.");
        }
        if (emp.role != Role.Intern) {
            throw new IllegalStateException("Only Interns may be promoted but provided: " + emp.getRole() + "[" + emp.getPeselNumber() + "].");
        }
        if (promotedRole == Role.Intern) {
            throw new IllegalStateException("Cannot promote to Intern.");
        }
        emp.role = promotedRole;
    }

    // RESERVATION LOGIC

    public static boolean canBeAssignedToReservation(Employee emp) {
        return emp != null && (emp.role == Role.Waiter || emp.role == Role.Intern);
    }

    public static void requireAssignableToReservation(Employee emp) {
        if (!canBeAssignedToReservation(emp)) {
            throw new IllegalArgumentException("Assigned employee must be a Waiter or an Intern.");
        }
    }

    void addReservation(Reservation newReservation){
        requireAssignableToReservation(this);
        if(!reservations.contains(newReservation)){
            reservations.add(newReservation);
        }
    }
    void removeReservation(Reservation reservation){
        reservations.remove(reservation);
    }
    
    public long getActiveReservationCount(){
        var reservationsCopy = List.copyOf(reservations);
        return reservationsCopy.stream().filter(Reservation::isActive).count();
    }

    public void changeReservationAssignedEmployee(Employee emp, Reservation reservation){
        if (getRole() != Role.Manager) {
            throw new IllegalArgumentException("Only Manager can alter Reservation info.");
        }
        requireAssignableToReservation(emp);
        reservation.getAssignedEmployee().reservations.remove(reservation);
        reservation.setAssignedEmployee(emp);
        emp.reservations.add(reservation);
    }

    // EMAIL LOGIC

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
     public void addEmail(String email){
        if (emails.size() >= 3){
            throw new
                    IllegalStateException("Employee may have three emails at most");
        }
        emails.add(email);
    }

    private void setEmails(List<String> emails){
        validateEmail(emails);
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


    // EXTENT LOGIC

    public static List<Employee> getExtent() {
        return List.copyOf(extent);
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

    public static void removeFromExtent(Employee emp){
        if(!emp.reservations.isEmpty()){
            throw new IllegalStateException("Cannot remove an Employee with assigned Reservations");
        }
        extent.remove(emp);
    }

    public static void showExtent(){
        extent.forEach(System.out::println);
    }

    public static void clearExtent(){
        extent.clear();
    }
    

    // FIND BY ROLE LOGIC
    public static List<Employee> findByRole(Role role){
        return extent.stream().filter(e -> e.role == role).toList();
    }

    public static List<Employee> findByRole(Role... roles){
        return extent.stream().filter(e -> List.of(roles).contains(e.role)).toList();
    }

    
    // GETTERS AND SETTERS

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
        validateString(name, "Name");
        this.name = name;
    }

    public void setMiddleName(String middleName) {
        if (middleName == null) {
            this.middleName = null;
            return;
        }
        if (middleName.isEmpty()) {
            throw new IllegalArgumentException("Middle name cannot be empty");
        }
        this.middleName = middleName;
    }

    public void setSurname(String surname) {
        validateString(surname, "Surname");
        this.surname = surname;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        if (dateOfBirth == null) {
            throw new IllegalArgumentException("Date of birth cannot be empty");
        }
        if (dateOfBirth.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Date of birth cannot be in the future");
        }
        this.dateOfBirth = dateOfBirth;
    }

    public void setRole(Role role) {
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }
        this.role = role;
    }
    
    public static double getMinSalary() {
        return minSalary;
    }

    public List<Reservation> getReservations() {
        var reservationCopy = List.copyOf(reservations);
        return reservationCopy;
    }

    public String getPeselNumber() {
        return peselNumber;
    }

    public void setPeselNumber(String peselNumber) {
        validateString(peselNumber, "Pesel number");
        this.peselNumber = peselNumber;
    }

    public List<Restaurant> getWorksInRestaurants() {
        var restaurantsCopy = List.copyOf(worksInRestaurants);
        return restaurantsCopy;
    }

     @Override
    public String toString() {
        return this.getClass().getSimpleName() + 
                " [name=" + name + 
                ", middleName=" + middleName 
                + ", surname=" + surname 
                + ", emails=" + emails
                + ", dateOfBirth=" + dateOfBirth 
                + ", role=" + role 
                + ", reservations=" + reservations 
                + ", works in " + worksInRestaurants.size() + " restaurants" + "]";
    }    
}
