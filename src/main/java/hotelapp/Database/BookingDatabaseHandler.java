package hotelapp.Database;

import hotelapp.Model.Booking;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BookingDatabaseHandler {
    private final DatabaseHandler dbHandler = DatabaseHandler.getInstance();

    public boolean createBookingTable() {
        Connection dbConnection = dbHandler.getConnection();
        if (dbConnection == null) {
            System.out.println("Unable to connect to database.");
            return false;
        }
        try {
            PreparedStatement statement = dbConnection.prepareStatement(PreparedStatements.CREATE_BOOKING_TABLE);
            statement.executeUpdate();
        } catch (SQLException e) {
            if (e.getErrorCode() == DatabaseErrorCodes.TABLE_EXISTS) {
                System.out.println("User table already exists.");
            } else {
                System.out.println(e.getMessage());
                System.out.println(e.getErrorCode());
                System.out.println("An error occurred.");
            }
            return false;
        }
        return true;
    }

    public boolean dropBookingTable() {
        Connection dbConnection = dbHandler.getConnection();
        if (dbConnection == null) {
            System.out.println("Unable to connect to database.");
            return false;
        }
        try {
            PreparedStatement statement = dbConnection.prepareStatement(PreparedStatements.DROP_BOOKING_TABLE);
            statement.executeUpdate();
        } catch (SQLException e) {
            if (e.getErrorCode() == DatabaseErrorCodes.TABLE_DOES_NOT_EXIST) {
                System.out.println("Table hotels does not exist.");
            } else {
                System.out.println("An error occurred.");
            }
            return false;
        }
        return true;
    }

    public void addBooking(Booking booking) {
        Connection dbConnection = dbHandler.getConnection();
        if (dbConnection == null) {
            System.out.println("Unable to connect to database.");
            return;
        }

        String bookingId = createBookingId(booking);
        try {
            PreparedStatement statement = dbConnection.prepareStatement(PreparedStatements.ADD_BOOKING);
            statement.setString(1, bookingId);
            statement.setInt(2, booking.hotelId());
            statement.setDate(3, Date.valueOf(booking.startDate()));
            statement.setDate(4, Date.valueOf(booking.endDate()));
            statement.setInt(5, booking.numRooms());
            statement.setString(6, booking.username());
            statement.setTimestamp(7, Timestamp.valueOf(booking.timeBooked()));
            statement.executeUpdate();
            statement.close();

        } catch (SQLException e) {
            System.out.println(e.getErrorCode());
            System.out.println(e.getMessage());
        }
    }

    public List<Booking> getUserBooking(String username) {
        Connection dbConnection = dbHandler.getConnection();
        if (dbConnection == null) {
            System.out.println("Unable to connect to database.");
            return new ArrayList<>();
        }

        List<Booking> bookingList = new ArrayList<>();
        try {
            PreparedStatement statement;
            statement = dbConnection.prepareStatement(PreparedStatements.GET_USER_BOOKING);
            statement.setString(1, username);
            ResultSet result = statement.executeQuery();
            while (result.next()) {
                bookingList.add(
                        new Booking(
                                result.getInt("hotel_id"),
                                result.getDate("startDate").toLocalDate(),
                                result.getDate("endDate").toLocalDate(),
                                result.getInt("numRooms"),
                                result.getString("username"),
                                result.getTimestamp("timeBooked").toLocalDateTime()
                        )
                );
            }
        } catch (SQLException e) {
            System.out.println(e.getErrorCode());
            System.out.println(e.getMessage());
        }
        return bookingList;

    }

    public List<Booking> getAllBookings() {
        Connection dbConnection = dbHandler.getConnection();
        if (dbConnection == null) {
            System.out.println("Unable to connect to database.");
            return new ArrayList<>();
        }

        List<Booking> bookingList = new ArrayList<>();
        try {
            PreparedStatement statement;
            statement = dbConnection.prepareStatement(PreparedStatements.GET_ALL_BOOKING);
            ResultSet result = statement.executeQuery();
            while (result.next()) {
                bookingList.add(
                        new Booking(
                                result.getInt("hotel_id"),
                                result.getDate("startDate").toLocalDate(),
                                result.getDate("endDate").toLocalDate(),
                                result.getInt("numRooms"),
                                result.getString("username"),
                                result.getTimestamp("timeBooked").toLocalDateTime()
                        )
                );
            }
        } catch (SQLException e) {
            System.out.println(e.getErrorCode());
            System.out.println(e.getMessage());
        }
        return bookingList;
    }

    private String createBookingId(Booking booking) {
        String target = booking.username() + booking.hotelId() + booking.startDate() + booking.endDate() + LocalDate.now();
        String hashed = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(target.getBytes());
            hashed = encodeHex(md.digest());
        } catch (Exception ex) {
            System.out.println("Password hash failed");
        }
        assert hashed != null;
        return hashed.substring(0, 20);
    }

    private static String encodeHex(byte[] bytes) {
        BigInteger bigInt = new BigInteger(1, bytes);
        String hex = String.format("%0" + 64 + "X", bigInt);
        assert hex.length() == 64;
        return hex;
    }

    public static void main(String[] args) {
        BookingDatabaseHandler bookingHandler = new BookingDatabaseHandler();
        bookingHandler.dropBookingTable();
        bookingHandler.createBookingTable();
    }

}
