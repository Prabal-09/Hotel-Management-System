package model;

import java.math.BigDecimal;

public class Booking {
    private final String id;
    private final String customerId;
    private final String roomType;
    private final int nights;
    private final BigDecimal roomCharge;
    private final String status;

    public Booking(String id, String customerId, String roomType, int nights,
            BigDecimal roomCharge, String status) {
        this.id = id;
        this.customerId = customerId;
        this.roomType = roomType;
        this.nights = nights;
        this.roomCharge = roomCharge;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getRoomType() {
        return roomType;
    }

    public int getNights() {
        return nights;
    }

    public BigDecimal getRoomCharge() {
        return roomCharge;
    }

    public String getStatus() {
        return status;
    }

    public Booking withStatus(String newStatus) {
        return new Booking(id, customerId, roomType, nights, roomCharge, newStatus);
    }

    public String toRecord() {
        return String.join("|", id, customerId, roomType, Integer.toString(nights),
                roomCharge.toPlainString(), status);
    }

    public static Booking fromRecord(String record) {
        String[] fields = record.split("\\|", -1);
        if (fields.length != 6) {
            throw new IllegalArgumentException("Invalid booking record: " + record);
        }
        try {
            int nights = Integer.parseInt(fields[3]);
            BigDecimal charge = new BigDecimal(fields[4]);
            if (nights <= 0 || charge.signum() < 0
                    || !(fields[5].equals("CONFIRMED") || fields[5].equals("CANCELLED"))) {
                throw new IllegalArgumentException();
            }
            return new Booking(fields[0], fields[1], fields[2], nights, charge, fields[5]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid booking record: " + record, e);
        }
    }
}
