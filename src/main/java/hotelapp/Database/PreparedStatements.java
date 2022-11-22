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
            "DROP TABLE hotels";

    public static final String ADD_HOTEL =
            "INSERT INTO hotels (hotel_name, hotel_id, lat, lng, addr, city, state, country) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?);";

    public static String getHotelByKeyword(String keyword) {
        return "SELECT * FROM hotels WHERE hotel_name LIKE '%" + keyword + "%';";
    }


    public static final String GET_ALL_HOTELS =
            "SELECT * FROM hotels;";

}