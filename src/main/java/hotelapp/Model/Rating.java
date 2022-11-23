package hotelapp.Model;

public record Rating(int numReviews, double avgRating, double cleanliness, double service,
                     double roomComfort, double hotelCondition, double convenience, double neighborhood) {
}
