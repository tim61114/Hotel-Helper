package hotelapp.Database;

public class PreparedStatements {
    /** For creating the users table */
    public static final String CREATE_USER_TABLE =
            "CREATE TABLE users (" +
                    "userid INTEGER AUTO_INCREMENT PRIMARY KEY, " +
                    "username VARCHAR(32) NOT NULL UNIQUE, " +
                    "password CHAR(64) NOT NULL, " +
                    "usersalt CHAR(32) NOT NULL);";

    public static final String DROP_USER_TABLE =
            "DROP TABLE users";

    /** Used to insert a new user into the database. */
    public static final String REGISTER_SQL =
            "INSERT INTO users (username, password, usersalt) " +
                    "VALUES (?, ?, ?);";

    /** Used to retrieve the salt associated with a specific user. */
    public static final String SALT_SQL =
            "SELECT usersalt FROM users WHERE username LIKE binary ?";

    /** Used to authenticate a user. */
    public static final String AUTH_SQL =
            "SELECT username FROM users " +
                    "WHERE username LIKE binary ? AND password = ?";

    public static final String CREATE_LOGIN_INFO_TABLE =
            "CREATE TABLE login_info (" +
                    "username VARCHAR(32) PRIMARY KEY, " +
                    "previous_login TIMESTAMP NOT NULL) ";

    public static final String DROP_LOGIN_INFO_TABLE =
            "DROP TABLE login_info";

    public static final String GET_PREVIOUS_LOGIN_BY_USERNAME =
            "SELECT previous_login FROM login_info WHERE username = ?";

    public static final String INSERT_TO_LOGIN_INFO =
            "INSERT INTO login_info (username, previous_login) " +
                    "VALUES(?, ?) ON DUPLICATE KEY UPDATE previous_login=?";

    public static final String CREATE_HOTEL_TABLE =
            "CREATE TABLE hotels (" +
                    "id INTEGER AUTO_INCREMENT PRIMARY KEY, " +
                    "hotel_name VARCHAR(60) NOT NULL UNIQUE, " +
                    "hotel_id INTEGER NOT NULL UNIQUE, " +
                    "lat DOUBLE NOT NULL, " +
                    "lng DOUBLE NOT NULL, " +
                    "addr VARCHAR(32) NOT NULL, " +
                    "city VARCHAR(32) NOT NULL, " +
                    "state VARCHAR(10), " +
                    "country VARCHAR(10));";

    public static final String DROP_HOTEL_TABLE =
            "DROP TABLE hotels;";

    public static final String ADD_HOTEL =
            "INSERT INTO hotels (hotel_name, hotel_id, lat, lng, addr, city, state, country) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?);";

    public static String getHotelByKeyword(String keyword) {
        return "SELECT * FROM hotels WHERE hotel_name LIKE '%" + keyword + "%';";
    }

    public static final String GET_ALL_HOTELS =
            "SELECT * FROM hotels;";

    public static final String GET_HOTEL_BY_ID =
            "SELECT * FROM hotels WHERE hotel_id = ?;";

    public static final String GET_ALL_HOTEL_ID =
            "SELECT hotel_id FROM hotels;";

    public static final String CREATE_EXPEDIA_HISTORY_TABLE =
            "CREATE TABLE expedia_history (" +
                    "id INTEGER AUTO_INCREMENT PRIMARY KEY, " +
                    "username VARCHAR(20) NOT NULL, " +
                    "hotel_id INTEGER NOT NULL, " +
                    "time TIMESTAMP);";

    public static final String DROP_EXPEDIA_HISTORY_TABLE =
            "DROP TABLE expedia_history;";

    public static final String GET_USER_EXPEDIA_HISTORY =
            "SELECT hotel_name, hotels.hotel_id, time FROM " +
                    "(SELECT * FROM expedia_history WHERE username = ?) a " +
                    "INNER JOIN hotels on a.hotel_id = hotels.hotel_id ORDER BY time DESC";

    public static final String DELETE_USER_EXPEDIA_HISTORY =
            "DELETE FROM expedia_history WHERE username = ?";

    public static final String ADD_USER_EXPEDIA_HISTORY =
            "INSERT INTO expedia_history (username, hotel_id, time) " +
                    "VALUES (?, ?, ?);";

    public static final String CREATE_REVIEW_TABLE =
            "CREATE TABLE reviews (" +
                    "id INTEGER AUTO_INCREMENT PRIMARY KEY, " +
                    "review_id VARCHAR(32) NOT NULL UNIQUE, " +
                    "hotel_id INTEGER NOT NULL , " +
                    "title VARCHAR(60) NOT NULL, " +
                    "reviewText TEXT NOT NULL, " +
                    "userNickname VARCHAR(20), " +
                    "reviewDate DATETIME, " +
                    "rating INTEGER);";

    public static final String DROP_REVIEW_TABLE =
            "DROP TABLE reviews;";

    public static final String ADD_REVIEW =
            "INSERT INTO reviews (review_id, hotel_id, title, reviewText, userNickname, reviewDate, rating) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?);";

    public static final String DELETE_REVIEW_BY_REVIEW_ID =
            "DELETE FROM reviews WHERE review_id = ?";

    public static final String GET_REVIEW_BY_HOTEL_ID =
            "SELECT * FROM reviews WHERE hotel_id = ? ORDER BY reviewDate DESC;";

    public static final String GET_REVIEW_BY_REVIEW_ID =
            "SELECT * FROM reviews WHERE review_id = ?;";

    public static final String GET_AVERAGE_RATING_BY_HOTEL_ID =
            "SELECT AVG(rating) FROM reviews WHERE hotel_id = ?;";

    public static final String CREATE_RATING_TABLE =
            "CREATE TABLE ratings (" +
                    "hotel_id INTEGER NOT NULL UNIQUE, " +
                    "num_reviews INTEGER, " +
                    "avg_rating DOUBLE, " +
                    "cleanliness DOUBLE, " +
                    "service DOUBLE, " +
                    "room_comfort DOUBLE, " +
                    "hotel_condition DOUBLE, " +
                    "convenience DOUBLE, " +
                    "neighborhood DOUBLE);";

    public static final String DROP_RATING_TABLE =
            "DROP TABLE ratings";

    public static final String ADD_RATING =
            "INSERT INTO ratings (hotel_id, num_reviews, avg_rating, cleanliness, service, room_comfort, hotel_condition, convenience, neighborhood) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    public static final String GET_RATING_BY_HOTEL_ID =
            "SELECT * FROM ratings WHERE hotel_id = ?;";

    public static final String CREATE_BOOKING_TABLE =
            "CREATE TABLE bookings (" +
                    "id INTEGER AUTO_INCREMENT PRIMARY KEY, " +
                    "booking_id VARCHAR(32) NOT NULL UNIQUE, " +
                    "hotel_id INTEGER NOT NULL , " +
                    "startDate DATE NOT NULL, " +
                    "endDate DATE NOT NULL, " +
                    "numRooms INTEGER NOT NULL, " +
                    "username VARCHAR(20) NOT NULL, " +
                    "timeBooked TIMESTAMP NOT NULL);";

    public static final String DROP_BOOKING_TABLE =
            "DROP TABLE bookings";

    public static final String ADD_BOOKING =
            "INSERT INTO bookings (booking_id, hotel_id, startDate, endDate, numRooms, username, timeBooked) " +
                    "VALUES(?, ?, ?, ?, ?, ?, ?)";

    public static final String GET_ALL_BOOKING =
            "SELECT hotel_name, a.* FROM hotels INNER JOIN (SELECT * FROM bookings) a on a.hotel_id = hotels.hotel_id";

    public static final String GET_USER_BOOKING =
            "SELECT hotel_name, a.* FROM hotels INNER JOIN (SELECT * FROM bookings WHERE username = ?) a on a.hotel_id = hotels.hotel_id ORDER BY startDate ASC";

    public static final String GET_BOOKING_BY_BOOKING_ID =
            "SELECT hotel_name, a.* FROM hotels INNER JOIN (SELECT * FROM bookings WHERE booking_id = ?) a ON a.hotel_id = hotels.hotel_id";

    public static final String DELETE_BOOKING =
            "DELETE FROM bookings WHERE booking_id = ?";


    public static final String CREATE_FAVORITES_TABLE =
            "CREATE TABLE favorites (" +
                    "id INTEGER AUTO_INCREMENT PRIMARY KEY, " +
                    "hotel_id INTEGER NOT NULL, " +
                    "username VARCHAR(20) NOT NULL)";

    public static final String ADD_FAVORITE =
            "INSERT INTO favorites (hotel_id, username) " +
                    "VALUES(?, ?)";

    public static final String DELETE_USER_FAVORITE =
            "DELETE FROM favorites WHERE username = ?";

    public static final String DELETE_FAVORITE =
            "DELETE FROM favorites WHERE hotel_id = ? AND username = ?";

    public static final String DROP_FAVORITES_TABLE =
            "DROP TABLE favorites";

    public static final String CHECK_IS_FAVORITE_HOTEL =
            "SELECT * FROM favorites WHERE hotel_id = ? AND username = ?";

    public static final String GET_USER_FAVORITES =
            "SELECT hotels.* FROM hotels INNER JOIN (SELECT * FROM favorites WHERE username = ?) a ON a.hotel_id = hotels.hotel_id";
}