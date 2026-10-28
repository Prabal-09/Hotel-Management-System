# Hotel Management System

A console-based hotel management application built with Core Java and pipe-separated text files. It uses no external libraries or database.

## Requirements

- JDK 8 or later
- Run commands from the project root directory

## Compile and run

```powershell
javac -d out src\Main.java src\Hotel.java src\util\FileHandler.java src\model\Customer.java src\model\Booking.java src\service\CustomerService.java src\service\BookingService.java
java -cp out Main
```

The application creates `data` and the required record files at startup. Records persist when the application is closed and restarted.

## Features

- **Customers:** register, list, search, update contact details, delete customers without active bookings, and view booking history.
- **Bookings:** show room availability and nightly rates, create/search/list bookings, view bookings by customer, and cancel bookings. A confirmed booking reserves one room of its type until cancelled.

## Sample room rates and inventory

| Room type | Rate per night | Rooms |
|---|---:|---:|
| Standard | Rs. 1,500 | 10 |
| Deluxe | Rs. 2,500 | 5 |
| Suite | Rs. 4,000 | 2 |

These are learning-project sample values, not actual hotel rates.

## Data files

The `data` folder contains `customers.txt`, `bookings.txt`, and `id-counters.txt`. Records are stored one per line using `|` between fields. The application rejects `|` and line breaks in user-entered text so records remain parseable. ID counters are persisted separately so deleting a record does not cause its ID to be reused.

- Customer: `C101|Name|Phone|Email|Address`
- Booking: `B101|C101|Deluxe|2|5000|CONFIRMED`
