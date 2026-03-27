//Question 1

abstract class Device {
    private String deviceName;
    private boolean powerStatus;

    public Device(String deviceName) {
        this.deviceName = deviceName;
        this.powerStatus = false;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public boolean isPowerOn() {
        return powerStatus;
    }

    public void turnOn() {
        powerStatus = true;
    }

    public void turnOff() {
        powerStatus = false;
    }

    public abstract void displayStatus();
}

class Light extends Device {
    public Light(String name) {
        super(name);
    }

    @Override
    public void displayStatus() {
        System.out.println("Light: " + getDeviceName() +
                " is " + (isPowerOn() ? "ON" : "OFF"));
    }
}

class Thermostat extends Device {
    public Thermostat(String name) {
        super(name);
    }

    @Override
    public void displayStatus() {
        System.out.println("Thermostat: " + getDeviceName() +
                " is " + (isPowerOn() ? "ON" : "OFF"));
    }
}

//Question 2
interface PaymentMethod {
    void processPayment(double amount);
}

class CreditCardPayment implements PaymentMethod {
    public void processPayment(double amount) {
        System.out.println("Processing Credit Card Payment of Rs." + amount);
    }
}

class PayPalPayment implements PaymentMethod {
    public void processPayment(double amount) {
        System.out.println("Processing PayPal Payment of Rs." + amount);
    }
}

class UPIPayment implements PaymentMethod {
    public void processPayment(double amount) {
        System.out.println("Processing UPI Payment of Rs." + amount);
    }
}

class PaymentProcessor {
    public void process(PaymentMethod method, double amount) {
        method.processPayment(amount);
    }
}

//Question 3

interface EmailSender {
    void sendEmail(String message);
}

interface SMSSender {
    void sendSMS(String message);
}

interface PushNotificationSender {
    void sendPushNotification(String message);
}

class EmailNotification implements EmailSender {
    public void sendEmail(String message) {
        System.out.println("Sending Email: " + message);
    }
}

class SMSNotification implements SMSSender {
    public void sendSMS(String message) {
        System.out.println("Sending SMS: " + message);
    }
}

class MobileAppNotification implements PushNotificationSender {
    public void sendPushNotification(String message) {
        System.out.println("Sending Push Notification: " + message);
    }
}
public class Week3solution {
    public static void main(String[] args) 
    {
        System.out.println("=== Smart Home System ===");
        Device light = new Light("Living Room Light");
        Device thermostat = new Thermostat("Bedroom Thermostat");

        light.turnOn();
        thermostat.turnOff();

        light.displayStatus();
        thermostat.displayStatus();
        System.out.println("\n=== Payment System ===");
        PaymentProcessor processor = new PaymentProcessor();

        PaymentMethod credit = new CreditCardPayment();
        PaymentMethod paypal = new PayPalPayment();
        PaymentMethod upi = new UPIPayment();

        processor.process(credit, 1000);
        processor.process(paypal, 500);
        processor.process(upi, 200);
        System.out.println("\n=== Notification System ===");

        EmailSender email = new EmailNotification();
        SMSSender sms = new SMSNotification();
        PushNotificationSender push = new MobileAppNotification();

        email.sendEmail("Hello via Email!");
        sms.sendSMS("Hello via SMS!");
        push.sendPushNotification("Hello via Push Notification!");
    }
}