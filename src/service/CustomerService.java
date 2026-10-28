package service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import model.Booking;
import model.Customer;
import util.FileHandler;

public class CustomerService {
    private static final String FILE_NAME = "customers.txt";

    public List<Customer> getAllCustomers() throws IOException {
        List<Customer> customers = new ArrayList<>();
        for (String record : FileHandler.readAllRecords(FILE_NAME)) {
            customers.add(Customer.fromRecord(record));
        }
        return customers;
    }

    public Customer findById(String id) throws IOException {
        for (Customer customer : getAllCustomers()) {
            if (customer.getId().equalsIgnoreCase(id.trim())) {
                return customer;
            }
        }
        return null;
    }

    public Customer register(String name, String phone, String email, String address)
            throws IOException {
        validateField("Name", name);
        validateField("Phone", phone);
        validateField("Email", email);
        validateField("Address", address);

        List<Customer> customers = getAllCustomers();
        String id = nextId(customers);
        Customer customer = new Customer(id, clean(name), clean(phone), clean(email), clean(address));
        FileHandler.appendRecord(FILE_NAME, customer.toRecord());
        return customer;
    }

    public Customer updateContact(String id, String phone, String email, String address)
            throws IOException {
        validateField("Phone", phone);
        validateField("Email", email);
        validateField("Address", address);

        List<Customer> customers = getAllCustomers();
        List<String> updatedRecords = new ArrayList<>();
        Customer updatedCustomer = null;
        for (Customer customer : customers) {
            if (customer.getId().equalsIgnoreCase(id.trim())) {
                updatedCustomer = new Customer(customer.getId(), customer.getName(),
                        clean(phone), clean(email), clean(address));
                updatedRecords.add(updatedCustomer.toRecord());
            } else {
                updatedRecords.add(customer.toRecord());
            }
        }
        if (updatedCustomer == null) {
            throw new IllegalArgumentException("Customer not found: " + id);
        }
        FileHandler.writeRecords(FILE_NAME, updatedRecords);
        return updatedCustomer;
    }

    public void delete(String id, List<Booking> bookings) throws IOException {
        List<Customer> customers = getAllCustomers();
        Customer target = null;
        for (Customer customer : customers) {
            if (customer.getId().equalsIgnoreCase(id.trim())) {
                target = customer;
                break;
            }
        }
        if (target == null) {
            throw new IllegalArgumentException("Customer not found: " + id);
        }
        for (Booking booking : bookings) {
            if (booking.getCustomerId().equalsIgnoreCase(target.getId())
                    && booking.getStatus().equals("CONFIRMED")) {
                throw new IllegalArgumentException(
                        "Customer has an active booking and cannot be deleted.");
            }
        }

        List<String> remaining = new ArrayList<>();
        for (Customer customer : customers) {
            if (!customer.getId().equalsIgnoreCase(target.getId())) {
                remaining.add(customer.toRecord());
            }
        }
        FileHandler.writeRecords(FILE_NAME, remaining);
    }

    private String nextId(List<Customer> customers) throws IOException {
        int max = 100;
        for (Customer customer : customers) {
            max = Math.max(max, numericSuffix(customer.getId(), "C"));
        }
        return FileHandler.nextId("C", 100, max);
    }

    private int numericSuffix(String id, String prefix) {
        try {
            if (id.startsWith(prefix)) {
                return Integer.parseInt(id.substring(prefix.length()));
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid record ID: " + id, e);
        }
        throw new IllegalArgumentException("Invalid record ID: " + id);
    }

    static void validateField(String label, String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(label + " cannot be empty.");
        }
        if (value.indexOf('|') >= 0 || value.indexOf('\n') >= 0 || value.indexOf('\r') >= 0) {
            throw new IllegalArgumentException(label + " cannot contain | or line breaks.");
        }
    }

    static String clean(String value) {
        return value.trim();
    }
}
