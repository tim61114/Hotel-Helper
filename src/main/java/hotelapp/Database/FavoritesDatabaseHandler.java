package hotelapp.Database;

import hotelapp.Model.Hotel;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FavoritesDatabaseHandler {
    private final DatabaseHandler dbHandler = DatabaseHandler.getInstance();

    /**
     * Creates a new Favorites Table
     *
     * @return true if success, otherwise false
     */
    public boolean createFavoritesTable() {
        Connection dbConnection = dbHandler.getConnection();
        if (dbConnection == null) {
            System.out.println("Unable to connect to database.");
            return false;
        }
        try {
            PreparedStatement statement = dbConnection.prepareStatement(PreparedStatements.CREATE_FAVORITES_TABLE);
            statement.executeUpdate();
        } catch (SQLException e) {
            if (e.getErrorCode() == DatabaseErrorCodes.TABLE_EXISTS) {
                System.out.println("Favorites table already exists.");
            } else {
                System.out.println("An error occurred.");
            }
            return false;
        }
        return true;
    }

    /**
     * Drops the Favorites Table
     *
     * @return true if success, otherwise false
     */
    public boolean dropFavoritesTable() {
        Connection dbConnection = dbHandler.getConnection();
        if (dbConnection == null) {
            System.out.println("Unable to connect to database.");
            return false;
        }
        try {
            PreparedStatement statement = dbConnection.prepareStatement(PreparedStatements.DROP_FAVORITES_TABLE);
            statement.executeUpdate();
        } catch (SQLException e) {
            if (e.getErrorCode() == DatabaseErrorCodes.TABLE_DOES_NOT_EXIST) {
                System.out.println("Table favorites does not exist.");
            } else {
                System.out.println("An error occurred.");
            }
            return false;
        }
        return true;
    }

    /**
     * Remove the current hotel from favorites if exists, add to the table if it does not.
     * @param username is the username
     * @param hotelId is the ID of the hotel
     */
    public void flipHotel(String username, int hotelId) {
        Connection dbConnection = dbHandler.getConnection();
        if (dbConnection == null) {
            System.out.println("Unable to connect to database.");
            return;
        }

        try {
            PreparedStatement statement;
            if (isFavorite(username, hotelId)) {
                statement = dbConnection.prepareStatement(PreparedStatements.DELETE_FAVORITE);
            } else {
                statement = dbConnection.prepareStatement(PreparedStatements.ADD_FAVORITE);
            }
            statement.setInt(1, hotelId);
            statement.setString(2, username);
            statement.executeUpdate();
        } catch (SQLException e) {
            if (e.getErrorCode() == DatabaseErrorCodes.TABLE_DOES_NOT_EXIST) {
                System.out.println("Table favorites does not exist.");
            } else {
                System.out.println("An error occurred.");
            }
        }
    }

    /**
     * Check if a hotel is favorite-d by the user
     * @param username is the username
     * @param hotelId is the ID of the hotel
     * @return true for yes.
     */
    public boolean isFavorite(String username, int hotelId) {
        Connection dbConnection = dbHandler.getConnection();
        if (dbConnection == null) {
            System.out.println("Unable to connect to database.");
            return false;
        }
        try {
            PreparedStatement statement = dbConnection.prepareStatement(PreparedStatements.CHECK_IS_FAVORITE_HOTEL);
            statement.setInt(1, hotelId);
            statement.setString(2, username);
            ResultSet result = statement.executeQuery();
            return result.next();
        } catch (SQLException e) {
            if (e.getErrorCode() == DatabaseErrorCodes.TABLE_DOES_NOT_EXIST) {
                System.out.println("Table favorites does not exist.");
            } else {
                System.out.println("An error occurred.");
            }
            return false;
        }
    }

    /**
     * Get all hotels the user favorite-d
     * @param username is the username
     * @return a list of hotels that is favorite-d by the user
     */
    public List<Hotel> getFavoriteHotels(String username) {
        Connection dbConnection = dbHandler.getConnection();
        if (dbConnection == null) {
            System.out.println("Unable to connect to database.");
            return new ArrayList<>();
        }

        List<Hotel> hotels = new ArrayList<>();
        try {
            PreparedStatement statement = dbConnection.prepareStatement(PreparedStatements.GET_USER_FAVORITES);
            statement.setString(1, username);

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
            if (e.getErrorCode() == DatabaseErrorCodes.TABLE_DOES_NOT_EXIST) {
                System.out.println("Table favorites does not exist.");
            } else {
                System.out.println("An error occurred.");
            }
            return new ArrayList<>();
        }

    }

    /**
     * Delete all hotels that are favorite-d by the user
     * @param username is the username to remove
     */
    public void removeAllFavorites(String username) {
        Connection dbConnection = dbHandler.getConnection();
        if (dbConnection == null) {
            System.out.println("Unable to connect to database.");
            return;
        }

        try {
            PreparedStatement statement = dbConnection.prepareStatement(PreparedStatements.DELETE_USER_FAVORITE);
            statement.setString(1, username);
            statement.executeUpdate();
        } catch (SQLException e) {
            if (e.getErrorCode() == DatabaseErrorCodes.TABLE_DOES_NOT_EXIST) {
                System.out.println("Table favorites does not exist.");
            } else {
                System.out.println("An error occurred.");
            }
        }
    }

    public static void main(String[] args) {
        FavoritesDatabaseHandler favoritesHandler = new FavoritesDatabaseHandler();
        favoritesHandler.dropFavoritesTable();
        favoritesHandler.createFavoritesTable();

    }

}
