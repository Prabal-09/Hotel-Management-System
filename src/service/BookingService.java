package service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import model.Booking;
import util.FileHandler;

public class BookingService {
    private static final String FILE_NAME = "bookings.txt";
    private static final Map<String, Integer> ROOM_RATES;
    private static final Map<String, Integer> ROOM_INVENTORY;

    static {
        Map<String, Integer> rates = new LinkedHashMap<>();
        rates.put("Standard", 1500);
        rates.put("Deluxe", 2500);
        rates.put("Suite", 4000);
        ROOM_RATES = Collections.unmodifiableMap(rates);

        Map<String, Integer> inventory = new LinkedHashMap<>();
        inventory.put("Standard", 10);
        inventory.put("Deluxe", 5);
        inventory.put("Suite", 2);
        ROOM_INVENTORY = Collections.unmodifiableMap(inventory);
    }

    public List<Booking> getAllBookings() throws IOException {
        List<Booking> bookings = new ArrayList<>();
        for (String record : FileHandler.readAllRecords(FILE_NAME)) {
            bookings.add(Booking.fromRecord(record));
        }
        return bookings;
    }

    public Map<String, Integer> getRoomRates() {
        return ROOM_RATES;
    }

    public Map<String, Integer> getAvailability() throws IOException {
        Map<String, Integer> available = new LinkedHashMap<>(ROOM_INVENTORY);
        for (Booking booking : getAllBookings()) {
            if (booking.getStatus().equals("CONFIRMED")) {
                Integer remaining = available.get(booking.getRoomType());
                if (remaining == null || remaining == 0) {
                    throw new IllegalStateException(
                            "Room inventory is inconsistent for " + booking.getRoomType() + ".");
                }
                available.put(booking.getRoomType(), remaining - 1);
            }
        }
        return available;
    }

    public Booking findById(String id) throws IOException {
        for (Booking booking : getAllBookings()) {
            if (booking.getId().equalsIgnoreCase(id.trim())) {
                return booking;
            }
        }
        return null;
    }

    public List<Booking> findByCustomerId(String customerId) throws IOException {
        List<Booking> results = new ArrayList<>();
        for (Booking booking : getAllBookings()) {
            if (booking.getCustomerId().equalsIgnoreCase(customerId.trim())) {
                results.add(booking);
            }
        }
        return results;
    }

    public Booking create(String customerId, String roomType, int nights,
            CustomerService customerService) throws IOException {
        if (customerService.findById(customerId) == null) {
            throw new IllegalArgumentException("Customer not found: " + customerId);
        }
        if (nights <= 0) {
            throw new IllegalArgumentException("Number of nights must be greater than zero.");
        }

        String normalizedRoomType = normalizeRoomType(roomType);
        Map<String, Integer> availability = getAvailability();
        if (availability.get(normalizedRoomType) == 0) {
            throw new IllegalArgumentException("No " + normalizedRoomType + " rooms are available.");
        }

        List<Booking> bookings = getAllBookings();
        String id = nextId(bookings);
        BigDecimal charge = BigDecimal.valueOf(ROOM_RATES.get(normalizedRoomType))
                .multiply(BigDecimal.valueOf(nights));
        Booking booking = new Booking(id, customerId.trim(), normalizedRoomType,
                nights, charge, "CONFIRMED");
        FileHandler.appendRecord(FILE_NAME, booking.toRecord());
        return booking;
    }

    public Booking cancel(String id) throws IOException {
        List<Booking> bookings = getAllBookings();
        List<String> updatedRecords = new ArrayList<>();
        Booking cancelled = null;
        for (Booking booking : bookings) {
            if (booking.getId().equalsIgnoreCase(id.trim())) {
                if (!booking.getStatus().equals("CONFIRMED")) {
                    throw new IllegalArgumentException("Booking is already cancelled.");
                }
                cancelled = booking.withStatus("CANCELLED");
                updatedRecords.add(cancelled.toRecord());
            } else {
                updatedRecords.add(booking.toRecord());
            }
        }
        if (cancelled == null) {
            throw new IllegalArgumentException("Booking not found: " + id);
        }
        FileHandler.writeRecords(FILE_NAME, updatedRecords);
        return cancelled;
    }

    private String normalizeRoomType(String roomType) {
        for (String supported : ROOM_RATES.keySet()) {
            if (supported.equalsIgnoreCase(roomType.trim())) {
                return supported;
            }
        }
        throw new IllegalArgumentException("Unknown room type: " + roomType);
    }

    private String nextId(List<Booking> bookings) throws IOException {
        int max = 100;
        for (Booking booking : bookings) {
            max = Math.max(max, numericSuffix(booking.getId(), "B"));
        }
        return FileHandler.nextId("B", 100, max);
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
}
