package hotelapp.Model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record Booking(int hotelId, LocalDate startDate, LocalDate endDate, int numRooms, String username, LocalDateTime timeBooked) {
}
