package hotelapp.Database;

import hotelapp.Database.DataPrep.LoadReviews;
import hotelapp.Model.Hotel;
import hotelapp.Model.Rating;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ReviewDatabaseHandler {
    private final DatabaseHandler dbHandler = DatabaseHandler.getInstance();

    public void createReviewAndRatingTable() {
        Connection dbConnection = dbHandler.getConnection();
        if (dbConnection == null) {
            System.out.println("Unable to connect to database.");
            return;
        }
        try {
            PreparedStatement statement = dbConnection.prepareStatement(PreparedStatements.CREATE_REVIEW_TABLE);
            statement.executeUpdate();
            statement = dbConnection.prepareStatement(PreparedStatements.CREATE_RATING_TABLE);
            statement.executeUpdate();
        } catch (SQLException e) {
            if (e.getErrorCode() == DatabaseErrorCodes.TABLE_EXISTS) {
                System.out.println("Review table already exists.");
            } else {
                System.out.println(e.getMessage());
                System.out.println("An error occurred.");
            }
        }
    }

    public void dropReviewAndRatingTable() {
        Connection dbConnection = dbHandler.getConnection();
        if (dbConnection == null) {
            System.out.println("Unable to connect to database.");
            return;
        }
        try {
            PreparedStatement statement = dbConnection.prepareStatement(PreparedStatements.DROP_REVIEW_TABLE);
            statement.executeUpdate();
            statement = dbConnection.prepareStatement(PreparedStatements.DROP_RATING_TABLE);
            statement.executeUpdate();
        } catch (SQLException e) {
            if (e.getErrorCode() == DatabaseErrorCodes.TABLE_EXISTS) {
                System.out.println("Table does not exist.");
            } else {
                System.out.println(e.getMessage());
                System.out.println("An error occurred.");
            }
        }
    }

    public List<String> getProcessedReviewsByHotelId(int hotelId) {
        Connection dbConnection = dbHandler.getConnection();
        if (dbConnection == null) {
            System.out.println("Unable to connect to database.");
            return new ArrayList<>();
        }

        List<String> queryResult = new ArrayList<>();
        try {
            PreparedStatement statement;
            statement = dbConnection.prepareStatement(PreparedStatements.GET_REVIEW_BY_HOTEL_ID);
            statement.setInt(1, hotelId);

            ResultSet result = statement.executeQuery();
            while (result.next()) {
                String str =
                        "<tr>" +
                        "<td>Rating: " + result.getInt("rating") + "/5</td>" +
                        "<td>" + result.getString("title") + "</td>" +
                        "<td>" + result.getDate("reviewDate") + "</td>" +
                        "<td>" + result.getString("userNickname") + "</td>" +
                        "</tr>" +
                        "<tr>" +
                        "<td colspan=\"3\">" + result.getString("reviewText") + "</td>" +
                        "</tr>";

                queryResult.add(str);
            }

        } catch (SQLException e) {
            System.out.println(e.getErrorCode());
            System.out.println(e.getMessage());
        }
        return queryResult;
    }

    public Optional<Rating> getRatingByHotelId(int hotelId) {
        Connection dbConnection = dbHandler.getConnection();
        if (dbConnection == null) {
            System.out.println("Unable to connect to database.");
            return Optional.empty();
        }
        try {
            PreparedStatement statement = dbConnection.prepareStatement(PreparedStatements.GET_RATING_BY_HOTEL_ID);
            statement.setInt(1, hotelId);
            ResultSet result = statement.executeQuery();

            if (result.next()) {
                return Optional.of(
                        new Rating(result.getInt(2),
                                result.getDouble(3),
                                result.getDouble(4),
                                result.getDouble(5),
                                result.getDouble(6),
                                result.getDouble(7),
                                result.getDouble(8),
                                result.getDouble(9))
                );
            }
        } catch (SQLException e) {
            System.out.println(e.getErrorCode());
            System.out.println(e.getMessage());
        }

        return Optional.empty();

    }

    private void testReview() {
        try {
            PreparedStatement statement = dbHandler.getConnection().prepareStatement(PreparedStatements.ADD_REVIEW);
            statement.setString(1, "asdfasdfasdf");
            statement.setInt(2, 123);
            statement.setString(3, "Bad Experience");
            statement.setString(4, "This was our third use of this facility and again we enjoyed our stay.  The facility is comfortable and kept well.  We  are retired and the price and  'ease of use' are important for us.  Not only does the hotel meet our needs, there's attractive shopping and dinning within a few blocks.  The proximity to the Bay Bridge is appealing.  There are a number of historic and entertaining site near this location, i.e., Jack London Square (Oakland); Pier 39 (SF); and Rosie the Rivitor NP (Richmond).");
            statement.setString(5, "someone");
            statement.setDate(6, Date.valueOf(LocalDate.now()));
            statement.setInt(7, 4);
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            System.out.println(e.getErrorCode());
            System.out.println(e.getMessage());
        }
    }

    private void testRating() {
        try {
            PreparedStatement statement = dbHandler.getConnection().prepareStatement(PreparedStatements.ADD_RATING);
            statement.setInt(1, 123);
            statement.setInt(2, 123);
            statement.setDouble(3, 1.0);
            statement.setDouble(4, 1.0);
            statement.setDouble(5, 1.0);
            statement.setDouble(6, 1.0);
            statement.setDouble(7, 1.0);
            statement.setDouble(8, 1.0);
            statement.setDouble(9, 1.0);
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            System.out.println(e.getErrorCode());
            System.out.println(e.getMessage());
        }
    }

    public static void main(String[] args) {
        ReviewDatabaseHandler reviewHandler = new ReviewDatabaseHandler();
        reviewHandler.dropReviewAndRatingTable();
        reviewHandler.createReviewAndRatingTable();
        LoadReviews.loadReviewsToDB("input/reviews");
    }
}
