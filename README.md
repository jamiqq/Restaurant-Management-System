# Pizza Restaurant Management System

A Java-based restaurant management system built as a university diploma project. The system models the core domain of a multi-location pizza restaurant chain, covering restaurants, tables, employees, and reservations, with a Swing GUI for the Prolong Reservation use case.

---

## Project Overview

The system manages:

- **Restaurants** and their associated **Storages**
- **Tables** of different types (Bar, Family, FamilyBar) with Inside/Outside location variants
- **Employees** split into Full-Time and Part-Time contracts, with roles: Manager, Waiter, Intern, Cleaner
- **Reservations** with support for plain, celebration, and private types, including a full lifecycle: creation, prolongation, stop, and cancellation

All domain objects are tracked via class extents and persisted to disk using Java object serialisation.

---

## Architecture

The project follows an object-oriented design with bidirectional associations maintained in code:

- `Restaurant` owns `Table` as a composition — tables are inner classes of `Restaurant` and are removed when the restaurant is deleted
- `Employee` and `Reservation` maintain a bidirectional many-to-many association — each employee holds a list of their reservations, each reservation holds a reference to its assigned employee
- `Restaurant` and `Employee` maintain a bidirectional association via a qualified PESEL-keyed map
- `Restaurant.Table` and `Reservation` maintain a bidirectional association — each table holds its reservation history, each reservation holds its assigned table

---

## Project Structure

```
src/
├── main/
│   └── java/
│       ├── Restaurant.java          # Restaurant + inner Table hierarchy
│       ├── Employee.java            # Abstract employee base class
│       ├── FullTimeEmployee.java    # Full-time contract with vacation logic
│       ├── PartTimeEmployee.java    # Part-time contract with hourly pay logic
│       ├── Reservation.java         # Reservation lifecycle and prolong logic
│       ├── Storage.java             # Storage associated with restaurants
│       ├── IFamilyTable.java        # Interface for family table behaviour
│       ├── ExtentPersistence.java   # Serialisation save/load helper
│       ├── ManageReservationGUI.java# Swing GUI for the Prolong Reservation use case
│       └── Main.java                # Entry point — populates data and launches GUI
└── test/
    └── java/
        └── ReservationSystemTest.java # JUnit 5 test suite
```

---

## Key Features

**Reservation lifecycle**
- Reservations can only be created by a Manager via `Restaurant.reserveTable()`
- Stop — marks the reservation inactive, table freed, record preserved
- Cancel — removes the reservation from all extents, table freed
- Prolong — extends the end time with guards for max duration, table conflict with optional table swap, and employee overload with automatic reassignment

**Time slot validation**
- Tables enforce non-overlapping reservations via `isReservedAt()` and `isReservedAtExcluding()`
- The standard duration is 2 hours; the maximum extensible duration is 4 hours

**Employee workload tracking**
- Each employee tracks active reservation count
- Prolong automatically reassigns to the least loaded eligible employee when the assigned one reaches the maximum of 5 active reservations

**Persistence**
- All extents are serialised to `extent.bin` on every state change in the GUI
- State is restored on startup via `ExtentPersistence.load()`

---

## GUI — Manage Reservation Use Case

The Swing GUI implements the Prolong Reservation use case with supporting Stop and Cancel actions.

**Flow:**
1. On launch, a dialog shows all Waiters and Interns from the extent
2. After selecting an employee, a second dialog shows their active reservations — populated via the `employee.getReservations()` association, not by filtering the global extent
3. The Manage Reservation window opens showing full reservation details and three action buttons: Stop, Cancel, Prolong
4. The Prolong flow handles all alternative paths: max duration exceeded, table conflict with swap confirmation, employee reassignment, no table available, no employee available

A collapsible testing controls panel is available in the GUI for demonstrating specific scenario outcomes without modifying domain data.

---

## Requirements

- Java 21 or later
- Maven 3.6 or later

---

## Building and Running

```bash
# compile and run all tests
mvn test

# run the application
mvn compile exec:java -Dexec.mainClass="Main"

# or compile and run manually
javac -d out src/main/java/*.java
java -cp out Main
```

---

## Running Tests

```bash
mvn test
```

The test suite covers:

- Table time slot availability and overlap logic
- Reservation creation guards (manager-only, eligible employee, valid table)
- Celebration and private reservation type accessors
- Cancel — removal from all extents
- Stop — record preserved, table freed
- Prolong — success, max duration, table swap, employee reassignment
- `findAvailableTables` at various time slots
- Employee active reservation count across lifecycle events

---

## Author

Tymofii Lysenko — s30879