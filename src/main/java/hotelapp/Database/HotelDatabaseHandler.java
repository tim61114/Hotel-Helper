package hotelapp.Database;

import hotelapp.Database.LoadData.LoadHotels;
import hotelapp.Model.Hotel;

import java.sql.*;
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
        hotelHandler.dropHotelTable();
        hotelHandler.createHotelTable();
        LoadHotels.LoadHotelsToDB("input/hotels/hotels.json");
    }
}
