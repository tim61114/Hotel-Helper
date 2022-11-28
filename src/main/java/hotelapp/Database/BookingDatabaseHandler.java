package hotelapp.Database;

import hotelapp.Model.Booking;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookingDatabaseHandler {
    private final DatabaseHandler dbHandler = DatabaseHandler.getInstance();

    /**
     * Creates a booking table in the database
     * @return true if success, false if failed
     */
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

    /**
     * Drop the booking table
     * @return true if success, false if failed
     */
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

    /**
     * Add a booking to the bookings table
     * @param booking is the booking to be added
     */
    public void addBooking(Booking booking) {
        Connection dbConnection = dbHandler.getConnection();
        if (dbConnection == null) {
            System.out.println("Unable to connect to database.");
            return;
        }

        try {
            PreparedStatement statement = dbConnection.prepareStatement(PreparedStatements.ADD_BOOKING);
            statement.setString(1, booking.bookingId());
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

    /**
     * Get all bookings of a user
     * @param username is the target username
     * @return a list of Booking
     */
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
                                result.getString("booking_id"),
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

    /**
     * Get all existing booking in the table
     * @return a List of booking
     */
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
                                result.getString("booking_id"),
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

    /**
     * Get a booking by booking ID
     * @param bookingId is the target booking ID
     * @return a Booking if found, otherwise null
     */
    public Booking getBookingByBookingId(String bookingId) {
        Connection dbConnection = dbHandler.getConnection();
        if (dbConnection == null) {
            System.out.println("Unable to connect to database.");
            return null;
        }
        try {
            PreparedStatement statement;
            statement = dbConnection.prepareStatement(PreparedStatements.GET_BOOKING_BY_BOOKING_ID);
            statement.setString(1, bookingId);
            ResultSet result = statement.executeQuery();
            if (result.next()) {
                return new Booking(
                        result.getString("booking_id"),
                        result.getInt("hotel_id"),
                        result.getDate("startDate").toLocalDate(),
                        result.getDate("endDate").toLocalDate(),
                        result.getInt("numRooms"),
                        result.getString("username"),
                        result.getTimestamp("timeBooked").toLocalDateTime()
                );
            }
        } catch (SQLException e) {
            System.out.println(e.getErrorCode());
            System.out.println(e.getMessage());
        }
        return null;
    }

    /**
     * Delete a booking by the booking ID
     * @param bookingId is the target booking ID to remove
     * @return true if delete is successful, otherwise false
     */
    public boolean deleteBooking(String bookingId) {
        Connection dbConnection = dbHandler.getConnection();
        if (dbConnection == null) {
            System.out.println("Unable to connect to database.");
            return false;
        }

        try {
            PreparedStatement statement;
            statement = dbConnection.prepareStatement(PreparedStatements.DELETE_BOOKING);
            statement.setString(1, bookingId);
            statement.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println(e.getErrorCode());
            System.out.println(e.getMessage());
        }
        return false;

    }



    public static void main(String[] args) {
        BookingDatabaseHandler bookingHandler = new BookingDatabaseHandler();
        bookingHandler.dropBookingTable();
        bookingHandler.createBookingTable();
    }

}
