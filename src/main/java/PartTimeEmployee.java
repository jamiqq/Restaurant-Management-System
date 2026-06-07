import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PartTimeEmployee extends Employee{

    private static List<PartTimeEmployee> extent = new ArrayList<>();

    private double hourlyRate;
    private int weeklyWorkingHours;

    public PartTimeEmployee(String name, String middleName, String surname, List<String> emails, LocalDate dateOfBirth,
            Employee.Role role, String pesel, List<Restaurant> restaurant, double hourlyRate, int weeklyWorkingHours) {
        super(name, middleName, surname, emails, dateOfBirth, role, pesel, restaurant);
        this.hourlyRate = hourlyRate;
        this.weeklyWorkingHours = weeklyWorkingHours;

        extent.add(this);
    }

    private PartTimeEmployee(Employee prevEmp, double hourlyRate, int weeklyWorkingHours) {
        super(prevEmp.name, prevEmp.middleName, prevEmp.surname, prevEmp.emails, prevEmp.dateOfBirth, prevEmp.role, prevEmp.peselNumber, prevEmp.worksInRestaurants);
        this.hourlyRate = hourlyRate;
        this.weeklyWorkingHours = weeklyWorkingHours;
        
        Employee.removeFromExtent(prevEmp);
        FullTimeEmployee.removeFromFullTimeEmployeeExtent(prevEmp);
        extent.add(this);
    }

    public static PartTimeEmployee changeContractToPartTime(Employee prevEmployee, double hourlyRate, int weeklyWorkingHours){
        return new PartTimeEmployee(prevEmployee, hourlyRate, weeklyWorkingHours);
    }

    public double getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(double hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public int getWeeklyWorkingHours() {
        return weeklyWorkingHours;
    }

    public void setWeeklyWorkingHours(int weeklyWorkingHours) {
        this.weeklyWorkingHours = weeklyWorkingHours;
    }

    public double calculateHourlyPay(){
        return weeklyWorkingHours * hourlyRate;
    }
    
    @Override
    public double getSalary() {
        return super.getSalary() - 1000 + calculateHourlyPay();
    }

    public static List<PartTimeEmployee> getPartTimeEmployeeExtent() {
        return extent;
    }

    public static void showPartTimeEmployeeExtent(){
        extent.forEach(System.out::println);
    }

    public static void clearPartTimeEmployeeExtent(){
        extent.clear();
    }

    public static void removeFromPartTimeEmployee(Employee emp){
        extent.remove(emp);
    }

    @Override
    public String toString(){
        return super.toString() 
            +  ", hourlyRate=" + hourlyRate 
            + ", weeklyWorkingHours" + weeklyWorkingHours + " ]";
    }
}