import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public final class ExtentPersistence {

    public static final String EXTENT_FILE = "extent.bin";

    private ExtentPersistence() {
    }

    public static boolean exists() {
        return new File(EXTENT_FILE).exists();
    }

    public static void clearAll() {
        Restaurant.clearExtent();
        Employee.clearExtent();
        Reservation.clearExtent();
        Restaurant.Table.clearExtent();
    }

    public static void save() throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(EXTENT_FILE))) {
            Restaurant.writeExtent(oos);
            Employee.writeExtent(oos);
            Reservation.writeExtent(oos);
            Restaurant.Table.writeExtent(oos);
        }
    }

    public static void load() throws IOException, ClassNotFoundException {
        clearAll();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(EXTENT_FILE))) {
            Restaurant.readExtent(ois);
            Employee.readExtent(ois);
            Reservation.readExtent(ois);
            Restaurant.Table.readExtent(ois);
        }
    }
}
