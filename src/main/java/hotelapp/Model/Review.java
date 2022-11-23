package hotelapp.Model;

import java.time.LocalDateTime;

public record Review(String reviewId, int hotelId, String title, String reviewText, String username, LocalDateTime reviewDate, int rating) {
}
