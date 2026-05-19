
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class Main {

    private static final String pathToFile = "extent.bin";

    public static void main(String[] args) throws Exception {

        // --- Storages ---
        Storage storage1 = new Storage("Warehouse Lane", 5, 3000, 15.2);
        Storage storage2 = new Storage("Depot Street", 12, 8000, -4.0);
        Storage storage3 = new Storage("Supply Road", 3, 4700, 9.3);

        // --- Restaurants ---
        Restaurant r1 = new Restaurant("Marszalkowska", 14, storage1);
        Restaurant r2 = new Restaurant("Nowy Swiat", 37, storage2);
        Restaurant r3 = new Restaurant("Chmielna", 8, storage3);

        Restaurant.BarTable r1_t1 = r1.addBarTable(1, "High Stool", Restaurant.Table.Location.Inside,
                Restaurant.Table.Section.Modern, false, 2);
        Restaurant.BarTable r1_t2 = r1.addBarTable(2, "High Stool", Restaurant.Table.Location.Outside, null, true, 5);
        Restaurant.FamilyTable r1_t3 = r1.addFamilyTable(3, "Round", Restaurant.Table.Location.Inside,
                Restaurant.Table.Section.Family, false, true, true);
        Restaurant.FamilyTable r1_t4 = r1.addFamilyTable(4, "Square", Restaurant.Table.Location.Outside, null, true,
                false, true);
        Restaurant.FamilyTable r1_t5 = r1.addFamilyTable(5, "Oval", Restaurant.Table.Location.Inside,
                Restaurant.Table.Section.Retro, false, true, false);
        Restaurant.FamilyBarTable r1_t6 = r1.addFamilyBarTable(6, "Corner", Restaurant.Table.Location.Inside,
                Restaurant.Table.Section.Family, false, 3, true, true);
        Restaurant.FamilyBarTable r1_t7 = r1.addFamilyBarTable(7, "Corner", Restaurant.Table.Location.Outside, null,
                true, 6, false, true);
        Restaurant.BarTable r1_t8 = r1.addBarTable(8, "Tall Round", Restaurant.Table.Location.Inside,
                Restaurant.Table.Section.Modern, false, 1);

        Restaurant.BarTable r2_t1 = r2.addBarTable(1, "Straight", Restaurant.Table.Location.Inside,
                Restaurant.Table.Section.Retro, false, 4);
        Restaurant.BarTable r2_t2 = r2.addBarTable(2, "Straight", Restaurant.Table.Location.Outside, null, false, 7);
        Restaurant.FamilyTable r2_t3 = r2.addFamilyTable(3, "Round", Restaurant.Table.Location.Inside,
                Restaurant.Table.Section.Family, false, false, true);
        Restaurant.FamilyTable r2_t4 = r2.addFamilyTable(4, "Rectangle", Restaurant.Table.Location.Outside, null, true,
                true, false);
        Restaurant.FamilyBarTable r2_t5 = r2.addFamilyBarTable(5, "L-Shape", Restaurant.Table.Location.Inside,
                Restaurant.Table.Section.Modern, false, 2, true, true);
        Restaurant.FamilyBarTable r2_t6 = r2.addFamilyBarTable(6, "L-Shape", Restaurant.Table.Location.Outside, null,
                true, 8, false, false);
        Restaurant.FamilyTable r2_t7 = r2.addFamilyTable(7, "Square", Restaurant.Table.Location.Inside,
                Restaurant.Table.Section.Retro, false, true, true);
        Restaurant.BarTable r2_t8 = r2.addBarTable(8, "Round", Restaurant.Table.Location.Inside,
                Restaurant.Table.Section.Family, false, 3);

        Restaurant.BarTable r3_t1 = r3.addBarTable(1, "High Stool", Restaurant.Table.Location.Outside, null, true, 10);
        Restaurant.BarTable r3_t2 = r3.addBarTable(2, "High Stool", Restaurant.Table.Location.Inside,
                Restaurant.Table.Section.Modern, false, 2);
        Restaurant.FamilyTable r3_t3 = r3.addFamilyTable(3, "Oval", Restaurant.Table.Location.Inside,
                Restaurant.Table.Section.Family, false, true, true);
        Restaurant.FamilyTable r3_t4 = r3.addFamilyTable(4, "Round", Restaurant.Table.Location.Outside, null, false,
                true, false);
        Restaurant.FamilyBarTable r3_t5 = r3.addFamilyBarTable(5, "Corner", Restaurant.Table.Location.Inside,
                Restaurant.Table.Section.Retro, false, 5, true, false);
        Restaurant.FamilyBarTable r3_t6 = r3.addFamilyBarTable(6, "Corner", Restaurant.Table.Location.Outside, null,
                true, 9, true, true);
        Restaurant.FamilyTable r3_t7 = r3.addFamilyTable(7, "Rectangle", Restaurant.Table.Location.Inside,
                Restaurant.Table.Section.Modern, false, false, true);
        Restaurant.BarTable r3_t8 = r3.addBarTable(8, "Tall Round", Restaurant.Table.Location.Inside,
                Restaurant.Table.Section.Family, false, 1);

                
        // -----------------------------------------------
        // Restaurant 1 — Marszalkowska 14
        // -----------------------------------------------
        FullTimeEmployee emp1_1 = new FullTimeEmployee(
                "Anna", "Maria", "Kowalska",
                List.of("anna.kowalska@mail.com"),
                LocalDate.of(1990, 3, 15),
                Employee.Role.Manager,
                "90031512345",
                List.of(r1),
                FullTimeEmployee.Insurance.Family);

        FullTimeEmployee emp1_2 = new FullTimeEmployee(
                "Piotr", null, "Nowak",
                List.of("piotr.nowak@mail.com"),
                LocalDate.of(1995, 7, 22),
                Employee.Role.Waiter,
                "95072267890",
                List.of(r1),
                FullTimeEmployee.Insurance.Upgraded);

        PartTimeEmployee emp1_3 = new PartTimeEmployee(
                "Kamil", null, "Wiśniewski",
                List.of("kamil.wisniewski@mail.com"),
                LocalDate.of(2000, 11, 5),
                Employee.Role.Waiter,
                "00110534521",
                List.of(r1),
                25.0, 20);

        PartTimeEmployee emp1_4 = new PartTimeEmployee(
                "Zuzanna", "Ewa", "Dąbrowska",
                List.of("zuzanna.dabrowska@mail.com"),
                LocalDate.of(2002, 1, 18),
                Employee.Role.Intern,
                "02011812378",
                List.of(r1),
                18.0, 15);

        FullTimeEmployee emp1_5 = new FullTimeEmployee(
                "Marek", null, "Zielinski",
                List.of("marek.zielinski@mail.com"),
                LocalDate.of(1988, 6, 30),
                Employee.Role.Cleaner,
                "88063054321",
                List.of(r1),
                FullTimeEmployee.Insurance.Basic);

        // -----------------------------------------------
        // Restaurant 2 — Nowy Swiat 37
        // -----------------------------------------------
        FullTimeEmployee emp2_1 = new FullTimeEmployee(
                "Katarzyna", "Anna", "Lewandowska",
                List.of("katarzyna.lewandowska@mail.com"),
                LocalDate.of(1985, 9, 12),
                Employee.Role.Manager,
                "85091298765",
                List.of(r2),
                FullTimeEmployee.Insurance.Family);

        FullTimeEmployee emp2_2 = new FullTimeEmployee(
                "Tomasz", null, "Wójcik",
                List.of("tomasz.wojcik@mail.com"),
                LocalDate.of(1993, 4, 8),
                Employee.Role.Waiter,
                "93040811223",
                List.of(r2),
                FullTimeEmployee.Insurance.Upgraded);

        PartTimeEmployee emp2_3 = new PartTimeEmployee(
                "Natalia", "Julia", "Kaminska",
                List.of("natalia.kaminska@mail.com"),
                LocalDate.of(2001, 8, 25),
                Employee.Role.Waiter,
                "01082544556",
                List.of(r2),
                25.0, 24);

        PartTimeEmployee emp2_4 = new PartTimeEmployee(
                "Bartosz", null, "Kowalczyk",
                List.of("bartosz.kowalczyk@mail.com"),
                LocalDate.of(2003, 3, 2),
                Employee.Role.Intern,
                "03030277889",
                List.of(r2),
                18.0, 12);

        FullTimeEmployee emp2_5 = new FullTimeEmployee(
                "Agnieszka", null, "Szymanska",
                List.of("agnieszka.szymanska@mail.com"),
                LocalDate.of(1979, 12, 19),
                Employee.Role.Cleaner,
                "79121933210",
                List.of(r2),
                FullTimeEmployee.Insurance.Basic);

        // -----------------------------------------------
        // Restaurant 3 — Chmielna 8
        // -----------------------------------------------
        FullTimeEmployee emp3_1 = new FullTimeEmployee(
                "Michal", "Jan", "Pawlak",
                List.of("michal.pawlak@mail.com"),
                LocalDate.of(1982, 5, 27),
                Employee.Role.Manager,
                "82052722334",
                List.of(r3),
                FullTimeEmployee.Insurance.Family);

        FullTimeEmployee emp3_2 = new FullTimeEmployee(
                "Monika", null, "Adamczyk",
                List.of("monika.adamczyk@mail.com"),
                LocalDate.of(1997, 10, 14),
                Employee.Role.Waiter,
                "97101455667",
                List.of(r3),
                FullTimeEmployee.Insurance.Upgraded);

        PartTimeEmployee emp3_3 = new PartTimeEmployee(
                "Lukasz", null, "Mazur",
                List.of("lukasz.mazur@mail.com"),
                LocalDate.of(1999, 2, 3),
                Employee.Role.Waiter,
                "99020388990",
                List.of(r3),
                27.0, 20);

        PartTimeEmployee emp3_4 = new PartTimeEmployee(
                "Weronika", "Maria", "Krawczyk",
                List.of("weronika.krawczyk@mail.com"),
                LocalDate.of(2004, 7, 9),
                Employee.Role.Intern,
                "04070911122",
                List.of(r3),
                18.0, 10);

        FullTimeEmployee emp3_5 = new FullTimeEmployee(
                "Krzysztof", null, "Wojciechowski",
                List.of("krzysztof.wojciechowski@mail.com"),
                LocalDate.of(1975, 11, 1),
                Employee.Role.Cleaner,
                "75110144321",
                List.of(r3),
                FullTimeEmployee.Insurance.Basic);

        // -----------------------------------------------
        // Reservations — Restaurant 1 (Marszalkowska 14)
        // -----------------------------------------------

        // Plain reservation
        Reservation res1_1 = new Reservation(
                "Jan Kowalski", "600111222",
                LocalDateTime.of(2026, 6, 1, 18, 0),
                emp1_2, r1_t1,
                false, false);

        // Celebration reservation — birthday
        Reservation res1_2 = new Reservation(
                "Marta Nowak", "601222333",
                LocalDateTime.of(2026, 6, 3, 19, 30),
                emp1_2, r1_t3,
                true, false);
        res1_2.setOccasion(Reservation.Occasion.Birthday);
        res1_2.setDecorationDescription("Balloon arch, gold theme");

        // Private reservation
        Reservation res1_3 = new Reservation(
                "Adam Wieczorek", "602333444",
                LocalDateTime.of(2026, 6, 7, 20, 0),
                emp1_3, r1_t6,
                false, true);
        res1_3.setSecurityRequired(true);

        // Celebration + Private (both flags)
        Reservation res1_4 = new Reservation(
                "Ewa Brzezinska", "603444555",
                LocalDateTime.of(2026, 6, 14, 17, 0),
                emp1_2, r1_t5,
                true, true);
        res1_4.setOccasion(Reservation.Occasion.Wedding);
        res1_4.setDecorationDescription("White flowers, candle centerpieces");
        res1_4.setSecurityRequired(true);

        // Plain reservation
        Reservation res1_5 = new Reservation(
                "Tomasz Grabowski", "604555666",
                LocalDateTime.of(2026, 6, 20, 12, 30),
                emp1_3, r1_t2,
                false, false);

        // -----------------------------------------------
        // Reservations — Restaurant 2 (Nowy Swiat 37)
        // -----------------------------------------------

        // Plain reservation
        Reservation res2_1 = new Reservation(
                "Aleksandra Piotrowska", "605666777",
                LocalDateTime.of(2026, 6, 2, 13, 0),
                emp2_2, r2_t1,
                false, false);

        // Celebration reservation — corporate
        Reservation res2_2 = new Reservation(
                "Firma XYZ Sp. z o.o.", "606777888",
                LocalDateTime.of(2026, 6, 5, 18, 0),
                emp2_2, r2_t5,
                true, false);
        res2_2.setOccasion(Reservation.Occasion.Corporate);
        res2_2.setDecorationDescription("Company banners, formal layout");

        // Private reservation
        Reservation res2_3 = new Reservation(
                "Robert Majewski", "607888999",
                LocalDateTime.of(2026, 6, 10, 19, 0),
                emp2_3, r2_t7,
                false, true);
        res2_3.setSecurityRequired(false);

        // Celebration reservation — custom
        Reservation res2_4 = new Reservation(
                "Zofia Kruk", "608999000",
                LocalDateTime.of(2026, 6, 15, 20, 30),
                emp2_2, r2_t3,
                true, false);
        res2_4.setOccasion(Reservation.Occasion.Custom);
        res2_4.setDecorationDescription("Vintage floral, pastel palette");

        // Plain reservation
        Reservation res2_5 = new Reservation(
                "Pawel Wisniewski", "609000111",
                LocalDateTime.of(2026, 6, 22, 11, 0),
                emp2_3, r2_t2,
                false, false);

        // -----------------------------------------------
        // Reservations — Restaurant 3 (Chmielna 8)
        // -----------------------------------------------

        // Plain reservation
        Reservation res3_1 = new Reservation(
                "Dorota Kubiak", "610111222",
                LocalDateTime.of(2026, 6, 3, 14, 0),
                emp3_2, r3_t2,
                false, false);

        // Celebration reservation — birthday
        Reservation res3_2 = new Reservation(
                "Marcin Ostrowski", "611222333",
                LocalDateTime.of(2026, 6, 8, 18, 30),
                emp3_2, r3_t3,
                true, false);
        res3_2.setOccasion(Reservation.Occasion.Birthday);
        res3_2.setDecorationDescription("Superhero theme, red and blue");

        // Private + Celebration
        Reservation res3_3 = new Reservation(
                "Justyna Kaminska", "612333444",
                LocalDateTime.of(2026, 6, 12, 20, 0),
                emp3_3, r3_t5,
                true, true);
        res3_3.setOccasion(Reservation.Occasion.Wedding);
        res3_3.setDecorationDescription("Rustic wood, dried flowers");
        res3_3.setSecurityRequired(true);

        // Private reservation
        Reservation res3_4 = new Reservation(
                "Grzegorz Wolski", "613444555",
                LocalDateTime.of(2026, 6, 18, 19, 30),
                emp3_3, r3_t6,
                false, true);
        res3_4.setSecurityRequired(false);

        // Plain reservation
        Reservation res3_5 = new Reservation(
                "Sylwia Dabrowska", "614555666",
                LocalDateTime.of(2026, 6, 25, 12, 0),
                emp3_2, r3_t1,
                false, false);
        

        // -----------------------------------------------
        // Persistence — write all extents to file
        // -----------------------------------------------

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(pathToFile))) {
            Restaurant.writeExtent(oos);
            Employee.writeExtent(oos);
            Reservation.writeExtent(oos);
            Restaurant.Table.writeExtent(oos);
            System.out.println("\n=== Extents written to " + pathToFile + " ===");
        } catch (IOException e) {
            System.err.println("Failed to write extents: " + e.getMessage());
        }

        // -----------------------------------------------
        // Persistence — clear everything and read back
        // -----------------------------------------------

        Restaurant.clearExtent();
        Employee.clearExtent();
        Reservation.clearExtent();
        Restaurant.Table.clearExtent();

        System.out.println("\n=== Extents cleared — reading back from file ===");

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(pathToFile))) {
            Restaurant.readExtent(ois);
            Employee.readExtent(ois);
            Reservation.readExtent(ois);
            Restaurant.Table.readExtent(ois);
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Failed to read extents: " + e.getMessage());
        }

        // -----------------------------------------------
        // Verify restored state
        // -----------------------------------------------

        System.out.println("\n=== Restaurants after restore ===");
        Restaurant.showExtent();

        System.out.println("\n=== Employees after restore ===");
        Employee.showExtent();

        System.out.println("\n=== Active reservations after restore ===");
        Reservation.showExtent();
    }   
}