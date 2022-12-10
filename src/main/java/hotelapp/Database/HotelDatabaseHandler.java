package hotelapp.Database;

import hotelapp.Database.LoadData.LoadHotels;
import hotelapp.Model.ExpediaHistory;
import hotelapp.Model.Hotel;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class HotelDatabaseHandler {
    private final DatabaseHandler dbHandler = DatabaseHandler.getInstance();

    /**
     * Creates a new Hotel Table
     * @return true if success, otherwise false
     */
    public boolean createHotelTable() {
        Connection dbConnection = dbHandler.getConnection();
        if (dbConnection == null) {
            System.out.println("Unable to connect to database.");
            return false;
        }
        try {
            PreparedStatement statement = dbConnection.prepareStatement(PreparedStatements.CREATE_HOTEL_TABLE);
            statement.executeUpdate();
        } catch (SQLException e) {
            if (e.getErrorCode() == DatabaseErrorCodes.TABLE_EXISTS) {
                System.out.println("User table already exists.");
            } else {
                System.out.println("An error occurred.");
            }
            return false;
        }
        return true;
    }

    /**
     * Drops the Hotel Table
     * @return true if success, otherwise false
     */
    public boolean dropHotelTable() {
        Connection dbConnection = dbHandler.getConnection();
        if (dbConnection == null) {
            System.out.println("Unable to connect to database.");
            return false;
        }
        try {
            PreparedStatement statement = dbConnection.prepareStatement(PreparedStatements.DROP_HOTEL_TABLE);
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
     * Get all hotel IDs from the hotel table
     * @return a list of hotel IDs, otherwise an empty list
     */
    public List<Integer> getHotelIdList() {
        Connection dbConnection = dbHandler.getConnection();
        if (dbConnection == null) {
            System.out.println("Unable to connect to database.");
            return new ArrayList<>();
        }

        List<Integer> idList = new ArrayList<>();
        try {
            PreparedStatement statement;
            statement = dbConnection.prepareStatement(PreparedStatements.GET_ALL_HOTEL_ID);
            ResultSet result = statement.executeQuery();
            while (result.next()) {
                idList.add(result.getInt(1));
            }
        } catch (SQLException e) {
            System.out.println(e.getErrorCode());
            System.out.println(e.getMessage());
        }
        return idList;
    }

    /**
     * Get a list of hotels given a search keyword
     * @param query is the keyword, if query is empty then all hotels are returned
     * @return a processed HTML form format string of all hotels containing the search keyword
     */
    public List<String> getProcessedHotels(String query) {
        Connection dbConnection = dbHandler.getConnection();
        if (dbConnection == null) {
            System.out.println("Unable to connect to database.");
            return new ArrayList<>();
        }

        List<String> queryResult = new ArrayList<>();
        try {
            PreparedStatement statement;
            if (query.equals("")) {
                statement = dbConnection.prepareStatement(PreparedStatements.GET_ALL_HOTELS);
            } else {
                statement = dbConnection.prepareStatement(
                        PreparedStatements.getHotelByKeyword(query.replaceAll("/+", " ")));
            }

            ResultSet result = statement.executeQuery();
            ResultSetMetaData metaData = result.getMetaData();
            while (result.next()) {
                StringBuilder sb = new StringBuilder();
                sb.append("<tr>");
                for (int i = 2; i <= metaData.getColumnCount(); i++) {
                    if (i >= 3 && i <= 5) {
                        continue;
                    }
                    sb.append("<td>");
                    if (i == 2) {
                        sb.append("<a href=\"hotel?hotelId=")
                                .append(result.getInt(3))
                                .append("\">")
                                .append(result.getString(i))
                                .append("</a>");
                    } else {
                        sb.append(result.getString(i));
                    }
                    sb.append("</td>");
                }
                sb.append("</tr>");
                queryResult.add(sb.toString());
            }

            return queryResult;
        } catch (SQLException e) {
            System.out.println(e.getErrorCode());
            System.out.println(e.getMessage());
        }
        return queryResult;
    }

    public List<Hotel> getHotels(String query) {
        Connection dbConnection = dbHandler.getConnection();
        if (dbConnection == null) {
            System.out.println("Unable to connect to database.");
            return new ArrayList<>();
        }

        List<Hotel> hotels = new ArrayList<>();
        try {
            PreparedStatement statement;
            if (query.equals("")) {
                statement = dbConnection.prepareStatement(PreparedStatements.GET_ALL_HOTELS);
            } else {
                statement = dbConnection.prepareStatement(
                        PreparedStatements.getHotelByKeyword(query.replaceAll("/+", " ")));
            }

            ResultSet result = statement.executeQuery();
            while (result.next()) {
                hotels.add(
                        new Hotel(
                               result.getString(2),
                               result.getInt(3),
                               result.getDouble(4),
                               result.getDouble(5),
                               result.getString(6),
                               result.getString(7),
                               result.getString(8),
                               result.getString(9)
                        )
                );
            }

            return hotels;
        } catch (SQLException e) {
            System.out.println(e.getErrorCode());
            System.out.println(e.getMessage());
        }
        return hotels;
    }

    /**
     * Get an Hotel object by hotel ID
     * @param hotelId is the target hotel ID
     * @return an optional of a Hotel, empty if the hotel is not found.
     */
    public Optional<Hotel> getHotelById(int hotelId) {
        Connection dbConnection = dbHandler.getConnection();
        if (dbConnection == null) {
            System.out.println("Unable to connect to database.");
            return Optional.empty();
        }

        try {
            PreparedStatement statement = dbConnection.prepareStatement(PreparedStatements.GET_HOTEL_BY_ID);
            statement.setInt(1, hotelId);

            ResultSet result = statement.executeQuery();

            if (result.next()) {
                return Optional.of(
                        new Hotel(result.getString(2),
                        result.getInt(3),
                        result.getDouble(4),
                        result.getDouble(5),
                        result.getString(6),
                        result.getString(7),
                        result.getString(8),
                        result.getString(9))
                );
            }
        } catch (SQLException e) {
            System.out.println(e.getErrorCode());
            System.out.println(e.getMessage());
        }

        return Optional.empty();
    }

    /**
     * Creates a new Expedia history Table
     * @return true if success, otherwise false
     */
    public boolean createExpediaHistoryTable() {
        Connection dbConnection = dbHandler.getConnection();
        if (dbConnection == null) {
            System.out.println("Unable to connect to database.");
            return false;
        }
        try {
            PreparedStatement statement = dbConnection.prepareStatement(PreparedStatements.CREATE_EXPEDIA_HISTORY_TABLE);
            statement.executeUpdate();
        } catch (SQLException e) {
            if (e.getErrorCode() == DatabaseErrorCodes.TABLE_EXISTS) {
                System.out.println("Expedia history table already exists.");
            } else {
                System.out.println("An error occurred.");
            }
            return false;
        }
        return true;
    }

    /**
     * Drops the Expedia history Table
     * @return true if success, otherwise false
     */
    public boolean dropExpediaHistoryTable() {
        Connection dbConnection = dbHandler.getConnection();
        if (dbConnection == null) {
            System.out.println("Unable to connect to database.");
            return false;
        }
        try {
            PreparedStatement statement = dbConnection.prepareStatement(PreparedStatements.DROP_EXPEDIA_HISTORY_TABLE);
            statement.executeUpdate();
        } catch (SQLException e) {
            if (e.getErrorCode() == DatabaseErrorCodes.TABLE_DOES_NOT_EXIST) {
                System.out.println("Table expedia history does not exist.");
            } else {
                System.out.println("An error occurred.");
            }
            return false;
        }
        return true;
    }

    /**
     * Get a list of Expedia history of given a user
     * @param username is the username to look for
     * @return a list of ExpediaHistory objects, consisting Hotel name and timestamp
     */
    public List<ExpediaHistory> getExpediaHistory(String username) {
        Connection dbConnection = dbHandler.getConnection();
        if (dbConnection == null) {
            System.out.println("Unable to connect to database.");
            return new ArrayList<>();
        }

        List<ExpediaHistory> history = new ArrayList<>();
        try {
            PreparedStatement statement = dbConnection.prepareStatement(PreparedStatements.GET_USER_EXPEDIA_HISTORY);
            statement.setString(1, username);

            ResultSet result = statement.executeQuery();
            while (result.next()) {
                history.add(new ExpediaHistory(
                        result.getString(1),
                        result.getInt(2),
                        result.getTimestamp(3).toLocalDateTime()
                ));
            }

            return history;
        } catch (SQLException e) {
            System.out.println(e.getErrorCode());
            System.out.println(e.getMessage());
        }
        return history;
    }

    /**
     * Delete the expedia history of the user
     * @param username is the username
     */
    public void deleteUserExpediaHistory(String username) {
        Connection dbConnection = dbHandler.getConnection();
        if (dbConnection == null) {
            System.out.println("Unable to connect to database.");
            return;
        }
        try {
            PreparedStatement statement = dbConnection.prepareStatement(PreparedStatements.DELETE_USER_EXPEDIA_HISTORY);
            statement.setString(1, username);
            statement.executeUpdate();

        } catch (SQLException e) {
            System.out.println(e.getErrorCode());
            System.out.println(e.getMessage());
        }
    }

    /**
     * Add a new row to the user's expedia history
     * @param username is the username
     * @param hotelId is the hotelId
     * @param time is the time the user clicked
     */
    public void addUserExpediaHistory(String username, int hotelId, LocalDateTime time) {
        Connection dbConnection = dbHandler.getConnection();
        if (dbConnection == null) {
            System.out.println("Unable to connect to database.");
            return;
        }
        try {
            PreparedStatement statement = dbConnection.prepareStatement(PreparedStatements.ADD_USER_EXPEDIA_HISTORY);
            statement.setString(1, username);
            statement.setInt(2, hotelId);
            statement.setTimestamp(3, Timestamp.valueOf(time));
            statement.setTimestamp(4, Timestamp.valueOf(time));
            statement.executeUpdate();

        } catch (SQLException e) {
            System.out.println(e.getErrorCode());
            System.out.println(e.getMessage());
        }
    }

    /**
     * Test method
     */
//    private void test() {
//        try {
//            PreparedStatement statement = dbHandler.getConnection().prepareStatement(PreparedStatements.ADD_HOTEL);
//            statement.setString(1, "test");
//            statement.setInt(2, 1);
//            statement.setDouble(3, 0d);
//            statement.setDouble(4, 0d);
//            statement.setString(5, "somewhere");
//            statement.setString(6, "SF");
//            statement.setString(7, "CA");
//            statement.setString(8, "USA");
//            statement.executeUpdate();
//            statement.close();
//        } catch (SQLException e) {
//            System.out.println(e.getErrorCode());
//            System.out.println(e.getMessage());
//        }
//
//    }

    public static void main(String[] args) {
        HotelDatabaseHandler hotelHandler = new HotelDatabaseHandler();
//        hotelHandler.dropHotelTable();
//        hotelHandler.createHotelTable();
//        LoadHotels.LoadHotelsToDB("input/hotels/hotels.json");
        hotelHandler.dropExpediaHistoryTable();
        hotelHandler.createExpediaHistoryTable();
    }
}
