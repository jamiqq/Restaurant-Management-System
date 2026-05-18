import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Restaurant Reservation System")
class ReservationSystemTest {

    // -------------------------------------------------------------------
    // Shared fixtures — rebuilt fresh before every test
    // -------------------------------------------------------------------

    private Storage    storage;
    private Restaurant restaurant;

    private Restaurant.BarTable    barTable;
    private Restaurant.FamilyTable familyTable;

    private FullTimeEmployee manager;
    private FullTimeEmployee waiter;
    private PartTimeEmployee intern;

    @BeforeEach
    void setUp() throws Exception {
        // clear all extents so tests are fully isolated
        Restaurant.clearExtent();
        Employee.clearExtent();
        Reservation.clearExtent();

        storage    = new Storage("Depot Street", 1, 1000, 1.2);
        restaurant = new Restaurant("Marszalkowska", 10, storage);

        barTable    = restaurant.addBarTable(1, "High Stool",
                Restaurant.Table.Location.Inside, Restaurant.Table.Section.Modern, false, 2);
        familyTable = restaurant.addFamilyTable(2, "Round",
                Restaurant.Table.Location.Inside, Restaurant.Table.Section.Family, false, true, true);

        manager = new FullTimeEmployee(
                "Anna", null, "Kowalska",
                List.of("anna@mail.com"),
                LocalDate.of(1985, 3, 10),
                Employee.Role.Manager,
                "85031012345",
                List.of(restaurant),
                FullTimeEmployee.Insurance.Family);

        waiter = new FullTimeEmployee(
                "Piotr", null, "Nowak",
                List.of("piotr@mail.com"),
                LocalDate.of(1995, 7, 22),
                Employee.Role.Waiter,
                "95072267890",
                List.of(restaurant),
                FullTimeEmployee.Insurance.Upgraded);

        intern = new PartTimeEmployee(
                "Kamil", null, "Wisniewski",
                List.of("kamil@mail.com"),
                LocalDate.of(2002, 5, 5),
                Employee.Role.Intern,
                "02050534521",
                List.of(restaurant),
                20.0, 15);
    }

    // ===================================================================
    // 1. Table — time slot availability
    // ===================================================================

    @Nested
    @DisplayName("Table time slot availability")
    class TableAvailability {

        @Test
        @DisplayName("Fresh table is available at any time")
        void freshTableIsAvailable() {
            assertFalse(barTable.isReservedAt(LocalDateTime.of(2026, 7, 1, 18, 0)));
        }

        @Test
        @DisplayName("Table is unavailable during an active reservation window")
        void tableUnavailableDuringActiveReservation() {
            LocalDateTime start = LocalDateTime.of(2026, 7, 1, 18, 0);
            new Reservation("Jan K.", "600111222", start, waiter, barTable, false, false);

            // inside the 18:00–20:00 window
            assertTrue(barTable.isReservedAt(LocalDateTime.of(2026, 7, 1, 19, 0)));
        }

        @Test
        @DisplayName("Table is available exactly when the previous reservation ends")
        void tableAvailableAtExactEndTime() {
            LocalDateTime start = LocalDateTime.of(2026, 7, 1, 18, 0);
            new Reservation("Jan K.", "600111222", start, waiter, barTable, false, false);

            // 20:00 == end time — no overlap (strict isBefore / isAfter)
            assertFalse(barTable.isReservedAt(LocalDateTime.of(2026, 7, 1, 20, 0)));
        }

        @Test
        @DisplayName("Overlapping start is rejected")
        void overlappingStartIsRejected() {
            LocalDateTime first = LocalDateTime.of(2026, 7, 1, 18, 0);
            new Reservation("Jan K.", "600111222", first, waiter, barTable, false, false);

            // 19:00 overlaps with 18:00–20:00
            assertThrows(IllegalStateException.class, () ->
                    new Reservation("Anna N.", "601222333",
                            LocalDateTime.of(2026, 7, 1, 19, 0), intern, barTable, false, false));
        }

        @Test
        @DisplayName("Back-to-back reservations on the same table are permitted")
        void backToBackReservationsPermitted() {
            LocalDateTime first  = LocalDateTime.of(2026, 7, 1, 18, 0);
            LocalDateTime second = LocalDateTime.of(2026, 7, 1, 20, 0); // exactly at end
            new Reservation("Jan K.",  "600111222", first,  waiter, barTable, false, false);

            assertDoesNotThrow(() ->
                    new Reservation("Anna N.", "601222333", second, intern, barTable, false, false));
        }

        @Test
        @DisplayName("Table becomes available again after reservation is stopped")
        void tableAvailableAfterStop() {
            LocalDateTime start = LocalDateTime.of(2026, 7, 1, 18, 0);
            Reservation res = new Reservation("Jan K.", "600111222", start, waiter, barTable, false, false);

            res.stopReservation();

            assertFalse(barTable.isReservedAt(LocalDateTime.of(2026, 7, 1, 19, 0)));
        }

        @Test
        @DisplayName("Table becomes available again after reservation is cancelled")
        void tableAvailableAfterCancel() {
            LocalDateTime start = LocalDateTime.of(2026, 7, 1, 18, 0);
            Reservation res = new Reservation("Jan K.", "600111222", start, waiter, barTable, false, false);

            res.cancel();

            assertFalse(barTable.isReservedAt(LocalDateTime.of(2026, 7, 1, 19, 0)));
        }
    }

    // ===================================================================
    // 2. Reservation — creation and assignment
    // ===================================================================

    @Nested
    @DisplayName("Reservation creation")
    class ReservationCreation {

        @Test
        @DisplayName("Valid reservation is added to extent")
        void validReservationAddedToExtent() {
            new Reservation("Jan K.", "600111222",
                    LocalDateTime.of(2026, 7, 1, 18, 0), waiter, barTable, false, false);

            assertEquals(1, Reservation.getExtent().size());
        }

        @Test
        @DisplayName("Manager cannot be assigned to a reservation")
        void managerCannotBeAssigned() {
            assertThrows(IllegalArgumentException.class, () ->
                    new Reservation("Jan K.", "600111222",
                            LocalDateTime.of(2026, 7, 1, 18, 0), manager, barTable, false, false));
        }

        @Test
        @DisplayName("Null employee is rejected")
        void nullEmployeeRejected() {
            assertThrows(IllegalArgumentException.class, () ->
                    new Reservation("Jan K.", "600111222",
                            LocalDateTime.of(2026, 7, 1, 18, 0), null, barTable, false, false));
        }

        @Test
        @DisplayName("Null table is rejected")
        void nullTableRejected() {
            assertThrows(IllegalArgumentException.class, () ->
                    new Reservation("Jan K.", "600111222",
                            LocalDateTime.of(2026, 7, 1, 18, 0), waiter, null, false, false));
        }

        @Test
        @DisplayName("Reservation is linked to employee and table")
        void reservationLinkedToAssociates() {
            Reservation res = new Reservation("Jan K.", "600111222",
                    LocalDateTime.of(2026, 7, 1, 18, 0), waiter, barTable, false, false);

            assertEquals(waiter,    res.getAssignedEmployee());
            assertEquals(barTable,  res.getAssignedTable());
            assertTrue(waiter.getReservations().contains(res));
            assertTrue(barTable.getReservations().contains(res));
        }

        @Test
        @DisplayName("End time is exactly DURATION_HOURS after start")
        void endTimeIsCorrect() {
            LocalDateTime start = LocalDateTime.of(2026, 7, 1, 18, 0);
            Reservation res = new Reservation("Jan K.", "600111222", start, waiter, barTable, false, false);

            assertEquals(start.plusHours(Reservation.durationHours), res.getEndTime());
        }
    }

    // ===================================================================
    // 3. Reservation — celebration type
    // ===================================================================

    @Nested
    @DisplayName("Celebration reservation")
    class CelebrationReservation {

        @Test
        @DisplayName("Occasion and decoration can be set on a celebration reservation")
        void occasionAndDecorationSet() throws Exception {
            Reservation res = new Reservation("Ewa B.", "603444555",
                    LocalDateTime.of(2026, 7, 5, 18, 0), waiter, barTable, true, false);

            res.setOccasion(Reservation.Occasion.Birthday);
            res.setDecorationDescription("Balloon arch");

            assertEquals(Reservation.Occasion.Birthday, res.hasOccasion());
            assertEquals("Balloon arch", res.hasDecorationDescription());
        }

        @Test
        @DisplayName("Setting occasion on a plain reservation throws")
        void occasionOnPlainReservationThrows() {
            Reservation res = new Reservation("Jan K.", "600111222",
                    LocalDateTime.of(2026, 7, 1, 18, 0), waiter, barTable, false, false);

            assertThrows(Exception.class, () -> res.setOccasion(Reservation.Occasion.Birthday));
        }
    }

    // ===================================================================
    // 4. Reservation — private type
    // ===================================================================

    @Nested
    @DisplayName("Private reservation")
    class PrivateReservation {

        @Test
        @DisplayName("Security flag can be set on a private reservation")
        void securityFlagSet() throws Exception {
            Reservation res = new Reservation("Adam W.", "602333444",
                    LocalDateTime.of(2026, 7, 6, 20, 0), intern, familyTable, false, true);

            res.setSecurityRequired(true);

            assertTrue(res.hasSecurityRequired());
        }

        @Test
        @DisplayName("Minimum spend is accessible on a private reservation")
        void minimumSpendAccessible() throws Exception {
            Reservation res = new Reservation("Adam W.", "602333444",
                    LocalDateTime.of(2026, 7, 6, 20, 0), intern, familyTable, false, true);

            assertTrue(res.hasMinimumSpend() > 0);
        }

        @Test
        @DisplayName("Security flag on a plain reservation throws")
        void securityOnPlainReservationThrows() {
            Reservation res = new Reservation("Jan K.", "600111222",
                    LocalDateTime.of(2026, 7, 1, 18, 0), waiter, barTable, false, false);

            assertThrows(Exception.class, () -> res.setSecurityRequired(true));
        }
    }

    // ===================================================================
    // 5. Reservation — cancel
    // ===================================================================

    @Nested
    @DisplayName("Reservation cancellation")
    class ReservationCancellation {

        @Test
        @DisplayName("Cancelled reservation is removed from Reservation extent")
        void cancelRemovesFromExtent() {
            Reservation res = new Reservation("Jan K.", "600111222",
                    LocalDateTime.of(2026, 7, 1, 18, 0), waiter, barTable, false, false);

            res.cancel();

            assertFalse(Reservation.getExtent().contains(res));
        }

        @Test
        @DisplayName("Cancelled reservation is removed from employee list")
        void cancelRemovesFromEmployee() {
            Reservation res = new Reservation("Jan K.", "600111222",
                    LocalDateTime.of(2026, 7, 1, 18, 0), waiter, barTable, false, false);

            res.cancel();

            assertFalse(waiter.getReservations().contains(res));
        }

        @Test
        @DisplayName("Cancelled reservation is removed from table list")
        void cancelRemovesFromTable() {
            Reservation res = new Reservation("Jan K.", "600111222",
                    LocalDateTime.of(2026, 7, 1, 18, 0), waiter, barTable, false, false);

            res.cancel();

            assertFalse(barTable.getReservations().contains(res));
        }

        @Test
        @DisplayName("Cancelling an already inactive reservation throws")
        void cancelInactiveThrows() {
            Reservation res = new Reservation("Jan K.", "600111222",
                    LocalDateTime.of(2026, 7, 1, 18, 0), waiter, barTable, false, false);

            res.cancel();

            assertThrows(IllegalStateException.class, res::cancel);
        }
    }

    // ===================================================================
    // 6. Reservation — stop
    // ===================================================================

    @Nested
    @DisplayName("Reservation stop")
    class ReservationStop {

        @Test
        @DisplayName("Stopped reservation remains in Reservation extent")
        void stopKeepsInExtent() {
            Reservation res = new Reservation("Jan K.", "600111222",
                    LocalDateTime.of(2026, 7, 1, 18, 0), waiter, barTable, false, false);

            res.stopReservation();

            assertTrue(Reservation.getExtent().contains(res));
        }

        @Test
        @DisplayName("Stopped reservation remains in employee list")
        void stopKeepsInEmployee() {
            Reservation res = new Reservation("Jan K.", "600111222",
                    LocalDateTime.of(2026, 7, 1, 18, 0), waiter, barTable, false, false);

            res.stopReservation();

            assertTrue(waiter.getReservations().contains(res));
        }

        @Test
        @DisplayName("Stopped reservation remains in table list")
        void stopKeepsInTable() {
            Reservation res = new Reservation("Jan K.", "600111222",
                    LocalDateTime.of(2026, 7, 1, 18, 0), waiter, barTable, false, false);

            res.stopReservation();

            assertTrue(barTable.getReservations().contains(res));
        }

        @Test
        @DisplayName("Stopped reservation is marked inactive")
        void stopMarksInactive() {
            Reservation res = new Reservation("Jan K.", "600111222",
                    LocalDateTime.of(2026, 7, 1, 18, 0), waiter, barTable, false, false);

            res.stopReservation();

            assertFalse(res.isActive());
        }

        @Test
        @DisplayName("Stopping an already inactive reservation throws")
        void stopInactiveThrows() {
            Reservation res = new Reservation("Jan K.", "600111222",
                    LocalDateTime.of(2026, 7, 1, 18, 0), waiter, barTable, false, false);

            res.stopReservation();

            assertThrows(IllegalStateException.class, res::stopReservation);
        }
    }

    // ===================================================================
    // 7. Restaurant.reserveTable — auto-assignment
    // ===================================================================

    @Nested
    @DisplayName("Restaurant.reserveTable")
    class RestaurantReserveTable {

        @Test
        @DisplayName("reserveTable creates a valid reservation")
        void reserveTableCreatesReservation() throws Exception {
            Reservation res = restaurant.reserveTable(1, "Jan K.", "600111222",
                    LocalDateTime.of(2026, 8, 1, 18, 0), false, false);

            assertNotNull(res);
            assertTrue(res.isActive());
        }

        @Test
        @DisplayName("Employee is auto-assigned and is a Waiter or Intern")
        void autoAssignedEmployeeIsEligible() throws Exception {
            Reservation res = restaurant.reserveTable(1, "Jan K.", "600111222",
                    LocalDateTime.of(2026, 8, 1, 18, 0), false, false);

            Employee.Role role = res.getAssignedEmployee().getRole();
            assertTrue(role == Employee.Role.Waiter || role == Employee.Role.Intern);
        }

        @Test
        @DisplayName("Least-loaded eligible employee is preferred")
        void leastLoadedEmployeePreferred() throws Exception {
            // Give waiter one reservation first so intern has fewer
            Reservation res1 = restaurant.reserveTable(2, "Anna N.", "601222333",
                    LocalDateTime.of(2026, 8, 1, 12, 0), false, false);

            // Second reservation should go to whichever eligible employee has fewer
            Reservation res2 = restaurant.reserveTable(1, "Jan K.", "600111222",
                    LocalDateTime.of(2026, 8, 2, 18, 0), false, false);
            
            Employee.Role assigned = res2.getAssignedEmployee().getRole();


            if (waiter.getReservations().contains(res1)) {
                assertEquals(Employee.Role.Intern, assigned);
            } else {
                assertEquals(Employee.Role.Waiter, assigned);
            }
        }

        @Test
        @DisplayName("Reserving an already-reserved table at the same time throws")
        void doubleBookingThrows() throws Exception {
            LocalDateTime time = LocalDateTime.of(2026, 8, 1, 18, 0);
            restaurant.reserveTable(1, "Jan K.", "600111222", time, false, false);

            assertThrows(IllegalStateException.class, () ->
                    restaurant.reserveTable(1, "Anna N.", "601222333", time, false, false));
        }

        @Test
        @DisplayName("Non-existent table number throws")
        void nonExistentTableThrows() {
            assertThrows(IllegalStateException.class, () ->
                    restaurant.reserveTable(99, "Jan K.", "600111222",
                            LocalDateTime.of(2026, 8, 1, 18, 0), false, false));
        }
    }

    // ===================================================================
    // 8. Restaurant.findAvailableTables
    // ===================================================================

    @Nested
    @DisplayName("Restaurant.findAvailableTables")
    class FindAvailableTables {

        @Test
        @DisplayName("All tables are available when none are reserved")
        void allTablesAvailableWhenEmpty() {
            LocalDateTime time = LocalDateTime.of(2026, 9, 1, 18, 0);
            List<Restaurant.Table> available = restaurant.findAvailableTables(time);

            assertEquals(2, available.size());
        }

        @Test
        @DisplayName("Reserved table does not appear in available list")
        void reservedTableExcluded() {
            LocalDateTime time = LocalDateTime.of(2026, 9, 1, 18, 0);
            new Reservation("Jan K.", "600111222", time, waiter, barTable, false, false);

            List<Restaurant.Table> available = restaurant.findAvailableTables(time);

            assertFalse(available.contains(barTable));
            assertTrue(available.contains(familyTable));
        }

        @Test
        @DisplayName("Stopped reservation frees the table in the available list")
        void stoppedReservationFreesTable() {
            LocalDateTime time = LocalDateTime.of(2026, 9, 1, 18, 0);
            Reservation res = new Reservation("Jan K.", "600111222", time, waiter, barTable, false, false);

            res.stopReservation();

            assertTrue(restaurant.findAvailableTables(time).contains(barTable));
        }
    }

    // ===================================================================
    // 9. Employee — active reservation count
    // ===================================================================

    @Nested
    @DisplayName("Employee active reservation count")
    class EmployeeReservationCount {

        @Test
        @DisplayName("Count increments when reservation is added")
        void countIncrementsOnAdd() {
            assertEquals(0, waiter.getActiveReservationCount());

            new Reservation("Jan K.", "600111222",
                    LocalDateTime.of(2026, 7, 1, 18, 0), waiter, barTable, false, false);

            assertEquals(1, waiter.getActiveReservationCount());
        }

        @Test
        @DisplayName("Count decrements after cancel (reservation removed from list)")
        void countDecrementsAfterCancel() {
            Reservation res = new Reservation("Jan K.", "600111222",
                    LocalDateTime.of(2026, 7, 1, 18, 0), waiter, barTable, false, false);

            res.cancel();

            assertEquals(0, waiter.getActiveReservationCount());
        }

        @Test
        @DisplayName("Count decrements after stop (reservation stays in list but inactive)")
        void countDecrementsAfterStop() {
            Reservation res = new Reservation("Jan K.", "600111222",
                    LocalDateTime.of(2026, 7, 1, 18, 0), waiter, barTable, false, false);

            res.stopReservation();

            assertEquals(0, waiter.getActiveReservationCount());
        }
    }
}