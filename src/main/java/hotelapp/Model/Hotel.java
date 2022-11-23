package hotelapp.Model;

public record Hotel(String name, int hotelId, double lat, double lng, String addr,
                    String city, String state, String country) {
}
