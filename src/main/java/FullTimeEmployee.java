import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class FullTimeEmployee extends Employee{

    public enum Insurance{
        Basic,
        Upgraded,
        Family
    }

    private static List<FullTimeEmployee> extent = new ArrayList<>();
    private Insurance insuranceType;
    private static int paidVacationDays = 28;
    private int usedVacationDays;

    public FullTimeEmployee(String name, String middleName, String surname, List<String> emails, LocalDate dateOfBirth,
            Employee.Role role, String pesel, List<Restaurant> restaurant, FullTimeEmployee.Insurance insuranceType) {
        super(name, middleName, surname, emails, dateOfBirth, role, pesel, restaurant);
        this.insuranceType = insuranceType;
        usedVacationDays = 0;

        extent.add(this);
    }

    public FullTimeEmployee(Employee prevEmp, FullTimeEmployee.Insurance insuranceType) {
        super(prevEmp.name, prevEmp.middleName, prevEmp.surname, prevEmp.emails, prevEmp.dateOfBirth, prevEmp.role, prevEmp.peselNumber, prevEmp.worksInRestaurants);
        this.insuranceType = insuranceType;
        usedVacationDays = 0;

        Employee.removeFromExtent(prevEmp);
        PartTimeEmployee.removeFromPartTimeEmployee(prevEmp);
        extent.add(this);
    }

    public Insurance getInsuranceType() {
        return insuranceType;
    }

    public void setInsuranceType(Insurance insuranceType) {
        this.insuranceType = insuranceType;
    }

    public int getPaidVacationDays() {
        return paidVacationDays;
    }

    public int calculateUsedVacationDays(){
        return paidVacationDays - getUsedVacationDays();
    }

    @Override
    public double getSalary() {
        return super.getSalary() + (calculateUsedVacationDays() * 30.0);
    }

    public static List<FullTimeEmployee> getFullTimeEmployeeExtent() {
        return extent;
    }
    
    public int getUsedVacationDays() {
        return usedVacationDays;
    }

    public void setUsedVacationDays(int usedVacationDays) {
        this.usedVacationDays = usedVacationDays;
    }
    
    public static void showFullTimeEmployeeExtent(){
        extent.forEach(System.out::println);
    }
    
    public static void clearFullTimeEmployeeExtent(){
        extent.clear();
    }

    public static void removeFromFullTimeEmployeeExtent(Employee emp){
        extent.remove(emp);
    }

    @Override
    public String toString(){
        return super.toString() + 
            ", insuranceType=" + insuranceType + 
            ", usedVacationDays=" + usedVacationDays + " ]";
    }
}
