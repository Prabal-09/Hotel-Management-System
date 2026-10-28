package model;

public class Customer {
    private final String id;
    private final String name;
    private final String phone;
    private final String email;
    private final String address;

    public Customer(String id, String name, String phone, String email, String address) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getAddress() {
        return address;
    }

    public String toRecord() {
        return String.join("|", id, name, phone, email, address);
    }

    public static Customer fromRecord(String record) {
        String[] fields = record.split("\\|", -1);
        if (fields.length != 5) {
            throw new IllegalArgumentException("Invalid customer record: " + record);
        }
        return new Customer(fields[0], fields[1], fields[2], fields[3], fields[4]);
    }
}
