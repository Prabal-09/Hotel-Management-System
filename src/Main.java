
import java.io.IOException;

import util.FileHandler;

public class Main {

    public static void main(String[] args) {
        try {
            FileHandler.initializeStorage();
            Hotel hotel = new Hotel();
            hotel.start();
        } catch (IOException e) {
            System.out.println("Storage error: " + e.getMessage());
        }
    }
}
