package hotelapp.Database;

import hotelapp.Database.DataPrep.LoadHotels;
import hotelapp.servlets.Home.HomeServlet;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HotelDatabaseHandler {
    private final DatabaseHandler dbHandler = DatabaseHandler.getInstance();

    public boolean createHotelTable() {
        Connection dbConnection = dbHandler.getConnection();
        if (dbConnection == null) {
            System.out.println("Unable to connect to database.");
            return false;
        }
        try {
            Statement statement = dbConnection.createStatement();
            statement.executeUpdate(PreparedStatements.CREATE_HOTEL_TABLE);
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

    public boolean dropHotelTable() {
        Connection dbConnection = dbHandler.getConnection();
        if (dbConnection == null) {
            System.out.println("Unable to connect to database.");
            return false;
        }
        try {
            Statement statement = dbConnection.createStatement();
            statement.executeUpdate(PreparedStatements.DROP_HOTEL_TABLE);
        } catch (SQLException e) {
            if (e.getErrorCode() == DatabaseErrorCodes.TABLE_DOES_NOT_EXIST) {
                System.out.println("Table hotels does not exist. Aborted.");
            } else {
                System.out.println("An error occurred.");
            }
            return false;
        }
        return true;
    }

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
                statement = dbConnection.prepareStatement(PreparedStatements.GET_HOTEL_BY_KEYWORD);
                statement.setString(1, query);
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
                    String columnValue = result.getString(i);
                    sb.append(columnValue);
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
        hotelHandler.getProcessedHotels("");
    }
}
