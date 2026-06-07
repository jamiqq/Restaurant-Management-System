import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Function;

/**
 * Manage Reservation GUI — operates on domain extents and associations.
 */
public class ManageReservationGUI extends JFrame {

  // ------------------------------------------------------------------
  // Palette
  // ------------------------------------------------------------------
  static final Color BG = new Color(0x0f, 0x11, 0x17);
  static final Color SURFACE = new Color(0x18, 0x1c, 0x27);
  static final Color BORDER = new Color(0x2a, 0x2f, 0x3e);
  static final Color ACCENT = new Color(0x4f, 0x8e, 0xf7);
  static final Color DANGER = new Color(0xe0, 0x5c, 0x5c);
  static final Color WARN = new Color(0xe0, 0xa9, 0x5c);
  static final Color SUCCESS = new Color(0x4f, 0xc9, 0x8e);
  static final Color INFO = new Color(0x5c, 0xd0, 0xe0);
  static final Color TEXT = new Color(0xd4, 0xda, 0xf0);
  static final Color MUTED = new Color(0x6b, 0x73, 0x90);

  // ------------------------------------------------------------------
  // Fonts
  // ------------------------------------------------------------------
  static final Font F_MONO_S = new Font("Monospaced", Font.PLAIN, 10);
  static final Font F_MONO_M = new Font("Monospaced", Font.BOLD, 12);
  static final Font F_SANS_M = new Font("SansSerif", Font.PLAIN, 13);
  static final Font F_SANS_B = new Font("SansSerif", Font.BOLD, 13);

  private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd  HH:mm");

  // ------------------------------------------------------------------
  // State
  // ------------------------------------------------------------------
  private final CardLayout cards = new CardLayout();
  private final JPanel deck = new JPanel(cards);
  private final Employee currentEmployee;
  private Reservation currentReservation;
  private final Runnable onBackToList;
  private JSpinner prolongSpinner;

  // ------------------------------------------------------------------
  // Boot
  // ------------------------------------------------------------------
  public ManageReservationGUI(Employee employee, Reservation reservation, Runnable onBackToList) {
    super("Pizza Restaurant Management System — Manage Reservation");
    this.currentEmployee = employee;
    this.currentReservation = reservation;
    this.onBackToList = onBackToList;

    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosed(WindowEvent e) {
        persistExtents();
        if (onBackToList != null) {
          onBackToList.run();
        }
      }
    });
    getContentPane().setBackground(BG);
    setLayout(new BorderLayout());

    deck.setBackground(BG);
    rebuildScreens();

    JScrollPane scroll = new JScrollPane(deck,
        JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
        JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    scroll.setBorder(null);
    scroll.getViewport().setBackground(BG);
    scroll.getVerticalScrollBar().setUnitIncrement(16);
    add(scroll, BorderLayout.CENTER);

    setSize(700, 720);
    setLocationRelativeTo(null);
    setVisible(true);
  }

  private void rebuildScreens() {
    deck.removeAll();
    addScreens();
    deck.revalidate();
    deck.repaint();
  }

  // ------------------------------------------------------------------
  // Screen registry
  // ------------------------------------------------------------------
  private void addScreens() {
    deck.add(screenMain(), "main");
    deck.add(screenStopConfirm(), "stop_confirm");
    deck.add(screenStopSuccess(), "stop_success");
    deck.add(screenCancelConfirm(), "cancel_confirm");
    deck.add(screenCancelSuccess(), "cancel_success");
    deck.add(screenNoShowConfirm(), "noshow_confirm");
    deck.add(screenNoShowError(), "noshow_error");
    deck.add(screenNoShowSuccess(), "noshow_success");
    deck.add(screenProlongForm(), "prolong_form");
    deck.add(screenProlongSuccess(), "prolong_success");
    deck.add(screenProlongMaxDur(), "prolong_maxdur");
    deck.add(screenProlongSwapQ(), "prolong_tableswap");
    deck.add(screenProlongSwapped(), "prolong_swapped");
    deck.add(screenProlongNoTable(), "prolong_notable");
    deck.add(screenProlongReassign(), "prolong_reassign");
    deck.add(screenProlongNoEmp(), "prolong_noemp");
  }

  // ==================================================================
  // Domain actions
  // ==================================================================

  private void persistExtents() {
    try {
      ExtentPersistence.save();
    } catch (IOException ex) {
      JOptionPane.showMessageDialog(this,
          "Failed to save changes: " + ex.getMessage(),
          "Save Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  private void returnToReservationList() {
    dispose();
  }

  private JPanel backToReservationsCard() {
    JPanel w = card("Navigation");
    w.add(btnFull("Back to Reservations", ACCENT, e -> returnToReservationList()));
    return w;
  }

  private void doStop() {
    currentReservation.stopReservation(currentEmployee);
    persistExtents();
    rebuildScreens();
    show("stop_success");
  }

  private void doCancel() {
    currentReservation.cancel();
    persistExtents();
    rebuildScreens();
    show("cancel_success");
  }

  private void doNoShowCancel() {
    currentReservation.cancelNoGuest(currentEmployee, LocalDateTime.now());
    persistExtents();
    rebuildScreens();
    show("noshow_success");
  }

  private void doProlong() {
    int hours = (Integer) prolongSpinner.getValue();
    Restaurant.Table tableBefore = currentReservation.getAssignedTable();
    String empPeselBefore = currentEmployee.getPeselNumber();

    try {
      currentReservation.prolongReservation(currentEmployee, hours);
      persistExtents();
      rebuildScreens();

      if (!currentReservation.getAssignedEmployee().getPeselNumber().equals(empPeselBefore)) {
        show("prolong_reassign");
      } else if (!currentReservation.getAssignedTable().equals(tableBefore)) {
        show("prolong_swapped");
      } else {
        show("prolong_success");
      }
    } catch (IllegalStateException ex) {
      String msg = ex.getMessage() == null ? "" : ex.getMessage();
      if (msg.contains("exceed") || msg.contains("duration")) {
        show("prolong_maxdur");
      } else if (msg.contains("no other table")) {
        show("prolong_notable");
      } else if (msg.contains("no other eligible employee")) {
        show("prolong_noemp");
      } else {
        JOptionPane.showMessageDialog(this, msg, "Prolong Failed", JOptionPane.ERROR_MESSAGE);
      }
    } catch (IllegalArgumentException ex) {
      JOptionPane.showMessageDialog(this, ex.getMessage(), "Prolong Failed", JOptionPane.ERROR_MESSAGE);
    }
  }

  // ==================================================================
  // SCREENS
  // ==================================================================

  private JPanel screenMain() {
    JPanel root = root();
    root.add(breadcrumb("Reservations", "/ Manage Reservation"));
    root.add(gap(6));

    JPanel det = card("Reservation Details");
    det.add(detailGrid(reservationDetailRows(currentReservation, false), null));
    root.add(det);
    root.add(gap(10));

    JPanel act = card("Actions");
    JPanel bar = grid(2, 2);
    boolean active = currentReservation.isActive();
    if (active) {
      bar.add(btn("Stop", INFO, e -> show("stop_confirm")));
      bar.add(btn("Cancel", DANGER, e -> show("cancel_confirm")));
      bar.add(btn("No-Show Cancel", WARN, e -> {
        LocalDateTime expiry = currentReservation.getDateTimeOfReservation()
            .plusMinutes(Reservation.WAITING_PERIOD);
        if (LocalDateTime.now().isBefore(expiry)) {
          show("noshow_error");
        } else {
          show("noshow_confirm");
        }
      }));
      bar.add(btn("Prolong", ACCENT, e -> show("prolong_form")));
    } else {
      bar.add(btnOff("Stop"));
      bar.add(btnOff("Cancel"));
      bar.add(btnOff("No-Show Cancel"));
      bar.add(btnOff("Prolong"));
      act.add(bar);
      act.add(gap(6));
      act.add(note("All action buttons are disabled — reservation is no longer active."));
      root.add(act);
      root.add(gap(10));
      root.add(backToReservationsCard());
      return root;
    }
    act.add(bar);
    root.add(act);
    root.add(gap(10));
    root.add(backToReservationsCard());
    return root;
  }

  private JPanel screenStopConfirm() {
    JPanel root = root();
    root.add(breadcrumb("Reservations / Manage Reservation", "/ Stop"));
    root.add(gap(6));
    JPanel w = card("Confirmation Required");
    w.add(dialogText(
        "Are you sure you want to stop this reservation?\n" +
            "The table will be freed. The record will be preserved in system history."));
    w.add(gap(10));
    w.add(btnRow(
        btn("Back", MUTED, e -> show("main")),
        btn("Confirm", SUCCESS, e -> doStop())));
    root.add(w);
    return root;
  }

  private JPanel screenStopSuccess() {
    JPanel root = root();
    root.add(breadcrumb("Reservations / Manage Reservation", "/ Stop"));
    root.add(gap(6));
    JPanel det = card("Reservation Details");
    det.add(detailGrid(new String[][] {
        { "Guest Name", currentReservation.getGuestName() },
        { "Table", formatTable(currentReservation.getAssignedTable()) },
        { "Start Time", formatDateTime(currentReservation.getDateTimeOfReservation()) },
        { "End Time", formatDateTime(currentReservation.getEndTime()) },
        { "Status", "Stopped" },
        { "Employee", formatEmployee(currentReservation.getAssignedEmployee()) },
    }, null));
    root.add(det);
    root.add(gap(8));
    root.add(toast("✓", SUCCESS, "Reservation Stopped",
        "Reservation successfully stopped. Table #" +
            currentReservation.getAssignedTable().getTableNumber() + " is now available."));
    root.add(gap(8));
    JPanel act = card("Actions");
    JPanel bar = grid(2, 2);
    bar.add(btnOff("Stop"));
    bar.add(btnOff("Cancel"));
    bar.add(btnOff("No-Show Cancel"));
    bar.add(btnOff("Prolong"));
    act.add(bar);
    act.add(gap(6));
    act.add(note("All action buttons are disabled — reservation is no longer active."));
    root.add(act);
    root.add(gap(10));
    root.add(backToReservationsCard());
    return root;
  }

  private JPanel screenCancelConfirm() {
    JPanel root = root();
    root.add(breadcrumb("Reservations / Manage Reservation", "/ Cancel"));
    root.add(gap(6));
    JPanel w = card("Confirmation Required");
    w.add(dialogText(
        "Are you sure you want to cancel this reservation?\n" +
            "This action cannot be undone.\n" +
            "The reservation will be permanently removed from the system."));
    w.add(gap(10));
    w.add(btnRow(
        btn("Back", MUTED, e -> show("main")),
        btn("Cancel Reservation", DANGER, e -> doCancel())));
    root.add(w);
    return root;
  }

  private JPanel screenCancelSuccess() {
    String guest = currentReservation.getGuestName();
    int tableNum = currentReservation.getAssignedTable().getTableNumber();
    JPanel root = root();
    root.add(breadcrumb("Reservations", "/ Reservation Cancelled"));
    root.add(gap(6));
    root.add(toast("✓", SUCCESS, "Reservation Cancelled",
        "Reservation for " + guest + " successfully cancelled. Table #" + tableNum + " is now available."));
    root.add(gap(8));
    JPanel w = card("Info");
    w.add(note("The reservation has been removed from all extents."));
    root.add(w);
    root.add(gap(10));
    root.add(backToReservationsCard());
    return root;
  }

  private JPanel screenNoShowConfirm() {
    JPanel root = root();
    root.add(breadcrumb("Reservations / Manage Reservation", "/ No-Show Cancel"));
    root.add(gap(6));
    JPanel w = card("Confirmation Required");
    w.add(dialogText(
        "The guest has not appeared.\n" +
            "Are you sure you want to cancel this reservation?"));
    w.add(gap(10));
    w.add(btnRow(
        btn("Back", MUTED, e -> show("main")),
        btn("Confirm No-Show Cancel", WARN, e -> {
          try {
            doNoShowCancel();
          } catch (IllegalStateException ex) {
            rebuildScreens();
            show("noshow_error");
          }
        })));
    root.add(w);
    return root;
  }

  private JPanel screenNoShowError() {
    LocalDateTime expiry = currentReservation.getDateTimeOfReservation()
        .plusMinutes(Reservation.WAITING_PERIOD);
    JPanel root = root();
    root.add(breadcrumb("Reservations / Manage Reservation", "/ No-Show Cancel"));
    root.add(gap(6));
    root.add(toast("✕", DANGER, "Cannot Cancel Yet",
        "The waiting period has not elapsed.\n" +
            "Grace period expires at " + expiry.toLocalTime() + ".\n" +
            "Please wait before cancelling for no-show."));
    root.add(gap(8));
    JPanel w = card("Actions");
    w.add(btnFull("Back to Actions", MUTED, e -> show("main")));
    root.add(w);
    return root;
  }

  private JPanel screenNoShowSuccess() {
    String guest = currentReservation.getGuestName();
    int tableNum = currentReservation.getAssignedTable().getTableNumber();
    JPanel root = root();
    root.add(breadcrumb("Reservations", "/ Reservation Cancelled"));
    root.add(gap(6));
    root.add(toast("✓", SUCCESS, "Reservation Cancelled — No Show",
        "Reservation for " + guest + " cancelled due to no-show.\n" +
            "Table #" + tableNum + " is now available."));
    root.add(gap(8));
    JPanel w = card("Info");
    w.add(note("The reservation has been removed from all extents."));
    root.add(w);
    root.add(gap(10));
    root.add(backToReservationsCard());
    return root;
  }

  private JPanel screenProlongForm() {
    JPanel root = root();
    root.add(breadcrumb("Reservations / Manage Reservation", "/ Prolong"));
    root.add(gap(6));
    JPanel w = card("Prolong Reservation");

    LocalDateTime maxEnd = currentReservation.getDateTimeOfReservation()
        .plusHours(Reservation.MAX_DURATION_HOURS);
    long remainingHours = remainingExtensibleHours(currentReservation);

    JPanel meta = grid(1, 2);
    meta.add(detailItem("Current End Time", formatDateTime(currentReservation.getEndTime()), TEXT));
    meta.add(detailItem("Max Allowed End", formatDateTime(maxEnd), TEXT));
    w.add(meta);
    w.add(gap(10));
    w.add(sep());
    w.add(gap(10));

    w.add(fieldLabel("Extension Duration (hours)"));
    w.add(gap(4));

    int maxSpinner = (int) Math.max(1, remainingHours);
    prolongSpinner = new JSpinner(new SpinnerNumberModel(1, 1, maxSpinner, 1));
    styleSpinner(prolongSpinner);
    w.add(prolongSpinner);
    w.add(gap(4));
    w.add(hint("Minimum 1 hour.  Maximum total duration: " + Reservation.MAX_DURATION_HOURS + " hours."));
    w.add(gap(12));

    w.add(btnRow(
        btn("Back", MUTED, e -> show("main")),
        btn("Confirm Extension", SUCCESS, e -> doProlong())));
    root.add(w);
    return root;
  }

  private JPanel screenProlongSuccess() {
    JPanel root = root();
    root.add(breadcrumb("Reservations / Manage Reservation", "/ Prolong — Success"));
    root.add(gap(6));
    root.add(toast("✓", SUCCESS, "Reservation Extended",
        "Reservation successfully extended until " + formatDateTime(currentReservation.getEndTime()) + "."));
    root.add(gap(8));
    JPanel det = card("Updated Reservation Details");
    det.add(detailGrid(reservationDetailRows(currentReservation, true), new int[] { 5 }));
    root.add(det);
    root.add(gap(10));
    JPanel act = card("Actions");
    JPanel bar = grid(2, 2);
    bar.add(btn("Stop", INFO, e -> show("stop_confirm")));
    bar.add(btn("Cancel", DANGER, e -> show("cancel_confirm")));
    bar.add(btn("No-Show Cancel", WARN, e -> show("noshow_confirm")));
    bar.add(btn("Prolong", ACCENT, e -> show("prolong_form")));
    act.add(bar);
    root.add(act);
    root.add(gap(10));
    root.add(backToReservationsCard());
    return root;
  }

  private JPanel screenProlongMaxDur() {
    long remaining = remainingExtensibleHours(currentReservation);
    JPanel root = root();
    root.add(breadcrumb("Reservations / Manage Reservation", "/ Prolong — Error"));
    root.add(gap(6));
    root.add(toast("✕", DANGER, "Maximum Duration Exceeded",
        "Cannot extend. Maximum reservation duration is " + Reservation.MAX_DURATION_HOURS + " hours.\n" +
            "Remaining extensible time: " + remaining + " hour(s)."));
    root.add(gap(8));
    JPanel w = card("Actions");
    w.add(btnFull("Back to Actions", MUTED, e -> show("main")));
    root.add(w);
    return root;
  }

  private JPanel screenProlongSwapQ() {
    JPanel root = root();
    root.add(breadcrumb("Reservations / Manage Reservation", "/ Prolong — Table Conflict"));
    root.add(gap(6));
    root.add(toast("⚠", WARN, "Table Conflict Detected",
        "Table #" + currentReservation.getAssignedTable().getTableNumber() +
            " is already booked for the extended period.\n" +
            "Would you like to move the reservation to another available table?"));
    root.add(gap(8));
    JPanel w = card("Actions");
    w.add(btnRow(
        btn("No, Cancel Extension", MUTED, e -> show("main")),
        btn("Yes, Move Table", SUCCESS, e -> doProlong())));
    root.add(w);
    return root;
  }

  private JPanel screenProlongSwapped() {
    JPanel root = root();
    root.add(breadcrumb("Reservations / Manage Reservation", "/ Prolong — Table Swapped"));
    root.add(gap(6));
    root.add(toast("↔", INFO, "Table Moved",
        "The previous table was unavailable for the extension.\n" +
            "Reservation has been moved to table #" +
            currentReservation.getAssignedTable().getTableNumber() + "."));
    root.add(gap(6));
    root.add(toast("✓", SUCCESS, "Reservation Extended",
        "Reservation successfully extended until " + formatDateTime(currentReservation.getEndTime()) + "."));
    root.add(gap(8));
    JPanel det = card("Updated Reservation Details");
    det.add(detailGrid(reservationDetailRows(currentReservation, true), new int[] { 2, 5 }));
    root.add(det);
    root.add(gap(10));
    JPanel w = card("Actions");
    w.add(btnFull("Back to Actions", MUTED, e -> show("main")));
    root.add(w);
    root.add(gap(10));
    root.add(backToReservationsCard());
    return root;
  }

  private JPanel screenProlongNoTable() {
    JPanel root = root();
    root.add(breadcrumb("Reservations / Manage Reservation", "/ Prolong — No Table"));
    root.add(gap(6));
    root.add(toast("✕", DANGER, "No Table Available",
        "Cannot extend. Table #" + currentReservation.getAssignedTable().getTableNumber() +
            " is already booked for that period\n" +
            "and no other table is available in this restaurant."));
    root.add(gap(8));
    JPanel w = card("Actions");
    w.add(btnFull("Back to Actions", MUTED, e -> show("main")));
    root.add(w);
    return root;
  }

  private JPanel screenProlongReassign() {
    JPanel root = root();
    root.add(breadcrumb("Reservations / Manage Reservation", "/ Prolong — Reassigned"));
    root.add(gap(6));
    root.add(toast("⇄", WARN, "Employee Reassigned",
        formatEmployee(currentEmployee) + " has reached the maximum active reservations (" +
            Reservation.MAX_ACTIVE_PER_EMP + ").\n" +
            "Reservation reassigned to " + formatEmployee(currentReservation.getAssignedEmployee()) + "."));
    root.add(gap(6));
    root.add(toast("✓", SUCCESS, "Reservation Extended",
        "Reservation successfully extended until " + formatDateTime(currentReservation.getEndTime()) + "."));
    root.add(gap(8));
    JPanel w = card("Notice — Access Revoked");
    w.add(inlineToast("⚠", WARN, "You No Longer Have Access",
        "This reservation has been reassigned to " +
            formatEmployee(currentReservation.getAssignedEmployee()) + ".\n" +
            "You no longer have access to it."));
    w.add(gap(10));
    JPanel bar = grid(2, 2);
    bar.add(btnOff("Stop"));
    bar.add(btnOff("Cancel"));
    bar.add(btnOff("No-Show Cancel"));
    bar.add(btnOff("Prolong"));
    w.add(bar);
    w.add(gap(6));
    w.add(note("All action buttons are disabled — you are no longer the assigned employee."));
    root.add(w);
    root.add(gap(10));
    root.add(backToReservationsCard());
    return root;
  }

  private JPanel screenProlongNoEmp() {
    JPanel root = root();
    root.add(breadcrumb("Reservations / Manage Reservation", "/ Prolong — No Employee"));
    root.add(gap(6));
    root.add(toast("✕", DANGER, "No Eligible Employee Available",
        "Cannot extend. The assigned employee has reached the maximum\n" +
            "active reservations and no other eligible employee is available."));
    root.add(gap(8));
    JPanel w = card("Actions");
    w.add(btnFull("Back to Actions", MUTED, e -> show("main")));
    root.add(w);
    return root;
  }

  // ==================================================================
  // Reservation display helpers
  // ==================================================================

  private String[][] reservationDetailRows(Reservation r, boolean highlightEnd) {
    String endTime = formatDateTime(r.getEndTime()) + (highlightEnd ? "  \u2191" : "");
    return new String[][] {
        { "Guest Name", r.getGuestName() },
        { "Phone", r.getGuestPhoneNumber() },
        { "Table", formatTable(r.getAssignedTable()) },
        { "Assigned Employee", formatEmployee(r.getAssignedEmployee()) },
        { "Start Time", formatDateTime(r.getDateTimeOfReservation()) },
        { "End Time", endTime },
        { "Status", formatStatus(r) },
        { "Type", formatType(r) },
    };
  }

  private static String formatDateTime(LocalDateTime dt) {
    return dt.format(DT_FMT);
  }

  private static String formatEmployee(Employee e) {
    return e.getName() + " " + e.getSurname();
  }

  private static String formatTable(Restaurant.Table t) {
    String loc = t.getLocation().name();
    String suffix;
    try {
      suffix = t.getSection().name() + " (" + loc + ")";
    } catch (Exception ex) {
      suffix = loc;
    }
    return "#" + t.getTableNumber() + " — " + t.getTableType() + " (" + suffix + ")";
  }

  private static String formatStatus(Reservation r) {
    if (!r.isActive()) {
      return Reservation.getExtent().contains(r) ? "Stopped" : "Cancelled";
    }
    long hours = Duration.between(r.getDateTimeOfReservation(), r.getEndTime()).toHours();
    return hours > Reservation.DURATION_HOURS ? "Prolonged" : "Active";
  }

  private static String formatType(Reservation r) {
    boolean celebration = false;
    boolean priv = false;
    try {
      r.hasOccasion();
      celebration = true;
    } catch (Exception ignored) {
    }
    try {
      r.hasSecurityRequired();
      priv = true;
    } catch (Exception ignored) {
    }
    if (celebration && priv) {
      return "Celebration + Private";
    }
    if (celebration) {
      return "Celebration";
    }
    if (priv) {
      return "Private";
    }
    return "Plain";
  }

  private static long remainingExtensibleHours(Reservation r) {
    long current = Duration.between(r.getDateTimeOfReservation(), r.getEndTime()).toHours();
    return Math.max(0, Reservation.MAX_DURATION_HOURS - current);
  }

  // ==================================================================
  // Extent loading & startup selection
  // ==================================================================

  private static final int DIALOG_CONFIRM = 0;
  private static final int DIALOG_CANCEL = 1;
  private static final int DIALOG_EXIT = 2;

  private static int selectEmployee(Employee[] selectedOut) {
    List<Employee> employees = Employee.getExtent().stream()
        .filter(Employee::canBeAssignedToReservation)
        .toList();
    if (employees.isEmpty()) {
      JOptionPane.showOptionDialog(null,
          "No Waiter or Intern employees are available.",
          "Select Employee",
          JOptionPane.DEFAULT_OPTION,
          JOptionPane.WARNING_MESSAGE,
          null,
          new String[] { "Exit" },
          "Exit");
      return DIALOG_EXIT;
    }
    return showSelectionDialog(
        "Select Employee",
        "Choose the employee managing this session (Waiter or Intern only):",
        employees,
        e -> formatEmployee(e) + "  [" + e.getRole() + "]",
        selectedOut);
  }

  private static int selectReservation(Employee employee, Reservation[] selectedOut) {
    List<Reservation> active = employee.getReservations().stream()
        .filter(Reservation::isActive)
        .toList();
    if (active.isEmpty()) {
      int choice = JOptionPane.showOptionDialog(null,
          "No active reservations assigned to " + formatEmployee(employee) + ".",
          "Select Reservation",
          JOptionPane.DEFAULT_OPTION,
          JOptionPane.INFORMATION_MESSAGE,
          null,
          new String[] { "Back", "Exit" },
          "Back");
      return choice == 1 ? DIALOG_EXIT : DIALOG_CANCEL;
    }
    return showSelectionDialog(
        "Select Reservation",
        "Choose an active reservation for " + formatEmployee(employee) + ":",
        active,
        Reservation::getShortInfo,
        selectedOut);
  }

  private static <T> int showSelectionDialog(String title, String message, List<T> items,
      Function<T, String> labeler, T[] selectedOut) {
    String[] labels = items.stream().map(labeler).toArray(String[]::new);
    JList<String> list = new JList<>(labels);
    list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    list.setSelectedIndex(0);
    list.setVisibleRowCount(Math.min(8, items.size()));

    final int[] action = { DIALOG_CANCEL };
    JDialog dialog = new JDialog((Frame) null, title, true);
    dialog.setLayout(new BorderLayout(10, 10));
    dialog.getContentPane().setBackground(BG);

    JLabel msg = new JLabel("<html>" + message + "</html>");
    msg.setBorder(new EmptyBorder(12, 12, 0, 12));
    msg.setForeground(TEXT);
    msg.setFont(F_SANS_M);
    dialog.add(msg, BorderLayout.NORTH);

    JScrollPane scroll = new JScrollPane(list);
    scroll.setBorder(new EmptyBorder(8, 12, 8, 12));
    dialog.add(scroll, BorderLayout.CENTER);

    JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
    buttons.setBackground(BG);
    JButton selectBtn = new JButton("Select");
    JButton cancelBtn = new JButton("Cancel");
    JButton exitBtn = new JButton("Exit");
    selectBtn.addActionListener(e -> {
      selectedOut[0] = items.get(list.getSelectedIndex());
      action[0] = DIALOG_CONFIRM;
      dialog.dispose();
    });
    cancelBtn.addActionListener(e -> {
      action[0] = DIALOG_CANCEL;
      dialog.dispose();
    });
    exitBtn.addActionListener(e -> {
      action[0] = DIALOG_EXIT;
      dialog.dispose();
    });
    buttons.add(selectBtn);
    buttons.add(cancelBtn);
    buttons.add(exitBtn);
    dialog.add(buttons, BorderLayout.SOUTH);

    dialog.pack();
    dialog.setMinimumSize(new Dimension(480, 280));
    dialog.setLocationRelativeTo(null);
    dialog.setVisible(true);
    return action[0];
  }

  private static void runStartupFlow() {
    showEmployeeDialog();
  }

  private static void showEmployeeDialog() {
    Employee[] employeeHolder = new Employee[1];
    int employeeAction = selectEmployee(employeeHolder);
    if (employeeAction == DIALOG_EXIT) {
      System.exit(0);
    }
    if (employeeAction != DIALOG_CONFIRM) {
      showEmployeeDialog();
      return;
    }
    showReservationDialog(employeeHolder[0]);
  }

  private static void showReservationDialog(Employee employee) {
    Reservation[] reservationHolder = new Reservation[1];
    int reservationAction = selectReservation(employee, reservationHolder);
    if (reservationAction == DIALOG_EXIT) {
      System.exit(0);
    }
    if (reservationAction == DIALOG_CANCEL) {
      showEmployeeDialog();
      return;
    }
    new ManageReservationGUI(employee, reservationHolder[0], () ->
        SwingUtilities.invokeLater(() -> showReservationDialog(employee)));
  }

  // ==================================================================
  // COMPONENT HELPERS
  // ==================================================================

  private JPanel root() {
    JPanel p = new JPanel();
    p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
    p.setBackground(BG);
    p.setBorder(new EmptyBorder(18, 18, 18, 18));
    return p;
  }

  private JPanel card(String title) {
    JPanel body = new JPanel();
    body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
    body.setBackground(SURFACE);
    body.setAlignmentX(Component.LEFT_ALIGNMENT);
    body.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
    body.setBorder(new CardBorder(title));
    return body;
  }

  private JPanel breadcrumb(String base, String active) {
    JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 0));
    p.setBackground(BG);
    p.setAlignmentX(Component.LEFT_ALIGNMENT);
    p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
    JLabel b = lbl(base, MUTED, F_MONO_S);
    JLabel a = lbl(active, ACCENT, F_MONO_S);
    p.add(b);
    p.add(a);
    return p;
  }

  private JPanel detailGrid(String[][] rows, int[] highlightedIndices) {
    JPanel grid = new JPanel(new GridLayout(0, 2, 14, 10));
    grid.setBackground(SURFACE);
    grid.setAlignmentX(Component.LEFT_ALIGNMENT);
    grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

    for (int i = 0; i < rows.length; i++) {
      boolean hi = false;
      if (highlightedIndices != null) {
        for (int h : highlightedIndices) {
          if (h == i) {
            hi = true;
            break;
          }
        }
      }
      String key = rows[i][0];
      String val = rows[i][1];
      if (key.equals("Status")) {
        grid.add(detailItemBadge(key, val));
      } else {
        grid.add(detailItem(key, val, hi ? WARN : TEXT));
      }
    }
    return grid;
  }

  private JPanel detailItem(String label, String value, Color vc) {
    JPanel p = new JPanel();
    p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
    p.setBackground(SURFACE);
    p.add(lbl(label.toUpperCase(), MUTED, F_MONO_S));
    p.add(gap(2));
    p.add(lbl(value, vc, F_SANS_B));
    return p;
  }

  private JPanel detailItemBadge(String label, String value) {
    JPanel p = new JPanel();
    p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
    p.setBackground(SURFACE);
    p.add(lbl(label.toUpperCase(), MUTED, F_MONO_S));
    p.add(gap(3));
    JLabel badge = new JLabel(" " + value + " ");
    badge.setFont(F_MONO_S);
    badge.setOpaque(true);
    badge.setBorder(new EmptyBorder(2, 6, 2, 6));
    switch (value) {
      case "Active" -> {
        badge.setForeground(SUCCESS);
        badge.setBackground(new Color(0x1a, 0x3a, 0x2a));
      }
      case "Stopped" -> {
        badge.setForeground(INFO);
        badge.setBackground(new Color(0x1a, 0x2a, 0x3a));
      }
      case "Prolonged" -> {
        badge.setForeground(WARN);
        badge.setBackground(new Color(0x2a, 0x2a, 0x1a));
      }
      case "Cancelled" -> {
        badge.setForeground(DANGER);
        badge.setBackground(new Color(0x3a, 0x1a, 0x1a));
      }
      default -> {
        badge.setForeground(ACCENT);
        badge.setBackground(new Color(0x1e, 0x22, 0x35));
      }
    }
    p.add(badge);
    return p;
  }

  private JPanel toast(String icon, Color c, String title, String msg) {
    JPanel outer = new JPanel();
    outer.setLayout(new BoxLayout(outer, BoxLayout.Y_AXIS));
    outer.setBackground(BG);
    outer.setAlignmentX(Component.LEFT_ALIGNMENT);
    outer.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
    outer.add(toastPanel(icon, c, title, msg));
    return outer;
  }

  private JPanel toastPanel(String icon, Color c, String title, String msg) {
    JPanel p = new JPanel(new BorderLayout(10, 0));
    p.setBackground(blend(c, BG, 0.09f));
    p.setBorder(new CompoundBorder(
        new MatteBorder(0, 3, 0, 0, c),
        new EmptyBorder(11, 13, 11, 13)));
    p.setAlignmentX(Component.LEFT_ALIGNMENT);
    p.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

    JLabel ico = lbl(icon, c, new Font("SansSerif", Font.BOLD, 14));
    ico.setVerticalAlignment(SwingConstants.TOP);
    p.add(ico, BorderLayout.WEST);

    JPanel right = new JPanel();
    right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
    right.setBackground(blend(c, BG, 0.09f));
    right.add(lbl(title.toUpperCase(), c, F_MONO_S));
    right.add(gap(3));
    for (String line : msg.split("\n")) {
      right.add(lbl(line, c, F_SANS_M));
    }
    p.add(right, BorderLayout.CENTER);
    return p;
  }

  private JPanel inlineToast(String icon, Color c, String title, String msg) {
    JPanel p = toastPanel(icon, c, title, msg);
    p.setAlignmentX(Component.LEFT_ALIGNMENT);
    p.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
    return p;
  }

  private JButton btn(String text, Color c, ActionListener al) {
    JButton b = new JButton(text.toUpperCase());
    b.setFont(F_MONO_M);
    b.setForeground(c);
    b.setBackground(SURFACE);
    b.setFocusPainted(false);
    b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    b.setBorder(new CompoundBorder(
        new LineBorder(c, 1, true),
        new EmptyBorder(9, 14, 9, 14)));
    b.addMouseListener(new MouseAdapter() {
      public void mouseEntered(MouseEvent e) {
        b.setBackground(blend(c, BG, 0.15f));
      }

      public void mouseExited(MouseEvent e) {
        b.setBackground(SURFACE);
      }
    });
    b.addActionListener(al);
    return b;
  }

  private JButton btnFull(String text, Color c, ActionListener al) {
    JButton b = btn(text, c, al);
    b.setAlignmentX(Component.LEFT_ALIGNMENT);
    b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
    return b;
  }

  private JButton btnOff(String text) {
    JButton b = new JButton(text.toUpperCase());
    b.setFont(F_MONO_M);
    b.setForeground(MUTED);
    b.setBackground(SURFACE);
    b.setFocusPainted(false);
    b.setEnabled(false);
    b.setBorder(new CompoundBorder(
        new LineBorder(BORDER, 1, true),
        new EmptyBorder(9, 14, 9, 14)));
    return b;
  }

  private JPanel btnRow(JButton left, JButton right) {
    JPanel p = grid(1, 2);
    p.add(left);
    p.add(right);
    return p;
  }

  private JPanel grid(int rows, int cols) {
    JPanel p = new JPanel(new GridLayout(rows, cols, 8, 8));
    p.setBackground(SURFACE);
    p.setAlignmentX(Component.LEFT_ALIGNMENT);
    p.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
    return p;
  }

  private JTextArea dialogText(String text) {
    JTextArea ta = new JTextArea(text);
    ta.setFont(F_SANS_M);
    ta.setForeground(TEXT);
    ta.setBackground(new Color(0x12, 0x15, 0x1f));
    ta.setEditable(false);
    ta.setLineWrap(true);
    ta.setWrapStyleWord(true);
    ta.setBorder(new CompoundBorder(
        new LineBorder(BORDER, 1, true),
        new EmptyBorder(11, 13, 11, 13)));
    ta.setAlignmentX(Component.LEFT_ALIGNMENT);
    ta.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
    return ta;
  }

  private JLabel fieldLabel(String text) {
    return lbl(text.toUpperCase(), MUTED, F_MONO_S);
  }

  private JLabel hint(String text) {
    return lbl(text, MUTED, F_MONO_S);
  }

  private JLabel note(String text) {
    JLabel l = new JLabel("<html><i>" + text + "</i></html>");
    l.setFont(F_SANS_M);
    l.setForeground(MUTED);
    l.setAlignmentX(Component.LEFT_ALIGNMENT);
    l.setBorder(new CompoundBorder(
        new MatteBorder(0, 2, 0, 0, BORDER),
        new EmptyBorder(3, 8, 3, 0)));
    return l;
  }

  private JSeparator sep() {
    JSeparator s = new JSeparator();
    s.setForeground(BORDER);
    s.setBackground(BORDER);
    s.setAlignmentX(Component.LEFT_ALIGNMENT);
    s.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
    return s;
  }

  private Component gap(int h) {
    return Box.createRigidArea(new Dimension(0, h));
  }

  private JLabel lbl(String text, Color c, Font f) {
    JLabel l = new JLabel(text);
    l.setFont(f);
    l.setForeground(c);
    l.setAlignmentX(Component.LEFT_ALIGNMENT);
    return l;
  }

  private void styleSpinner(JSpinner s) {
    s.setBackground(BG);
    s.setForeground(TEXT);
    s.setFont(F_MONO_M);
    s.setAlignmentX(Component.LEFT_ALIGNMENT);
    s.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
    s.setBorder(new LineBorder(BORDER, 1, true));
    JComponent ed = s.getEditor();
    if (ed instanceof JSpinner.DefaultEditor de) {
      JTextField tf = de.getTextField();
      tf.setBackground(BG);
      tf.setForeground(TEXT);
      tf.setFont(F_MONO_M);
      tf.setCaretColor(TEXT);
      tf.setBorder(new EmptyBorder(6, 8, 6, 8));
    }
  }

  private void show(String id) {
    cards.show(deck, id);
    SwingUtilities.invokeLater(() -> {
      Container parent = deck.getParent();
      if (parent != null) {
        parent = parent.getParent();
      }
      if (parent instanceof JScrollPane sp) {
        sp.getVerticalScrollBar().setValue(0);
      }
    });
  }

  private Color blend(Color c1, Color c2, float a) {
    return new Color(
        clamp((int) (c1.getRed() * a + c2.getRed() * (1 - a))),
        clamp((int) (c1.getGreen() * a + c2.getGreen() * (1 - a))),
        clamp((int) (c1.getBlue() * a + c2.getBlue() * (1 - a))));
  }

  private int clamp(int v) {
    return Math.min(255, Math.max(0, v));
  }

  static class CardBorder extends AbstractBorder {
    private final String title;
    static final int TITLE_H = 26;
    static final int PAD = 14;

    CardBorder(String title) {
      this.title = title;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      g2.setColor(BORDER);
      g2.drawRoundRect(x, y, w - 1, h - 1, 6, 6);

      g2.setColor(BORDER);
      g2.fillRoundRect(x + 1, y + 1, w - 2, TITLE_H, 5, 5);
      g2.fillRect(x + 1, y + TITLE_H / 2, w - 2, TITLE_H / 2);

      g2.setColor(ACCENT);
      g2.fillOval(x + PAD, y + TITLE_H / 2 - 4, 7, 7);

      g2.setColor(MUTED);
      g2.setFont(F_MONO_S.deriveFont(Font.BOLD));
      FontMetrics fm = g2.getFontMetrics();
      g2.drawString(title.toUpperCase(), x + PAD + 14, y + TITLE_H / 2 + fm.getAscent() / 2);

      g2.dispose();
    }

    @Override
    public Insets getBorderInsets(Component c) {
      return new Insets(TITLE_H + PAD, PAD, PAD, PAD);
    }

    @Override
    public Insets getBorderInsets(Component c, Insets insets) {
      insets.set(TITLE_H + PAD, PAD, PAD, PAD);
      return insets;
    }
  }

  // ==================================================================
  // Entry point
  // ==================================================================

  /** Start selection dialogs and open the GUI (extents must already be in memory). */
  public static void launch() {
    try {
      UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
    } catch (Exception ignored) {
    }

    SwingUtilities.invokeLater(ManageReservationGUI::runStartupFlow);
  }
}
