import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

class Room {
    String id;
    String category;
    double price;
    Room(String id, String category, double price) { this.id = id; this.category = category; this.price = price; }
    public String toString() { return id + " [" + category + "] - ₹" + String.format(Locale.ROOT,"%.2f",price); }
}

class Reservation {
    String resId;
    String roomId;
    String guest;
    LocalDate from;
    LocalDate to;
    double amount;
    Reservation(String resId, String roomId, String guest, LocalDate from, LocalDate to, double amount) {
        this.resId = resId; this.roomId = roomId; this.guest = guest; this.from = from; this.to = to; this.amount = amount;
    }
    String toCSV() { return String.join(",", resId, roomId, guest, from.toString(), to.toString(), String.format(Locale.ROOT,"%.2f", amount)); }
    static Reservation fromCSV(String line) {
        String[] p = line.split(",", -1);
        return new Reservation(p[0], p[1], p[2], LocalDate.parse(p[3]), LocalDate.parse(p[4]), Double.parseDouble(p[5]));
    }
    public String toString() {
        return String.format("%s | Room:%s | Guest:%s | From:%s | To:%s | ₹%.2f", resId, roomId, guest, from, to, amount);
    }
}

public class Main {
    static ArrayList<Room> rooms = new ArrayList<>();
    static ArrayList<Reservation> bookings = new ArrayList<>();
    static Scanner sc = new Scanner(System.in);
    static File bookFile = new File("bookings.csv");
    static DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static void main(String[] args) {
        initRooms();
        loadBookings();
        while (true) {
            System.out.println("\n1.Search 2.Book 3.Cancel 4.View By ID 5.All Bookings 6.Exit");
            System.out.print("Choice: ");
            String c = sc.nextLine().trim();
            switch (c) {
                case "1" -> handleSearch();
                case "2" -> handleBook();
                case "3" -> handleCancel();
                case "4" -> handleViewById();
                case "5" -> listAll();
                case "6" -> { saveBookings(); System.exit(0); }
                default -> System.out.println("Invalid");
            }
        }
    }

    static void initRooms() {
        rooms.clear();
        rooms.add(new Room("R101","Standard",2000));
        rooms.add(new Room("R102","Standard",2000));
        rooms.add(new Room("R201","Deluxe",3500));
        rooms.add(new Room("R202","Deluxe",3800));
        rooms.add(new Room("R301","Suite",6500));
    }

    static void handleSearch() {
        System.out.print("Category or ENTER for any: ");
        String cat = sc.nextLine().trim();
        System.out.print("Check-in (yyyy-MM-dd): ");
        LocalDate from = readDate();
        System.out.print("Check-out (yyyy-MM-dd): ");
        LocalDate to = readDate();
        if (!to.isAfter(from)) { System.out.println("Check-out must be after check-in."); return; }
        List<Room> avail = searchAvailable(cat.isEmpty() ? null : cat, from, to);
        if (avail.isEmpty()) System.out.println("No available rooms.");
        else avail.forEach(r -> System.out.println(r));
    }

    static void handleBook() {
        System.out.print("Category or ENTER for any: ");
        String cat = sc.nextLine().trim();
        System.out.print("Check-in (yyyy-MM-dd): ");
        LocalDate from = readDate();
        System.out.print("Check-out (yyyy-MM-dd): ");
        LocalDate to = readDate();
        if (!to.isAfter(from)) { System.out.println("Check-out must be after check-in."); return; }
        List<Room> avail = searchAvailable(cat.isEmpty() ? null : cat, from, to);
        if (avail.isEmpty()) { System.out.println("No available rooms."); return; }
        for (int i=0;i<avail.size();i++) System.out.println((i+1)+". "+avail.get(i));
        System.out.print("Choose number: ");
        int idx = Integer.parseInt(sc.nextLine().trim())-1;
        if (idx<0 || idx>=avail.size()) { System.out.println("Invalid selection."); return; }
        Room chosen = avail.get(idx);
        System.out.print("Guest name: ");
        String guest = sc.nextLine().trim();
        long nights = java.time.temporal.ChronoUnit.DAYS.between(from,to);
        double amount = nights * chosen.price;
        System.out.println("Amount: ₹"+String.format(Locale.ROOT,"%.2f",amount));
        System.out.print("Confirm pay (yes to proceed): ");
        String ok = sc.nextLine().trim();
        if (!ok.equalsIgnoreCase("yes")) { System.out.println("Aborted."); return; }
        String resId = "RES-"+UUID.randomUUID().toString().substring(0,8).toUpperCase();
        Reservation r = new Reservation(resId, chosen.id, guest, from, to, amount);
        bookings.add(r);
        saveBookings();
        System.out.println("Booked: "+r);
    }

    static void handleCancel() {
        System.out.print("Reservation ID: ");
        String id = sc.nextLine().trim();
        Reservation found = null;
        for (Reservation r: bookings) if (r.resId.equals(id)) { found = r; break; }
        if (found==null) { System.out.println("Not found."); return; }
        bookings.remove(found);
        saveBookings();
        System.out.println("Cancelled "+id);
    }

    static void handleViewById() {
        System.out.print("Reservation ID: ");
        String id = sc.nextLine().trim();
        bookings.stream().filter(b->b.resId.equals(id)).findFirst().ifPresentOrElse(
                b->System.out.println(b),
                ()->System.out.println("Not found."));
    }

    static void listAll() {
        if (bookings.isEmpty()) System.out.println("No bookings.");
        else bookings.forEach(b->System.out.println(b));
    }

    static LocalDate readDate() {
        while (true) {
            try { return LocalDate.parse(sc.nextLine().trim(), fmt); }
            catch (Exception e) { System.out.print("Invalid, enter yyyy-MM-dd: "); }
        }
    }

    static List<Room> searchAvailable(String category, LocalDate from, LocalDate to) {
        List<Room> res = new ArrayList<>();
        for (Room room: rooms) {
            if (category!=null && !room.category.equalsIgnoreCase(category)) continue;
            boolean ok = true;
            for (Reservation b: bookings) {
                if (!b.roomId.equals(room.id)) continue;
                if (from.isBefore(b.to) && b.from.isBefore(to)) { ok = false; break; }
            }
            if (ok) res.add(room);
        }
        return res;
    }

    static void loadBookings() {
        bookings.clear();
        if (!bookFile.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(bookFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                bookings.add(Reservation.fromCSV(line));
            }
        } catch (Exception e) { System.out.println("Failed load: "+e.getMessage()); }
    }

    static void saveBookings() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(bookFile,false))) {
            for (Reservation r: bookings) pw.println(r.toCSV());
        } catch (Exception e) { System.out.println("Failed save: "+e.getMessage()); }
    }
}