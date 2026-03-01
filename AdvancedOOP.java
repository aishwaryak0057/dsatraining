// ================================================================
//   ADVANCED OOP — DESIGN THINKING IN JAVA
//   SOLID + Design Patterns
// ================================================================
//   Compile:  javac AdvancedOOP.java
//   Run:      java AdvancedOOP
// ================================================================
//
//   WHAT IS DESIGN THINKING IN OOP?
//   Writing code that compiles is easy.
//   Writing code that is maintainable, scalable, and extendable
//   is a design problem. This file teaches you the principles
//   and patterns that professional engineers use every day.
//
//   TOPICS COVERED:
//   ── SOLID Principles (5 rules every engineer must know)
//      S — Single Responsibility Principle
//      O — Open/Closed Principle
//      L — Liskov Substitution Principle
//      I — Interface Segregation Principle
//      D — Dependency Inversion Principle
//
//   ── Design Patterns (proven solutions to common problems)
//      1. Singleton Pattern   (Creational)
//      2. Builder Pattern     (Creational)
//      3. Observer Pattern    (Behavioral)
//      4. Strategy Pattern    (Behavioral)
//      5. Decorator Pattern   (Structural)
// ================================================================

import java.util.*;


// ================================================================
//   PART 1: SOLID PRINCIPLES
// ================================================================


// ────────────────────────────────────────────────────────────────
// S — SINGLE RESPONSIBILITY PRINCIPLE (SRP)
// "A class should have ONE reason to change."
//
// BAD DESIGN: One class doing too many things
// ────────────────────────────────────────────────────────────────

// ❌ BAD: This class handles data, formatting, AND saving — 3 responsibilities
class BadReport {
    String title;
    List<String> data;

    BadReport(String title) {
        this.title = title;
        this.data = new ArrayList<>();
    }

    void addData(String line) { data.add(line); }

    // Responsibility 1: format the report
    String format() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== ").append(title).append(" ===\n");
        for (String line : data) sb.append("  • ").append(line).append("\n");
        return sb.toString();
    }

    // Responsibility 2: save to file — WRONG, this belongs elsewhere!
    void saveToFile(String filename) {
        System.out.println("[BadReport] Saving '" + filename + "' — mixed responsibility!");
    }
}

// ✅ GOOD: Split into focused classes — each has ONE job
class Report {
    private final String title;
    private final List<String> data = new ArrayList<>();

    Report(String title) { this.title = title; }

    void addData(String line) { data.add(line); }
    String getTitle()         { return title; }
    List<String> getData()    { return Collections.unmodifiableList(data); }
}

class ReportFormatter {
    // Only job: format a Report into a string
    static String format(Report report) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== ").append(report.getTitle()).append(" ===\n");
        for (String line : report.getData()) sb.append("  • ").append(line).append("\n");
        return sb.toString();
    }
}

class ReportSaver {
    // Only job: save a formatted report
    static void save(String content, String filename) {
        System.out.println("  [ReportSaver] Saving \"" + filename + "\"...");
        System.out.println("  Content preview: " + content.substring(0, Math.min(50, content.length())) + "...");
    }
}


// ────────────────────────────────────────────────────────────────
// O — OPEN/CLOSED PRINCIPLE (OCP)
// "Open for EXTENSION, Closed for MODIFICATION."
// Add new behaviour by ADDING new code, not editing existing code.
// ────────────────────────────────────────────────────────────────

// ❌ BAD: Every new discount type requires editing this class
class BadPricer {
    double getPrice(String customerType, double basePrice) {
        if (customerType.equals("VIP"))      return basePrice * 0.80; // 20% off
        if (customerType.equals("Student"))  return basePrice * 0.90; // 10% off
        // Want to add "Senior"? You must EDIT this method — dangerous!
        return basePrice;
    }
}

// ✅ GOOD: New discount types are EXTENSIONS, not modifications
interface DiscountStrategy {
    double apply(double price);
    String name();
}

class VIPDiscount implements DiscountStrategy {
    public double apply(double price) { return price * 0.80; }
    public String name()              { return "VIP (20% off)"; }
}

class StudentDiscount implements DiscountStrategy {
    public double apply(double price) { return price * 0.90; }
    public String name()              { return "Student (10% off)"; }
}

class SeniorDiscount implements DiscountStrategy {
    // ✅ New type added without touching any existing class!
    public double apply(double price) { return price * 0.85; }
    public String name()              { return "Senior (15% off)"; }
}

class Pricer {
    // This class NEVER needs to change when new discounts are added
    double getPrice(double basePrice, DiscountStrategy strategy) {
        return strategy.apply(basePrice);
    }
}


// ────────────────────────────────────────────────────────────────
// L — LISKOV SUBSTITUTION PRINCIPLE (LSP)
// "A child class must be substitutable for its parent class
//  without breaking the program."
// ────────────────────────────────────────────────────────────────

// ❌ BAD: Penguin extends Bird but can't fly — LSP violated!
class BadBird {
    void fly() { System.out.println("I am flying!"); }
}
class BadPenguin extends BadBird {
    @Override
    void fly() {
        throw new UnsupportedOperationException("Penguins can't fly!"); // 💀 breaks callers
    }
}

// ✅ GOOD: Separate the fly capability — only birds that CAN fly implement it
abstract class Bird {
    abstract void eat();
    abstract void makeSound();
}

interface CanFly {
    void fly();
}

class Sparrow extends Bird implements CanFly {
    public void eat()       { System.out.println("  Sparrow eating seeds"); }
    public void makeSound() { System.out.println("  Sparrow: Tweet tweet!"); }
    public void fly()       { System.out.println("  Sparrow flying through the sky"); }
}

class Penguin extends Bird {
    // Penguin does NOT implement CanFly — honest about its capability
    public void eat()       { System.out.println("  Penguin eating fish"); }
    public void makeSound() { System.out.println("  Penguin: Squawk!"); }
    public void swim()      { System.out.println("  Penguin swimming gracefully"); }
}

// ✅ Any Bird can be substituted safely — no surprises
class BirdHandler {
    static void handleBird(Bird bird) {
        bird.eat();
        bird.makeSound();
        if (bird instanceof CanFly) ((CanFly) bird).fly();
    }
}


// ────────────────────────────────────────────────────────────────
// I — INTERFACE SEGREGATION PRINCIPLE (ISP)
// "No class should be forced to implement methods it doesn't use."
// Split fat interfaces into smaller, focused ones.
// ────────────────────────────────────────────────────────────────

// ❌ BAD: One giant interface forces everyone to implement everything
interface BadMachine {
    void print();
    void scan();
    void fax();   // Old printer has no fax — forced to implement anyway!
}

// ✅ GOOD: Split into small, focused interfaces
interface Printable { void print(); }
interface Scannable  { void scan();  }
interface Faxable    { void fax();   }

// Modern printer can do all three
class ModernPrinter implements Printable, Scannable, Faxable {
    public void print() { System.out.println("  ModernPrinter: Printing document"); }
    public void scan()  { System.out.println("  ModernPrinter: Scanning document"); }
    public void fax()   { System.out.println("  ModernPrinter: Faxing document"); }
}

// Old printer can only print — no forced empty implementations!
class OldPrinter implements Printable {
    public void print() { System.out.println("  OldPrinter: Printing document"); }
}


// ────────────────────────────────────────────────────────────────
// D — DEPENDENCY INVERSION PRINCIPLE (DIP)
// "High-level modules should NOT depend on low-level modules.
//  Both should depend on ABSTRACTIONS (interfaces)."
// ────────────────────────────────────────────────────────────────

// ❌ BAD: OrderService is HARDWIRED to MySQLDatabase
class MySQLDatabase {
    void save(String data) { System.out.println("  Saving to MySQL: " + data); }
}
class BadOrderService {
    private MySQLDatabase db = new MySQLDatabase(); // tightly coupled!

    void placeOrder(String item) {
        System.out.println("  Processing order: " + item);
        db.save(item); // can't swap to MongoDB without rewriting this class
    }
}

// ✅ GOOD: Both depend on the Database interface (the abstraction)
interface Database {
    void save(String data);
}

class MySQL implements Database {
    public void save(String data) { System.out.println("  [MySQL] Saved: " + data); }
}

class MongoDB implements Database {
    public void save(String data) { System.out.println("  [MongoDB] Saved: " + data); }
}

// OrderService talks ONLY to the interface — doesn't care which DB is underneath
class OrderService {
    private final Database db; // depends on abstraction, not implementation

    OrderService(Database db) { this.db = db; } // INJECTED from outside

    void placeOrder(String item) {
        System.out.println("  Processing order: " + item);
        db.save(item);
    }
}


// ================================================================
//   PART 2: DESIGN PATTERNS
// ================================================================


// ────────────────────────────────────────────────────────────────
// PATTERN 1 — SINGLETON (Creational)
//
// Intent: Ensure a class has ONLY ONE instance throughout the program.
// Use when: Configuration manager, logger, database connection pool.
//
// How it works:
//   1. Make constructor PRIVATE — no one can call new Singleton()
//   2. Hold the single instance in a static field
//   3. Provide a static getInstance() method
// ────────────────────────────────────────────────────────────────
class AppConfig {
    private static AppConfig instance; // the one and only instance

    private String theme;
    private String language;
    private boolean darkMode;

    // PRIVATE constructor — cannot be called with 'new' from outside
    private AppConfig() {
        this.theme    = "default";
        this.language = "English";
        this.darkMode = false;
        System.out.println("  [AppConfig] Configuration initialized (only happens once!)");
    }

    // The ONLY way to get the instance
    public static AppConfig getInstance() {
        if (instance == null) {
            instance = new AppConfig(); // created only on first call
        }
        return instance;
    }

    void setTheme(String theme)       { this.theme = theme; }
    void setDarkMode(boolean mode)    { this.darkMode = mode; }
    void setLanguage(String lang)     { this.language = lang; }

    void display() {
        System.out.println("  Config → Theme: " + theme +
                " | Language: " + language + " | DarkMode: " + darkMode);
    }
}


// ────────────────────────────────────────────────────────────────
// PATTERN 2 — BUILDER (Creational)
//
// Intent: Construct complex objects step-by-step.
// Problem it solves:
//   new User("Alice", 25, "alice@email.com", "Engineer", "Chennai", true, false)
//   ← What does 'true' mean? What does 'false' mean? Unreadable!
//
// Builder makes construction readable and flexible.
// ────────────────────────────────────────────────────────────────
class UserProfile {
    // All fields — some required, some optional
    private final String name;       // required
    private final String email;      // required
    private final int age;           // optional
    private final String role;       // optional
    private final String city;       // optional
    private final boolean verified;  // optional

    // Private constructor — only the Builder can call this
    private UserProfile(Builder builder) {
        this.name     = builder.name;
        this.email    = builder.email;
        this.age      = builder.age;
        this.role     = builder.role;
        this.city     = builder.city;
        this.verified = builder.verified;
    }

    void display() {
        System.out.println("  User: " + name + " | Email: " + email +
                " | Age: " + age + " | Role: " + role +
                " | City: " + city + " | Verified: " + verified);
    }

    // ── INNER BUILDER CLASS ──
    static class Builder {
        // Required fields
        private final String name;
        private final String email;

        // Optional fields with sensible defaults
        private int age       = 0;
        private String role   = "Guest";
        private String city   = "Unknown";
        private boolean verified = false;

        // Constructor only takes required fields
        Builder(String name, String email) {
            this.name  = name;
            this.email = email;
        }

        // Each setter returns 'this' so calls can be CHAINED
        Builder age(int age)              { this.age = age;           return this; }
        Builder role(String role)         { this.role = role;         return this; }
        Builder city(String city)         { this.city = city;         return this; }
        Builder verified(boolean v)       { this.verified = v;        return this; }

        // Final step: build the UserProfile object
        UserProfile build() { return new UserProfile(this); }
    }
}


// ────────────────────────────────────────────────────────────────
// PATTERN 3 — OBSERVER (Behavioral)
//
// Intent: When one object changes state, all dependent objects
//         are automatically notified.
//
// Real world: YouTube channel → subscribers get notified on upload.
//             Stock price changes → traders get alerts.
//             Event listeners in UI frameworks.
//
// Players:
//   Subject (Publisher)  — the one being watched
//   Observer (Subscriber) — the ones watching
// ────────────────────────────────────────────────────────────────
interface Observer {
    void update(String event, String data);
}

interface Subject {
    void subscribe(Observer observer);
    void unsubscribe(Observer observer);
    void notifyObservers(String event, String data);
}

class NewsChannel implements Subject {
    private final String channelName;
    private final List<Observer> subscribers = new ArrayList<>();
    private String latestHeadline;

    NewsChannel(String channelName) { this.channelName = channelName; }

    public void subscribe(Observer observer) {
        subscribers.add(observer);
        System.out.println("  [" + channelName + "] New subscriber added. Total: " + subscribers.size());
    }

    public void unsubscribe(Observer observer) {
        subscribers.remove(observer);
        System.out.println("  [" + channelName + "] Subscriber removed. Total: " + subscribers.size());
    }

    public void notifyObservers(String event, String data) {
        System.out.println("  [" + channelName + "] Broadcasting: " + data);
        for (Observer obs : subscribers) {
            obs.update(event, data); // push to all subscribers
        }
    }

    void publishNews(String headline) {
        this.latestHeadline = headline;
        notifyObservers("BREAKING_NEWS", headline);
    }
}

class EmailSubscriber implements Observer {
    private final String email;
    EmailSubscriber(String email) { this.email = email; }

    public void update(String event, String data) {
        System.out.println("    📧 Email to " + email + ": [" + event + "] " + data);
    }
}

class PushNotificationSubscriber implements Observer {
    private final String deviceId;
    PushNotificationSubscriber(String deviceId) { this.deviceId = deviceId; }

    public void update(String event, String data) {
        System.out.println("    📱 Push to device " + deviceId + ": " + data);
    }
}


// ────────────────────────────────────────────────────────────────
// PATTERN 4 — STRATEGY (Behavioral)
//
// Intent: Define a FAMILY of algorithms, encapsulate each one,
//         and make them interchangeable at RUNTIME.
//
// Problem it solves:
//   You have multiple sorting algorithms. Instead of giant if-else
//   chains to choose one, each algorithm is a pluggable strategy.
//
// Real world: Payment methods (UPI / Card / Cash),
//             Sorting algorithms, Compression algorithms,
//             Navigation routes (fastest / shortest / scenic)
// ────────────────────────────────────────────────────────────────
interface SortStrategy {
    void sort(int[] arr);
    String name();
}

class BubbleSort implements SortStrategy {
    public String name() { return "Bubble Sort O(n²)"; }

    public void sort(int[] arr) {
        int[] a = arr.clone();
        int n = a.length;
        for (int i = 0; i < n - 1; i++)
            for (int j = 0; j < n - i - 1; j++)
                if (a[j] > a[j + 1]) { int t = a[j]; a[j] = a[j+1]; a[j+1] = t; }
        System.out.println("  [BubbleSort] " + Arrays.toString(a));
    }
}

class SelectionSort implements SortStrategy {
    public String name() { return "Selection Sort O(n²)"; }

    public void sort(int[] arr) {
        int[] a = arr.clone();
        int n = a.length;
        for (int i = 0; i < n - 1; i++) {
            int minIdx = i;
            for (int j = i + 1; j < n; j++)
                if (a[j] < a[minIdx]) minIdx = j;
            int t = a[minIdx]; a[minIdx] = a[i]; a[i] = t;
        }
        System.out.println("  [SelectionSort] " + Arrays.toString(a));
    }
}

class JavaBuiltInSort implements SortStrategy {
    public String name() { return "Java Built-in Sort O(n log n)"; }

    public void sort(int[] arr) {
        int[] a = arr.clone();
        Arrays.sort(a);
        System.out.println("  [JavaSort] " + Arrays.toString(a));
    }
}

class Sorter {
    private SortStrategy strategy;

    Sorter(SortStrategy strategy) { this.strategy = strategy; }

    // ✅ Strategy can be swapped at RUNTIME
    void setStrategy(SortStrategy strategy) {
        System.out.println("  Switching to: " + strategy.name());
        this.strategy = strategy;
    }

    void sort(int[] arr) { strategy.sort(arr); }
}


// ────────────────────────────────────────────────────────────────
// PATTERN 5 — DECORATOR (Structural)
//
// Intent: Add new behaviours to an object DYNAMICALLY at runtime
//         without altering its class or using inheritance.
//
// Think of it like wrapping a gift:
//   plain box → wrap in paper → add ribbon → add bow
//   Each layer ADDS to the previous one without changing it.
//
// Real world: Java I/O streams (BufferedReader wrapping FileReader),
//             Coffee shop orders (Espresso + Milk + Sugar + Caramel),
//             UI component styling, HTTP request/response filters.
// ────────────────────────────────────────────────────────────────
interface Coffee {
    String getDescription();
    double getCost();
}

// BASE component — the plain coffee
class Espresso implements Coffee {
    public String getDescription() { return "Espresso"; }
    public double getCost()        { return 50.0; }
}

class PlainCoffee implements Coffee {
    public String getDescription() { return "Plain Coffee"; }
    public double getCost()        { return 30.0; }
}

// BASE decorator — wraps a Coffee and delegates to it
abstract class CoffeeDecorator implements Coffee {
    protected final Coffee coffee; // the object being decorated

    CoffeeDecorator(Coffee coffee) { this.coffee = coffee; }

    public String getDescription() { return coffee.getDescription(); }
    public double getCost()        { return coffee.getCost(); }
}

// CONCRETE decorators — each adds something
class MilkDecorator extends CoffeeDecorator {
    MilkDecorator(Coffee coffee) { super(coffee); }

    public String getDescription() { return coffee.getDescription() + " + Milk"; }
    public double getCost()        { return coffee.getCost() + 15.0; }
}

class SugarDecorator extends CoffeeDecorator {
    SugarDecorator(Coffee coffee) { super(coffee); }

    public String getDescription() { return coffee.getDescription() + " + Sugar"; }
    public double getCost()        { return coffee.getCost() + 5.0; }
}

class CaramelDecorator extends CoffeeDecorator {
    CaramelDecorator(Coffee coffee) { super(coffee); }

    public String getDescription() { return coffee.getDescription() + " + Caramel"; }
    public double getCost()        { return coffee.getCost() + 20.0; }
}

class WhipDecorator extends CoffeeDecorator {
    WhipDecorator(Coffee coffee) { super(coffee); }

    public String getDescription() { return coffee.getDescription() + " + Whip"; }
    public double getCost()        { return coffee.getCost() + 25.0; }
}


// ================================================================
//   MAIN CLASS
// ================================================================
public class AdvancedOOP {

    static void section(String title) {
        System.out.println("\n╔══════════════════════════════════════════════════════════╗");
        System.out.printf ("║  %-56s║%n", title);
        System.out.println("╚══════════════════════════════════════════════════════════╝");
    }

    static void sub(String title) {
        System.out.println("\n  ── " + title + " ──");
    }

    public static void main(String[] args) {

        System.out.println("╔══════════════════════════════════════════════════════════╗");
        System.out.println("║        ADVANCED OOP — DESIGN THINKING IN JAVA           ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");


        // ════════════════════════════════════════════════════════
        // SOLID PRINCIPLES
        // ════════════════════════════════════════════════════════

        section("SOLID — S: Single Responsibility Principle");
        Report report = new Report("Q3 Sales");
        report.addData("North: ₹1,20,000");
        report.addData("South: ₹98,000");
        report.addData("West: ₹1,05,000");
        String formatted = ReportFormatter.format(report);
        System.out.println("\n" + formatted);
        ReportSaver.save(formatted, "q3_sales.txt");
        System.out.println("\n  ✓ Report, Formatter, Saver — 3 classes, 3 responsibilities.");


        section("SOLID — O: Open/Closed Principle");
        Pricer pricer   = new Pricer();
        double base     = 1000.0;
        DiscountStrategy[] strategies = {
            new VIPDiscount(), new StudentDiscount(), new SeniorDiscount()
        };
        System.out.println("\n  Base price: ₹" + base);
        for (DiscountStrategy s : strategies) {
            double final_price = pricer.getPrice(base, s);
            System.out.printf("  %-20s → ₹%.2f%n", s.name(), final_price);
        }
        System.out.println("\n  ✓ New discount types added without editing Pricer.");


        section("SOLID — L: Liskov Substitution Principle");
        System.out.println();
        Bird[] birds = { new Sparrow(), new Penguin() };
        for (Bird b : birds) {
            BirdHandler.handleBird(b);
        }
        System.out.println("\n  ✓ Penguin never throws an exception — LSP respected.");


        section("SOLID — I: Interface Segregation Principle");
        System.out.println();
        ModernPrinter modern = new ModernPrinter();
        OldPrinter    old    = new OldPrinter();
        modern.print(); modern.scan(); modern.fax();
        old.print();
        System.out.println("\n  ✓ OldPrinter only implements Printable — no empty fax() method.");


        section("SOLID — D: Dependency Inversion Principle");
        System.out.println("\n  Using MySQL:");
        OrderService mysqlService = new OrderService(new MySQL());
        mysqlService.placeOrder("Laptop");

        System.out.println("\n  Switching to MongoDB (zero code change in OrderService):");
        OrderService mongoService = new OrderService(new MongoDB());
        mongoService.placeOrder("Laptop");
        System.out.println("\n  ✓ OrderService unchanged — only the injected DB differs.");


        // ════════════════════════════════════════════════════════
        // DESIGN PATTERNS
        // ════════════════════════════════════════════════════════

        section("PATTERN 1 — Singleton");
        System.out.println();
        AppConfig config1 = AppConfig.getInstance();
        config1.setTheme("dark-blue");
        config1.setDarkMode(true);
        config1.setLanguage("Tamil");

        AppConfig config2 = AppConfig.getInstance(); // gets SAME instance
        System.out.println("\n  Fetching instance again (no re-initialization):");
        config2.display(); // reflects changes made via config1 — SAME OBJECT

        System.out.println("\n  config1 == config2 ? " + (config1 == config2));
        System.out.println("  ✓ Both variables point to the exact same object in memory.");


        section("PATTERN 2 — Builder");
        System.out.println();

        // Readable, flexible, step-by-step construction
        UserProfile user1 = new UserProfile.Builder("Navaneeth", "nav@email.com")
                .age(24)
                .role("Engineer")
                .city("Chennai")
                .verified(true)
                .build();

        UserProfile user2 = new UserProfile.Builder("Priya", "priya@email.com")
                .role("Designer")  // only set what you need
                .build();

        UserProfile user3 = new UserProfile.Builder("Ravi", "ravi@email.com")
                .age(19)
                .verified(false)
                .build();

        user1.display();
        user2.display();
        user3.display();
        System.out.println("\n  ✓ Each user built with only the fields needed — no null arguments.");


        section("PATTERN 3 — Observer");
        System.out.println();
        NewsChannel bbc = new NewsChannel("BBC News");

        Observer emailUser  = new EmailSubscriber("nav@gmail.com");
        Observer phoneUser1 = new PushNotificationSubscriber("DEVICE-001");
        Observer phoneUser2 = new PushNotificationSubscriber("DEVICE-002");

        bbc.subscribe(emailUser);
        bbc.subscribe(phoneUser1);
        bbc.subscribe(phoneUser2);

        System.out.println();
        bbc.publishNews("India wins the Cricket World Cup!");

        System.out.println("\n  [Unsubscribing DEVICE-001]");
        bbc.unsubscribe(phoneUser1);

        System.out.println();
        bbc.publishNews("New climate deal signed at UN summit.");
        System.out.println("\n  ✓ DEVICE-001 no longer receives updates after unsubscribing.");


        section("PATTERN 4 — Strategy");
        int[] data = {64, 25, 12, 22, 11};
        System.out.println("\n  Input: " + Arrays.toString(data));

        Sorter sorter = new Sorter(new BubbleSort());
        sorter.sort(data);

        sorter.setStrategy(new SelectionSort());
        sorter.sort(data);

        sorter.setStrategy(new JavaBuiltInSort());
        sorter.sort(data);

        System.out.println("\n  ✓ Same Sorter class, 3 different algorithms — swapped at runtime.");


        section("PATTERN 5 — Decorator");
        System.out.println();

        // Plain espresso
        Coffee order1 = new Espresso();
        System.out.printf("  %-45s ₹%.0f%n", order1.getDescription(), order1.getCost());

        // Espresso + Milk + Sugar
        Coffee order2 = new SugarDecorator(new MilkDecorator(new Espresso()));
        System.out.printf("  %-45s ₹%.0f%n", order2.getDescription(), order2.getCost());

        // Plain Coffee + Milk + Caramel + Whip + Sugar
        Coffee order3 = new SugarDecorator(
                            new WhipDecorator(
                                new CaramelDecorator(
                                    new MilkDecorator(
                                        new PlainCoffee()))));
        System.out.printf("  %-45s ₹%.0f%n", order3.getDescription(), order3.getCost());

        System.out.println("\n  ✓ Each decorator ADDS behaviour without modifying the Coffee class.");


        // ════════════════════════════════════════════════════════
        // SUMMARY
        // ════════════════════════════════════════════════════════
        section("DESIGN THINKING SUMMARY");
        System.out.println();
        System.out.println("  SOLID PRINCIPLES");
        System.out.println("  ┌─────────────────────────────────────────────────────────┐");
        System.out.println("  │ S — Single Responsibility  One class, one job           │");
        System.out.println("  │ O — Open/Closed            Extend without modifying     │");
        System.out.println("  │ L — Liskov Substitution    Child must honour parent      │");
        System.out.println("  │ I — Interface Segregation  Small focused interfaces      │");
        System.out.println("  │ D — Dependency Inversion   Depend on abstractions        │");
        System.out.println("  └─────────────────────────────────────────────────────────┘");
        System.out.println();
        System.out.println("  DESIGN PATTERNS");
        System.out.println("  ┌─────────────────────────────────────────────────────────┐");
        System.out.println("  │ Singleton   Creational   One instance, global access    │");
        System.out.println("  │ Builder     Creational   Step-by-step object creation   │");
        System.out.println("  │ Observer    Behavioral   Notify many on state change    │");
        System.out.println("  │ Strategy    Behavioral   Swap algorithms at runtime     │");
        System.out.println("  │ Decorator   Structural   Add behaviour without subclass │");
        System.out.println("  └─────────────────────────────────────────────────────────┘");
    }
}
