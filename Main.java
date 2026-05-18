import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;


public class Main {
    public static void main(String[] args) {
        
        
        System.out.println("Abstract class and polymorphic method calls");
        Storage s1 = new Storage("aaaa", 67, 3000, 21);
        Restaurant r1 = new Restaurant("aaa", 32, s1);
        Employee fullEmp = new FullTimeEmployee("Sanya", null, "Mykytchenko", List.of("sashamyk22@gmail.com"), LocalDate.of(2007, 7, 22), Employee.Role.Manager, "2207270428114", List.of(r1), FullTimeEmployee.Insurance.Basic);
        Employee partEmp = new PartTimeEmployee("Sanya", null, "Martyniuk", List.of("sashavmart@gmail.com"), LocalDate.of(2006, 6, 1), Employee.Role.Cleaner, "3213123213", List.of(r1), 5, 15);

        System.out.println(fullEmp.getSalary() + " salary of full time employee");
        System.out.println(partEmp.getSalary() + " salary of part time employee");
        
        System.out.println("Overlapping inheritance");
        Restaurant.BarTable t1 = null;
        try {
            t1 = r1.addBarTable(7, "Cool", Restaurant.Table.Location.Outside, null, false, 30);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Reservation res1 = new Reservation("Guest", "guestnumber", LocalDateTime.now(), partEmp, t1, true, true);
        Reservation re2 = new Reservation("Guest2", "guestnumber2", LocalDateTime.of(LocalDate.now(), LocalTime.of(18, 30)), partEmp, t1, true, false);

        System.out.println(res1.toString());
        System.out.println(re2.toString());

        System.out.println();
        System.out.println("Multi-aspect inheritance");
        Restaurant.FamilyTable f1 = null;
        try{
            f1 = r1.addFamilyTable(3, "Super", Restaurant.Table.Location.Inside, Restaurant.Table.Section.Retro, true, false, true);
        }catch(Exception e){
            e.printStackTrace();
        }
        System.out.println(t1.toString());
        System.out.println(f1.toString());

        System.out.println();
        System.out.println("Multi inheritance");
        Restaurant.FamilyBarTable fbt1 = null;
        try{
            fbt1 = r1.addFamilyBarTable(9, "Normal", Restaurant.Table.Location.Inside, Restaurant.Table.Section.Family, false, 5, false, true);
        }catch(Exception e){
            e.printStackTrace();
        }
        System.out.println(fbt1.toString());

        Restaurant.Table.showExtent();
        
        Employee.clearExtent();
        FullTimeEmployee.clearFullTimeEmployeeExtent();
        PartTimeEmployee.clearPartTimeEmployeeExtent();
        
        System.out.println();
        System.out.println("Dynamic inheritance");
        Employee emp1 = new FullTimeEmployee("Hawkin", null, "Pheonix", List.of("hawk@gmail.com"), LocalDate.of(1983, 2, 28), Employee.Role.Waiter, "3213123213", List.of(r1), FullTimeEmployee.Insurance.Upgraded);
        Employee.showExtent();
        FullTimeEmployee.showExtent();
        emp1 = new PartTimeEmployee(emp1, 7.0, 20);
        Employee.showExtent();
        PartTimeEmployee.showPartTimeEmployeeExtent();
        FullTimeEmployee.showFullTimeEmployeeExtent();
    }
}