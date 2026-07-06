import java.util.ArrayList;
import java.util.Scanner;

class Stock {
    String symbol;
    String name;
    double price;

    Stock(String symbol, String name, double price) {
        this.symbol = symbol;
        this.name = name;
        this.price = price;
    }

    public String toString() {
        return symbol + " (" + name + ") - ₹" + String.format("%.2f", price);
    }
}

class Transaction {
    String type; // Buy or Sell
    Stock stock;
    int quantity;
    double price;

    Transaction(String type, Stock stock, int quantity, double price) {
        this.type = type;
        this.stock = stock;
        this.quantity = quantity;
        this.price = price;
    }

    public String toString() {
        return type + " " + quantity + " shares of " + stock.symbol + " at ₹" + String.format("%.2f", price) + " each";
    }
}

class User {
    String name;
    double balance;
    ArrayList<Transaction> transactions = new ArrayList<>();
    ArrayList<Holding> holdings = new ArrayList<>();

    User(String name, double balance) {
        this.name = name;
        this.balance = balance;
    }

    void buyStock(Stock stock, int quantity) {
        double total = stock.price * quantity;
        if (total > balance) {
            System.out.println("Insufficient balance!");
            return;
        }
        balance -= total;
        transactions.add(new Transaction("Buy", stock, quantity, stock.price));
        boolean found = false;
        for (Holding h : holdings) {
            if (h.stock.symbol.equals(stock.symbol)) {
                h.quantity += quantity;
                found = true;
                break;
            }
        }
        if (!found) holdings.add(new Holding(stock, quantity));
        System.out.println("Bought " + quantity + " shares of " + stock.symbol);
    }

    void sellStock(Stock stock, int quantity) {
        Holding h = null;
        for (Holding hold : holdings) {
            if (hold.stock.symbol.equals(stock.symbol)) {
                h = hold;
                break;
            }
        }
        if (h == null || h.quantity < quantity) {
            System.out.println("Not enough shares to sell!");
            return;
        }
        double total = stock.price * quantity;
        balance += total;
        h.quantity -= quantity;
        if (h.quantity == 0) holdings.remove(h);
        transactions.add(new Transaction("Sell", stock, quantity, stock.price));
        System.out.println("Sold " + quantity + " shares of " + stock.symbol);
    }

    void portfolio() {
        System.out.println("\n--- Portfolio ---");
        System.out.println("Balance: ₹" + String.format("%.2f", balance));
        if (holdings.isEmpty()) {
            System.out.println("No holdings.");
        } else {
            for (Holding h : holdings) {
                System.out.println(h.stock.symbol + " - " + h.quantity + " shares, Current Price: ₹" + String.format("%.2f", h.stock.price));
            }
        }
        System.out.println("--- Transactions ---");
        if (transactions.isEmpty()) System.out.println("No transactions.");
        else transactions.forEach(t -> System.out.println(t));
    }
}

class Holding {
    Stock stock;
    int quantity;
    Holding(Stock stock, int quantity) {
        this.stock = stock;
        this.quantity = quantity;
    }
}

public class Main {
    static ArrayList<Stock> market = new ArrayList<>();
    static User user;
    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        initMarket();
        System.out.print("Enter your name: ");
        String name = sc.nextLine().trim();
        user = new User(name, 100000); // Starting balance ₹100,000

        while (true) {
            System.out.println("\n=== Stock Trading Platform ===");
            System.out.println("1. View Market");
            System.out.println("2. Buy Stock");
            System.out.println("3. Sell Stock");
            System.out.println("4. Portfolio");
            System.out.println("5. Exit");
            System.out.print("Choose: ");
            String choice = sc.nextLine();

            switch (choice) {
                case "1" -> viewMarket();
                case "2" -> buyStock();
                case "3" -> sellStock();
                case "4" -> user.portfolio();
                case "5" -> System.exit(0);
                default -> System.out.println("Invalid option.");
            }
        }
    }

    static void initMarket() {
        market.add(new Stock("TCS", "Tata Consultancy Services", 3500));
        market.add(new Stock("INFY", "Infosys", 1500));
        market.add(new Stock("RELI", "Reliance Industries", 2500));
        market.add(new Stock("HDFCBANK", "HDFC Bank", 1600));
        market.add(new Stock("ICICIBANK", "ICICI Bank", 900));
    }

    static void viewMarket() {
        System.out.println("\n--- Market Stocks ---");
        for (int i = 0; i < market.size(); i++) {
            System.out.println((i + 1) + ". " + market.get(i));
        }
    }

    static void buyStock() {
        viewMarket();
        System.out.print("Select stock number to buy: ");
        int idx = Integer.parseInt(sc.nextLine()) - 1;
        if (idx < 0 || idx >= market.size()) {
            System.out.println("Invalid stock.");
            return;
        }
        System.out.print("Enter quantity: ");
        int qty = Integer.parseInt(sc.nextLine());
        if (qty <= 0) {
            System.out.println("Invalid quantity.");
            return;
        }
        user.buyStock(market.get(idx), qty);
    }

    static void sellStock() {
        if (user.holdings.isEmpty()) {
            System.out.println("No stocks to sell.");
            return;
        }
        System.out.println("\n--- Your Holdings ---");
        for (int i = 0; i < user.holdings.size(); i++) {
            Holding h = user.holdings.get(i);
            System.out.println((i + 1) + ". " + h.stock.symbol + " - " + h.quantity + " shares");
        }
        System.out.print("Select holding number to sell: ");
        int idx = Integer.parseInt(sc.nextLine()) - 1;
        if (idx < 0 || idx >= user.holdings.size()) {
            System.out.println("Invalid selection.");
            return;
        }
        System.out.print("Enter quantity: ");
        int qty = Integer.parseInt(sc.nextLine());
        if (qty <= 0) {
            System.out.println("Invalid quantity.");
            return;
        }
        user.sellStock(user.holdings.get(idx).stock, qty);
    }
}