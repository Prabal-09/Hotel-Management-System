
package util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FileHandler {

    private static final String DATA_DIR = "data";

    private static final String[] FILES = {
        "customers.txt",
        "bookings.txt",
        "id-counters.txt"
    };

    // Create the data directory and required text files
    public static void initializeStorage() throws IOException {

        File directory = new File(DATA_DIR);

        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                throw new IOException("Could not create data directory.");
            }
        }

        for (String fileName : FILES) {
            File file = new File(directory, fileName);

            if (!file.exists() && !file.createNewFile()) {
                throw new IOException("Could not create " + fileName);
            }
        }

    }

    // Add a new record without deleting existing records
    public static void appendRecord(
            String fileName, String record) throws IOException {

        File file = new File(DATA_DIR, fileName);

        try (BufferedWriter writer =
                 new BufferedWriter(new FileWriter(file, true))) {

            writer.write(record);
            writer.newLine();
        }
    }

    public static List<String> readAllRecords(String fileName) throws IOException {
        File file = new File(DATA_DIR, fileName);
        List<String> records = new ArrayList<>();

        if (!file.exists()) {
            throw new IOException("File not found: " + file.getPath());
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    records.add(line);
                }
            }
        }
        return records;
    }

    public static void writeRecords(String fileName, List<String> records) throws IOException {
        File file = new File(DATA_DIR, fileName);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, false))) {
            for (String record : records) {
                writer.write(record);
                writer.newLine();
            }
        }
    }

    public static synchronized String nextId(String prefix, int startingValue, int observedMaximum)
            throws IOException {
        Map<String, Integer> counters = new LinkedHashMap<>();
        for (String record : readAllRecords("id-counters.txt")) {
            String[] fields = record.split("\\|", -1);
            if (fields.length != 2 || fields[0].isEmpty()) {
                throw new IOException("Invalid ID counter record: " + record);
            }
            try {
                int value = Integer.parseInt(fields[1]);
                if (value < 0 || counters.put(fields[0], value) != null) {
                    throw new IOException("Invalid or duplicate ID counter: " + record);
                }
            } catch (NumberFormatException e) {
                throw new IOException("Invalid ID counter record: " + record, e);
            }
        }

        int lastUsed = Math.max(startingValue, observedMaximum);
        Integer storedValue = counters.get(prefix);
        if (storedValue != null) {
            lastUsed = Math.max(lastUsed, storedValue);
        }
        int nextValue;
        try {
            nextValue = Math.addExact(lastUsed, 1);
        } catch (ArithmeticException e) {
            throw new IOException("ID counter is exhausted for " + prefix + ".", e);
        }
        counters.put(prefix, nextValue);

        List<String> records = new ArrayList<>();
        for (Map.Entry<String, Integer> counter : counters.entrySet()) {
            records.add(counter.getKey() + "|" + counter.getValue());
        }
        writeRecords("id-counters.txt", records);
        return prefix + nextValue;
    }

}
