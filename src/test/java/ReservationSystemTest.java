// import org.junit.jupiter.api.*;

// import java.time.LocalDate;
// import java.time.LocalDateTime;
// import java.util.List;

// import static org.junit.jupiter.api.Assertions.*;

// @DisplayName("Restaurant Reservation System")
// class ReservationSystemTest {

//     // -------------------------------------------------------------------
//     // Shared fixtures — rebuilt fresh before every test
//     // -------------------------------------------------------------------

//     private Storage storage;
//     private Restaurant restaurant;

//     private Restaurant.BarTable    barTable;
//     private Restaurant.FamilyTable familyTable;

//     private FullTimeEmployee manager;
//     private FullTimeEmployee waiter;
//     private PartTimeEmployee intern;

//     private static final String WAITER_PESEL  = "95072267890";
//     private static final String INTERN_PESEL  = "02050534521";
//     private static final String MANAGER_PESEL = "85031012345";

//     @BeforeEach
//     void setUp() throws Exception {
//         Restaurant.clearExtent();
//         Employee.clearExtent();
//         Reservation.clearExtent();

//         storage    = new Storage("Depot Street", 1, 1000, 1.2);
//         restaurant = new Restaurant("Marszalkowska", 10, storage);

//         barTable    = restaurant.addBarTable(1, "High Stool",
//                 Restaurant.Table.Location.Inside, Restaurant.Table.Section.Modern, false, 2);
//         familyTable = restaurant.addFamilyTable(2, "Round",
//                 Restaurant.Table.Location.Inside, Restaurant.Table.Section.Family, false, true, true);

//         manager = new FullTimeEmployee(
//                 "Anna", null, "Kowalska",
//                 List.of("anna@mail.com"),
//                 LocalDate.of(1985, 3, 10),
//                 Employee.Role.Manager,
//                 MANAGER_PESEL,
//                 List.of(restaurant),
//                 FullTimeEmployee.Insurance.Family);

//         waiter = new FullTimeEmployee(
//                 "Piotr", null, "Nowak",
//                 List.of("piotr@mail.com"),
//                 LocalDate.of(1995, 7, 22),
//                 Employee.Role.Waiter,
//                 WAITER_PESEL,
//                 List.of(restaurant),
//                 FullTimeEmployee.Insurance.Upgraded);

//         intern = new PartTimeEmployee(
//                 "Kamil", null, "Wisniewski",
//                 List.of("kamil@mail.com"),
//                 LocalDate.of(2002, 5, 5),
//                 Employee.Role.Intern,
//                 INTERN_PESEL,
//                 List.of(restaurant),
//                 20.0, 15);
//     }

//     // ===================================================================
//     // 1. Table — time slot availability
//     // ===================================================================

//     @Nested
//     @DisplayName("Table time slot availability")
//     class TableAvailability {

//         @Test
//         @DisplayName("Fresh table is available at any time")
//         void freshTableIsAvailable() {
//             assertFalse(barTable.isReservedAt(LocalDateTime.of(2026, 7, 1, 18, 0)));
//         }

//         @Test
//         @DisplayName("Table is unavailable during an active reservation window")
//         void tableUnavailableDuringActiveReservation() throws Exception {
//             restaurant.reserveTable(1, "Jan K.", "600111222",
//                     LocalDateTime.of(2026, 7, 1, 18, 0), false, false, WAITER_PESEL);

//             // 19:00 falls inside the 18:00–20:00 window
//             assertTrue(barTable.isReservedAt(LocalDateTime.of(2026, 7, 1, 19, 0)));
//         }

//         @Test
//         @DisplayName("Table is available exactly when the previous reservation ends")
//         void tableAvailableAtExactEndTime() throws Exception {
//             restaurant.reserveTable(1, "Jan K.", "600111222",
//                     LocalDateTime.of(2026, 7, 1, 18, 0), false, false, WAITER_PESEL);

//             // 20:00 == end time — strict boundary, no overlap
//             assertFalse(barTable.isReservedAt(LocalDateTime.of(2026, 7, 1, 20, 0)));
//         }

//         @Test
//         @DisplayName("Overlapping reservation on the same table is rejected")
//         void overlappingReservationRejected() throws Exception {
//             restaurant.reserveTable(1, "Jan K.", "600111222",
//                     LocalDateTime.of(2026, 7, 1, 18, 0), false, false, WAITER_PESEL);

//             assertThrows(IllegalStateException.class, () ->
//                     restaurant.reserveTable(1, "Anna N.", "601222333",
//                             LocalDateTime.of(2026, 7, 1, 19, 0), false, false, INTERN_PESEL));
//         }

//         @Test
//         @DisplayName("Back-to-back reservations on the same table are permitted")
//         void backToBackReservationsPermitted() throws Exception {
//             restaurant.reserveTable(1, "Jan K.", "600111222",
//                     LocalDateTime.of(2026, 7, 1, 18, 0), false, false, WAITER_PESEL);

//             // 20:00 starts exactly when the first ends
//             assertDoesNotThrow(() ->
//                     restaurant.reserveTable(1, "Anna N.", "601222333",
//                             LocalDateTime.of(2026, 7, 1, 20, 0), false, false, INTERN_PESEL));
//         }

//         @Test
//         @DisplayName("Table becomes available again after reservation is stopped")
//         void tableAvailableAfterStop() throws Exception {
//             Reservation res = restaurant.reserveTable(1, "Jan K.", "600111222",
//                     LocalDateTime.of(2026, 7, 1, 18, 0), false, false, WAITER_PESEL);

//             res.stopReservation(waiter);

//             assertFalse(barTable.isReservedAt(LocalDateTime.of(2026, 7, 1, 19, 0)));
//         }

//         @Test
//         @DisplayName("Table becomes available again after reservation is cancelled")
//         void tableAvailableAfterCancel() throws Exception {
//             Reservation res = restaurant.reserveTable(1, "Jan K.", "600111222",
//                     LocalDateTime.of(2026, 7, 1, 18, 0), false, false, WAITER_PESEL);

//             res.cancel(waiter);

//             assertFalse(barTable.isReservedAt(LocalDateTime.of(2026, 7, 1, 19, 0)));
//         }
//     }

//     // ===================================================================
//     // 2. Reservation — creation
//     // ===================================================================

//     @Nested
//     @DisplayName("Reservation creation")
//     class ReservationCreation {

//         @Test
//         @DisplayName("Valid reservation is added to extent")
//         void validReservationAddedToExtent() throws Exception {
//             restaurant.reserveTable(1, "Jan K.", "600111222",
//                     LocalDateTime.of(2026, 7, 1, 18, 0), false, false, WAITER_PESEL);

//             assertEquals(1, Reservation.getExtent().size());
//         }

//         @Test
//         @DisplayName("Reservation is linked to the explicitly assigned employee and table")
//         void reservationLinkedToExplicitAssignees() throws Exception {
//             Reservation res = restaurant.reserveTable(1, "Jan K.", "600111222",
//                     LocalDateTime.of(2026, 7, 1, 18, 0), false, false, WAITER_PESEL);

//             assertEquals(waiter,   res.getAssignedEmployee());
//             assertEquals(barTable, res.getAssignedTable());
//             assertTrue(waiter.getReservations().contains(res));
//             assertTrue(barTable.getReservations().contains(res));
//         }

//         @Test
//         @DisplayName("End time is exactly DURATION_HOURS after start")
//         void endTimeIsCorrect() throws Exception {
//             LocalDateTime start = LocalDateTime.of(2026, 7, 1, 18, 0);
//             Reservation res = restaurant.reserveTable(1, "Jan K.", "600111222",
//                     start, false, false, WAITER_PESEL);

//             assertEquals(start.plusHours(Reservation.durationHours), res.getEndTime());
//         }

//         @Test
//         @DisplayName("Manager cannot be assigned to a reservation")
//         void managerCannotBeAssigned() {
//             assertThrows(IllegalArgumentException.class, () ->
//                     restaurant.reserveTable(1, "Jan K.", "600111222",
//                             LocalDateTime.of(2026, 7, 1, 18, 0), false, false, MANAGER_PESEL));
//         }

//         @Test
//         @DisplayName("Unknown PESEL throws")
//         void unknownPeselThrows() {
//             assertThrows(IllegalArgumentException.class, () ->
//                     restaurant.reserveTable(1, "Jan K.", "600111222",
//                             LocalDateTime.of(2026, 7, 1, 18, 0), false, false, "00000000000"));
//         }

//         @Test
//         @DisplayName("Non-existent table number throws")
//         void nonExistentTableThrows() {
//             assertThrows(IllegalStateException.class, () ->
//                     restaurant.reserveTable(99, "Jan K.", "600111222",
//                             LocalDateTime.of(2026, 7, 1, 18, 0), false, false, WAITER_PESEL));
//         }

//         @Test
//         @DisplayName("Double booking on the same table and time throws")
//         void doubleBookingThrows() throws Exception {
//             LocalDateTime time = LocalDateTime.of(2026, 8, 1, 18, 0);
//             restaurant.reserveTable(1, "Jan K.", "600111222", time, false, false, WAITER_PESEL);

//             assertThrows(IllegalStateException.class, () ->
//                     restaurant.reserveTable(1, "Anna N.", "601222333", time, false, false, INTERN_PESEL));
//         }
//     }

//     // ===================================================================
//     // 3. Reservation — celebration type
//     // ===================================================================

//     @Nested
//     @DisplayName("Celebration reservation")
//     class CelebrationReservationTests {

//         @Test
//         @DisplayName("Occasion and decoration can be set on a celebration reservation")
//         void occasionAndDecorationSet() throws Exception {
//             Reservation res = restaurant.reserveTable(1, "Ewa B.", "603444555",
//                     LocalDateTime.of(2026, 7, 1, 18, 0), true, false, WAITER_PESEL);

//             res.setOccasion(Reservation.Occasion.Birthday);
//             res.setDecorationDescription("Balloon arch");

//             assertEquals(Reservation.Occasion.Birthday, res.hasOccasion());
//             assertEquals("Balloon arch", res.hasDecorationDescription());
//         }

//         @Test
//         @DisplayName("Setting occasion on a plain reservation throws")
//         void occasionOnPlainReservationThrows() throws Exception {
//             Reservation res = restaurant.reserveTable(1, "Jan K.", "600111222",
//                     LocalDateTime.of(2026, 7, 1, 18, 0), false, false, WAITER_PESEL);

//             assertThrows(Exception.class, () -> res.setOccasion(Reservation.Occasion.Birthday));
//         }

//         @Test
//         @DisplayName("Getting decoration on a plain reservation throws")
//         void decorationOnPlainReservationThrows() throws Exception {
//             Reservation res = restaurant.reserveTable(1, "Jan K.", "600111222",
//                     LocalDateTime.of(2026, 7, 1, 18, 0), false, false, WAITER_PESEL);

//             assertThrows(Exception.class, res::hasDecorationDescription);
//         }
//     }

//     // ===================================================================
//     // 4. Reservation — private type
//     // ===================================================================

//     @Nested
//     @DisplayName("Private reservation")
//     class PrivateReservationTests {

//         @Test
//         @DisplayName("Security flag can be set on a private reservation")
//         void securityFlagSet() throws Exception {
//             Reservation res = restaurant.reserveTable(1, "Adam W.", "602333444",
//                     LocalDateTime.of(2026, 7, 1, 18, 0), false, true, INTERN_PESEL);

//             res.setSecurityRequired(true);

//             assertTrue(res.hasSecurityRequired());
//         }

//         @Test
//         @DisplayName("Minimum spend is accessible on a private reservation")
//         void minimumSpendAccessible() throws Exception {
//             Reservation res = restaurant.reserveTable(1, "Adam W.", "602333444",
//                     LocalDateTime.of(2026, 7, 1, 18, 0), false, true, INTERN_PESEL);

//             assertTrue(res.hasMinimumSpend() > 0);
//         }

//         @Test
//         @DisplayName("Security flag on a plain reservation throws")
//         void securityOnPlainReservationThrows() throws Exception {
//             Reservation res = restaurant.reserveTable(1, "Jan K.", "600111222",
//                     LocalDateTime.of(2026, 7, 1, 18, 0), false, false, WAITER_PESEL);

//             assertThrows(Exception.class, () -> res.setSecurityRequired(true));
//         }
//     }

//     // ===================================================================
//     // 5. Reservation — cancel (employee-driven)
//     // ===================================================================

//     @Nested
//     @DisplayName("Reservation cancellation")
//     class ReservationCancellation {

//         @Test
//         @DisplayName("Cancelled reservation is removed from Reservation extent")
//         void cancelRemovesFromExtent() throws Exception {
//             Reservation res = restaurant.reserveTable(1, "Jan K.", "600111222",
//                     LocalDateTime.of(2026, 7, 1, 18, 0), false, false, WAITER_PESEL);

//             res.cancel(waiter);

//             assertFalse(Reservation.getExtent().contains(res));
//         }

//         @Test
//         @DisplayName("Cancelled reservation is removed from employee list")
//         void cancelRemovesFromEmployee() throws Exception {
//             Reservation res = restaurant.reserveTable(1, "Jan K.", "600111222",
//                     LocalDateTime.of(2026, 7, 1, 18, 0), false, false, WAITER_PESEL);

//             res.cancel(waiter);

//             assertFalse(waiter.getReservations().contains(res));
//         }

//         @Test
//         @DisplayName("Cancelled reservation is removed from table list")
//         void cancelRemovesFromTable() throws Exception {
//             Reservation res = restaurant.reserveTable(1, "Jan K.", "600111222",
//                     LocalDateTime.of(2026, 7, 1, 18, 0), false, false, WAITER_PESEL);

//             res.cancel(waiter);

//             assertFalse(barTable.getReservations().contains(res));
//         }

//         @Test
//         @DisplayName("Cancelling an already inactive reservation throws")
//         void cancelInactiveThrows() throws Exception {
//             Reservation res = restaurant.reserveTable(1, "Jan K.", "600111222",
//                     LocalDateTime.of(2026, 7, 1, 18, 0), false, false, WAITER_PESEL);

//             res.cancel(waiter);

//             assertThrows(IllegalStateException.class, () -> res.cancel(waiter));
//         }

//         @Test
//         @DisplayName("A different employee cannot cancel someone else's reservation")
//         void wrongEmployeeCannotCancel() throws Exception {
//             Reservation res = restaurant.reserveTable(1, "Jan K.", "600111222",
//                     LocalDateTime.of(2026, 7, 1, 18, 0), false, false, WAITER_PESEL);

//             assertThrows(IllegalArgumentException.class, () -> res.cancel(intern));
//         }
//     }

//     // ===================================================================
//     // 6. Reservation — stop (employee-driven)
//     // ===================================================================

//     @Nested
//     @DisplayName("Reservation stop")
//     class ReservationStop {

//         @Test
//         @DisplayName("Stopped reservation remains in Reservation extent")
//         void stopKeepsInExtent() throws Exception {
//             Reservation res = restaurant.reserveTable(1, "Jan K.", "600111222",
//                     LocalDateTime.of(2026, 7, 1, 18, 0), false, false, WAITER_PESEL);

//             res.stopReservation(waiter);

//             assertTrue(Reservation.getExtent().contains(res));
//         }

//         @Test
//         @DisplayName("Stopped reservation remains in employee list")
//         void stopKeepsInEmployee() throws Exception {
//             Reservation res = restaurant.reserveTable(1, "Jan K.", "600111222",
//                     LocalDateTime.of(2026, 7, 1, 18, 0), false, false, WAITER_PESEL);

//             res.stopReservation(waiter);

//             assertTrue(waiter.getReservations().contains(res));
//         }

//         @Test
//         @DisplayName("Stopped reservation remains in table list")
//         void stopKeepsInTable() throws Exception {
//             Reservation res = restaurant.reserveTable(1, "Jan K.", "600111222",
//                     LocalDateTime.of(2026, 7, 1, 18, 0), false, false, WAITER_PESEL);

//             res.stopReservation(waiter);

//             assertTrue(barTable.getReservations().contains(res));
//         }

//         @Test
//         @DisplayName("Stopped reservation is marked inactive")
//         void stopMarksInactive() throws Exception {
//             Reservation res = restaurant.reserveTable(1, "Jan K.", "600111222",
//                     LocalDateTime.of(2026, 7, 1, 18, 0), false, false, WAITER_PESEL);

//             res.stopReservation(waiter);

//             assertFalse(res.isActive());
//         }

//         @Test
//         @DisplayName("Stopping an already inactive reservation throws")
//         void stopInactiveThrows() throws Exception {
//             Reservation res = restaurant.reserveTable(1, "Jan K.", "600111222",
//                     LocalDateTime.of(2026, 7, 1, 18, 0), false, false, WAITER_PESEL);

//             res.stopReservation(waiter);

//             assertThrows(IllegalStateException.class, () -> res.stopReservation(waiter));
//         }

//         @Test
//         @DisplayName("A different employee cannot stop someone else's reservation")
//         void wrongEmployeeCannotStop() throws Exception {
//             Reservation res = restaurant.reserveTable(1, "Jan K.", "600111222",
//                     LocalDateTime.of(2026, 7, 1, 18, 0), false, false, WAITER_PESEL);

//             assertThrows(IllegalArgumentException.class, () -> res.stopReservation(intern));
//         }
//     }

//     // ===================================================================
//     // 7. Restaurant.findAvailableTables
//     // ===================================================================

//     @Nested
//     @DisplayName("Restaurant.findAvailableTables")
//     class FindAvailableTables {

//         @Test
//         @DisplayName("All tables are available when none are reserved")
//         void allTablesAvailableWhenEmpty() {
//             List<Restaurant.Table> available = restaurant.findAvailableTables(
//                     LocalDateTime.of(2026, 9, 1, 18, 0));

//             assertEquals(2, available.size());
//         }

//         @Test
//         @DisplayName("Reserved table does not appear in available list")
//         void reservedTableExcluded() throws Exception {
//             LocalDateTime time = LocalDateTime.of(2026, 9, 1, 18, 0);
//             restaurant.reserveTable(1, "Jan K.", "600111222", time, false, false, WAITER_PESEL);

//             List<Restaurant.Table> available = restaurant.findAvailableTables(time);

//             assertFalse(available.contains(barTable));
//             assertTrue(available.contains(familyTable));
//         }

//         @Test
//         @DisplayName("Stopped reservation frees the table in the available list")
//         void stoppedReservationFreesTable() throws Exception {
//             LocalDateTime time = LocalDateTime.of(2026, 9, 1, 18, 0);
//             Reservation res = restaurant.reserveTable(1, "Jan K.", "600111222",
//                     time, false, false, WAITER_PESEL);

//             res.stopReservation(waiter);

//             assertTrue(restaurant.findAvailableTables(time).contains(barTable));
//         }

//         @Test
//         @DisplayName("Cancelled reservation frees the table in the available list")
//         void cancelledReservationFreesTable() throws Exception {
//             LocalDateTime time = LocalDateTime.of(2026, 9, 1, 18, 0);
//             Reservation res = restaurant.reserveTable(1, "Jan K.", "600111222",
//                     time, false, false, WAITER_PESEL);

//             res.cancel(waiter);

//             assertTrue(restaurant.findAvailableTables(time).contains(barTable));
//         }

//         @Test
//         @DisplayName("Same table is available at a non-overlapping time slot")
//         void differentTimeSlotAvailable() throws Exception {
//             restaurant.reserveTable(1, "Jan K.", "600111222",
//                     LocalDateTime.of(2026, 9, 1, 18, 0), false, false, WAITER_PESEL);

//             // 20:00 is the exact end — no overlap
//             List<Restaurant.Table> available = restaurant.findAvailableTables(
//                     LocalDateTime.of(2026, 9, 1, 20, 0));

//             assertTrue(available.contains(barTable));
//         }
//     }

//     // ===================================================================
//     // 8. Employee — active reservation count
//     // ===================================================================

//     @Nested
//     @DisplayName("Employee active reservation count")
//     class EmployeeReservationCount {

//         @Test
//         @DisplayName("Count is zero before any reservation")
//         void countZeroInitially() {
//             assertEquals(0, waiter.getActiveReservationCount());
//             assertEquals(0, intern.getActiveReservationCount());
//         }

//         @Test
//         @DisplayName("Count increments when reservation is explicitly assigned")
//         void countIncrementsOnAssign() throws Exception {
//             restaurant.reserveTable(1, "Jan K.", "600111222",
//                     LocalDateTime.of(2026, 7, 1, 18, 0), false, false, WAITER_PESEL);

//             assertEquals(1, waiter.getActiveReservationCount());
//             assertEquals(0, intern.getActiveReservationCount());
//         }

//         @Test
//         @DisplayName("Count decrements after cancel — reservation removed from list")
//         void countDecrementsAfterCancel() throws Exception {
//             Reservation res = restaurant.reserveTable(1, "Jan K.", "600111222",
//                     LocalDateTime.of(2026, 7, 1, 18, 0), false, false, WAITER_PESEL);

//             res.cancel(waiter);

//             assertEquals(0, waiter.getActiveReservationCount());
//         }

//         @Test
//         @DisplayName("Count decrements after stop — record kept but inactive")
//         void countDecrementsAfterStop() throws Exception {
//             Reservation res = restaurant.reserveTable(1, "Jan K.", "600111222",
//                     LocalDateTime.of(2026, 7, 1, 18, 0), false, false, WAITER_PESEL);

//             res.stopReservation(waiter);

//             assertEquals(0, waiter.getActiveReservationCount());
//         }

//         @Test
//         @DisplayName("Multiple reservations across employees are counted independently")
//         void countsAreIndependent() throws Exception {
//             restaurant.reserveTable(1, "Jan K.", "600111222",
//                     LocalDateTime.of(2026, 7, 1, 18, 0), false, false, WAITER_PESEL);
//             restaurant.reserveTable(2, "Anna N.", "601222333",
//                     LocalDateTime.of(2026, 7, 1, 18, 0), false, false, INTERN_PESEL);

//             assertEquals(1, waiter.getActiveReservationCount());
//             assertEquals(1, intern.getActiveReservationCount());
//         }
//     }
// }