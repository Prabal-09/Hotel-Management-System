import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import model.Booking;
import model.Customer;
import service.BookingService;
import service.CustomerService;

public class Hotel {
    private final CustomerService customerService = new CustomerService();
    private final BookingService bookingService = new BookingService();

    public void start() {
        try (Scanner scanner = new Scanner(System.in)) {
            boolean running = true;
            while (running) {
                displayMainMenu();
                String choice = ask(scanner, "Select an option: ");
                if (choice == null) {
                    break;
                }
                switch (choice.trim()) {
                    case "1":
                        customerMenu(scanner);
                        break;
                    case "2":
                        bookingMenu(scanner);
                        break;
                    case "3":
                        runAction(this::viewAllRecords);
                        break;
                    case "0":
                        running = false;
                        System.out.println("Thank you for using the Hotel Management System.");
                        break;
                    default:
                        System.out.println("Invalid option. Please enter a number from 0 to 3.");
                }
            }
        }
    }

    private void displayMainMenu() {
        System.out.println("\n=== HOTEL MANAGEMENT SYSTEM ===");
        System.out.println("1. Customer management");
        System.out.println("2. Room booking");
        System.out.println("3. View all stored records");
        System.out.println("0. Exit");
    }

    private void customerMenu(Scanner scanner) {
        boolean inMenu = true;
        while (inMenu) {
            System.out.println("\n--- CUSTOMER MANAGEMENT ---");
            System.out.println("1. Register customer");
            System.out.println("2. List customers");
            System.out.println("3. Search customer");
            System.out.println("4. Update contact details");
            System.out.println("5. Delete customer");
            System.out.println("6. View customer booking history");
            System.out.println("0. Back");
            String choice = ask(scanner, "Select an option: ");
            if (choice == null) {
                return;
            }
            switch (choice.trim()) {
                case "1":
                    runAction(() -> registerCustomer(scanner));
                    break;
                case "2":
                    runAction(this::listCustomers);
                    break;
                case "3":
                    runAction(() -> searchCustomer(scanner));
                    break;
                case "4":
                    runAction(() -> updateCustomer(scanner));
                    break;
                case "5":
                    runAction(() -> deleteCustomer(scanner));
                    break;
                case "6":
                    runAction(() -> customerBookingHistory(scanner));
                    break;
                case "0":
                    inMenu = false;
                    break;
                default:
                    System.out.println("Invalid option. Please enter a number from 0 to 6.");
            }
        }
    }

    private void registerCustomer(Scanner scanner) throws IOException {
        String name = ask(scanner, "Name: ");
        String phone = ask(scanner, "Phone: ");
        String email = ask(scanner, "Email: ");
        String address = ask(scanner, "Address: ");
        if (hasMissingInput(name, phone, email, address)) {
            return;
        }
        Customer customer = customerService.register(name, phone, email, address);
        System.out.println("Customer registered successfully.");
        printCustomer(customer);
    }

    private void listCustomers() throws IOException {
        List<Customer> customers = customerService.getAllCustomers();
        if (customers.isEmpty()) {
            System.out.println("No customers found.");
            return;
        }
        for (Customer customer : customers) {
            printCustomer(customer);
        }
    }

    private void searchCustomer(Scanner scanner) throws IOException {
        String id = ask(scanner, "Customer ID: ");
        if (id == null) {
            return;
        }
        Customer customer = customerService.findById(id);
        if (customer == null) {
            System.out.println("Customer not found.");
        } else {
            printCustomer(customer);
        }
    }

    private void updateCustomer(Scanner scanner) throws IOException {
        String id = ask(scanner, "Customer ID: ");
        if (id == null) {
            return;
        }
        String phone = ask(scanner, "New phone: ");
        String email = ask(scanner, "New email: ");
        String address = ask(scanner, "New address: ");
        if (hasMissingInput(phone, email, address)) {
            return;
        }
        printCustomer(customerService.updateContact(id, phone, email, address));
        System.out.println("Contact details updated.");
    }

    private void deleteCustomer(Scanner scanner) throws IOException {
        String id = ask(scanner, "Customer ID to delete: ");
        if (id == null) {
            return;
        }
        customerService.delete(id, bookingService.getAllBookings());
        System.out.println("Customer deleted.");
    }

    private void customerBookingHistory(Scanner scanner) throws IOException {
        String id = ask(scanner, "Customer ID: ");
        if (id == null) {
            return;
        }
        Customer customer = customerService.findById(id);
        if (customer == null) {
            System.out.println("Customer not found.");
            return;
        }
        List<Booking> bookings = bookingService.findByCustomerId(customer.getId());
        if (bookings.isEmpty()) {
            System.out.println("No booking history for " + customer.getId() + ".");
            return;
        }
        for (Booking booking : bookings) {
            printBooking(booking);
        }
    }

    private void bookingMenu(Scanner scanner) {
        boolean inMenu = true;
        while (inMenu) {
            System.out.println("\n--- ROOM BOOKING ---");
            System.out.println("1. Show room availability and rates");
            System.out.println("2. Create booking");
            System.out.println("3. List bookings");
            System.out.println("4. Search booking");
            System.out.println("5. Cancel booking");
            System.out.println("6. View bookings by customer");
            System.out.println("0. Back");
            String choice = ask(scanner, "Select an option: ");
            if (choice == null) {
                return;
            }
            switch (choice.trim()) {
                case "1":
                    runAction(this::showRoomAvailability);
                    break;
                case "2":
                    runAction(() -> createBooking(scanner));
                    break;
                case "3":
                    runAction(this::listBookings);
                    break;
                case "4":
                    runAction(() -> searchBooking(scanner));
                    break;
                case "5":
                    runAction(() -> cancelBooking(scanner));
                    break;
                case "6":
                    runAction(() -> bookingsByCustomer(scanner));
                    break;
                case "0":
                    inMenu = false;
                    break;
                default:
                    System.out.println("Invalid option. Please enter a number from 0 to 6.");
            }
        }
    }

    private void showRoomAvailability() throws IOException {
        Map<String, Integer> availability = bookingService.getAvailability();
        System.out.println("\nRoom type    Rate per night    Available");
        for (Map.Entry<String, Integer> room : availability.entrySet()) {
            System.out.printf("%-12s Rs. %-12d %d%n", room.getKey(),
                    bookingService.getRoomRates().get(room.getKey()), room.getValue());
        }
    }

    private void createBooking(Scanner scanner) throws IOException {
        showRoomAvailability();
        String customerId = ask(scanner, "Customer ID: ");
        String roomType = ask(scanner, "Room type (Standard/Deluxe/Suite): ");
        Integer nights = askInteger(scanner, "Number of nights: ", 1, Integer.MAX_VALUE);
        if (hasMissingInput(customerId, roomType) || nights == null) {
            return;
        }
        Booking booking = bookingService.create(customerId, roomType, nights, customerService);
        System.out.println("Booking created.");
        printBooking(booking);
    }

    private void listBookings() throws IOException {
        List<Booking> bookings = bookingService.getAllBookings();
        if (bookings.isEmpty()) {
            System.out.println("No bookings found.");
            return;
        }
        for (Booking booking : bookings) {
            printBooking(booking);
        }
    }

    private void searchBooking(Scanner scanner) throws IOException {
        String id = ask(scanner, "Booking ID: ");
        if (id == null) {
            return;
        }
        Booking booking = bookingService.findById(id);
        if (booking == null) {
            System.out.println("Booking not found.");
        } else {
            printBooking(booking);
        }
    }

    private void cancelBooking(Scanner scanner) throws IOException {
        String id = ask(scanner, "Booking ID to cancel: ");
        if (id == null) {
            return;
        }
        printBooking(bookingService.cancel(id));
        System.out.println("Booking cancelled.");
    }

    private void bookingsByCustomer(Scanner scanner) throws IOException {
        String id = ask(scanner, "Customer ID: ");
        if (id == null) {
            return;
        }
        if (customerService.findById(id) == null) {
            System.out.println("Customer not found.");
            return;
        }
        List<Booking> bookings = bookingService.findByCustomerId(id);
        if (bookings.isEmpty()) {
            System.out.println("No bookings found for that customer.");
            return;
        }
        for (Booking booking : bookings) {
            printBooking(booking);
        }
    }

    private void viewAllRecords() throws IOException {
        System.out.println("\n--- CUSTOMERS ---");
        listCustomers();
        System.out.println("\n--- BOOKINGS ---");
        listBookings();
    }

    private void printCustomer(Customer customer) {
        System.out.printf("%s | %s | %s | %s | %s%n", customer.getId(), customer.getName(),
                customer.getPhone(), customer.getEmail(), customer.getAddress());
    }

    private void printBooking(Booking booking) {
        System.out.printf("%s | Customer: %s | Room: %s | Nights: %d | Charge: Rs. %s | %s%n",
                booking.getId(), booking.getCustomerId(), booking.getRoomType(),
                booking.getNights(), booking.getRoomCharge().toPlainString(), booking.getStatus());
    }

    private Integer askInteger(Scanner scanner, String prompt, int minimum, int maximum) {
        while (true) {
            String input = ask(scanner, prompt);
            if (input == null) {
                return null;
            }
            try {
                int value = Integer.parseInt(input.trim());
                if (value >= minimum && value <= maximum) {
                    return value;
                }
            } catch (NumberFormatException e) {
                System.out.println("Enter a whole number from " + minimum + " to " + maximum + ".");
                continue;
            }
            System.out.println("Enter a whole number from " + minimum + " to " + maximum + ".");
        }
    }

    private String ask(Scanner scanner, String prompt) {
        System.out.print(prompt);
        return scanner.hasNextLine() ? scanner.nextLine() : null;
    }

    private boolean hasMissingInput(String... values) {
        for (String value : values) {
            if (value == null) {
                return true;
            }
        }
        return false;
    }

    private void runAction(Action action) {
        try {
            action.run();
        } catch (IOException | IllegalArgumentException | IllegalStateException e) {
            System.out.println("Operation failed: " + e.getMessage());
        }
    }

    private interface Action {
        void run() throws IOException;
    }
}
