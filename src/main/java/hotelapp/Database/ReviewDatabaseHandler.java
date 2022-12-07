package hotelapp.Database;

import hotelapp.Database.LoadData.LoadReviews;
import hotelapp.Model.Rating;
import hotelapp.Model.Review;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ReviewDatabaseHandler {
    private final DatabaseHandler dbHandler = DatabaseHandler.getInstance();

    /**
     * Creates a review and a ratings table
     */
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

    /**
     * Drops the review and ratings table
     */
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

    /**
     * Get a list of reviews with the given hotel ID
     * @param hotelId is the target hotel ID
     * @return a list of Reviews
     */
    public List<Review> getProcessedReviewsByHotelId(int hotelId, String username) {
        Connection dbConnection = dbHandler.getConnection();
        if (dbConnection == null) {
            System.out.println("Unable to connect to database.");
            return new ArrayList<>();
        }

        List<Review> queryResult = new ArrayList<>();
        try {
            PreparedStatement statement;
            statement = dbConnection.prepareStatement(PreparedStatements.GET_REVIEW_BY_HOTEL_ID);
            statement.setInt(1, hotelId);

            ResultSet result = statement.executeQuery();
            while (result.next()) {
                queryResult.add(
                        new Review(
                                result.getString("review_id"),
                                hotelId,
                                result.getString("title"),
                                result.getString("reviewText"),
                                result.getString("userNickname"),
                                result.getTimestamp("reviewDate").toLocalDateTime(),
                                result.getInt("rating"),
                                result.getString("userNickname").equals(username)
                        )
                );
            }

        } catch (SQLException e) {
            System.out.println(e.getErrorCode());
            System.out.println(e.getMessage());
        }
        return queryResult;
    }

    /**
     * Find the ratings of a hotel
     * @param hotelId is the target hotel ID
     * @return an Optional of Rating, empty if the hotel ID is not found in the table
     */
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

    /**
     * Add a Review to the database
     * @param hotelId is the target hotel ID
     * @param title is the title of the Review
     * @param reviewText is the review Text
     * @param username is the username
     * @param reviewDate is the Date of the review
     * @param rating is the rating
     * @return true if successfully added to the database, otherwise false
     */
    public boolean addReview(int hotelId, String title, String reviewText,
                          String username, LocalDateTime reviewDate, int rating) {
        Connection dbConnection = dbHandler.getConnection();
        if (dbConnection == null) {
            System.out.println("Unable to connect to database.");
            return false;
        }

        String reviewId = getReviewId(username, hotelId, reviewDate);
        try {
            PreparedStatement statement = dbConnection.prepareStatement(PreparedStatements.ADD_REVIEW);
            statement.setString(1, reviewId);
            statement.setInt(2, hotelId);
            statement.setString(3, title);
            statement.setString(4, reviewText);
            statement.setString(5, username);
            statement.setTimestamp(6, Timestamp.valueOf(reviewDate));
            statement.setInt(7, rating);
            statement.executeUpdate();
            statement.close();

            return true;
        } catch (SQLException e) {
            System.out.println(e.getErrorCode());
            System.out.println(e.getMessage());
            return false;
        }
    }

    /**
     * Remove a review from the table given the review ID
     * @param reviewId is the target review ID to be removed
     * @return true if successfully removed, otherwise false
     */
    public boolean removeReviewById(String reviewId) {
        Connection dbConnection = dbHandler.getConnection();
        if (dbConnection == null) {
            System.out.println("Unable to connect to database.");
            return false;
        }

        try {
            PreparedStatement statement = dbConnection.prepareStatement(PreparedStatements.DELETE_REVIEW_BY_REVIEW_ID);
            statement.setString(1, reviewId);
            statement.executeUpdate();
            statement.close();
            return true;
        } catch (SQLException e) {
            System.out.println(e.getErrorCode());
            System.out.println(e.getMessage());
            return false;
        }

    }

    /**
     * Get a review given a review ID
     * @param reviewId is the target review ID
     * @return a Review if is found
     */
    public Review getReviewByReviewId(String reviewId) {
        Connection dbConnection = dbHandler.getConnection();
        if (dbConnection == null) {
            System.out.println("Unable to connect to database.");
            return null;
        }
        try {
            PreparedStatement statement = dbConnection.prepareStatement(PreparedStatements.GET_REVIEW_BY_REVIEW_ID);
            statement.setString(1, reviewId);

            ResultSet result = statement.executeQuery();

            if (result.next()) {
                return new Review(
                        result.getString(2),
                        result.getInt(3),
                        result.getString(4),
                        result.getString(5),
                        result.getString(6),
                        result.getTimestamp(7).toLocalDateTime(),
                        result.getInt(8),
                        false
                );
            }
        } catch (SQLException e) {
            System.out.println(e.getErrorCode());
            System.out.println(e.getMessage());
        }
        return null;
    }

    /**
     * Get the average rating of a hotel
     * @param hotelId is the target Hotel ID
     * @return the average rating
     */
    public double getAverageRatingByHotelId(int hotelId) {
        Connection dbConnection = dbHandler.getConnection();
        if (dbConnection == null) {
            System.out.println("Unable to connect to database.");
            return 0d;
        }
        try {
            PreparedStatement statement = dbConnection.prepareStatement(PreparedStatements.GET_AVERAGE_RATING_BY_HOTEL_ID);
            statement.setInt(1, hotelId);

            ResultSet result = statement.executeQuery();

            if (result.next()) {
                return result.getDouble(1);
            }
        } catch (SQLException e) {
            System.out.println(e.getErrorCode());
            System.out.println(e.getMessage());
        }
        return 0d;

    }

    public boolean createLikeReviewTable() {
        Connection dbConnection = dbHandler.getConnection();
        if (dbConnection == null) {
            System.out.println("Unable to connect to database.");
            return false;
        }
        try {
            PreparedStatement statement = dbConnection.prepareStatement(PreparedStatements.CREATE_LIKE_REVIEW_TABLE);
            statement.executeUpdate();
        } catch (SQLException e) {
            if (e.getErrorCode() == DatabaseErrorCodes.TABLE_EXISTS) {
                System.out.println("Like Review table already exists.");
            } else {
                System.out.println(e.getMessage());
                System.out.println("An error occurred.");
            }
            return false;
        }
        return true;
    }

    public boolean dropLikeReviewTable() {
        Connection dbConnection = dbHandler.getConnection();
        if (dbConnection == null) {
            System.out.println("Unable to connect to database.");
            return false;
        }

        try {
            PreparedStatement statement = dbConnection.prepareStatement(PreparedStatements.DROP_LIKE_REVIEW_TABLE);
            statement.executeUpdate();
        } catch (SQLException e) {
            if (e.getErrorCode() == DatabaseErrorCodes.TABLE_EXISTS) {
                System.out.println("Like Review table already exists.");
            } else {
                System.out.println(e.getMessage());
                System.out.println("An error occurred.");
            }
            return false;
        }
        return true;
    }

    public boolean checkUserLikesReview(String username, String reviewId) {
        Connection dbConnection = dbHandler.getConnection();
        if (dbConnection == null) {
            System.out.println("Unable to connect to database.");
            return false;
        }

        try {
            PreparedStatement statement = dbConnection.prepareStatement(PreparedStatements.CHECK_REVIEW_LIKED);
            statement.setString(1, username);
            statement.setString(2, reviewId);
            ResultSet result = statement.executeQuery();
            return result.next();
        } catch (SQLException e) {
            if (e.getErrorCode() == DatabaseErrorCodes.TABLE_DOES_NOT_EXIST) {
                System.out.println("Table favorites does not exist.");
            } else {
                System.out.println(e.getMessage());
                System.out.println("An error occurred.");
            }
            return false;
        }
    }

    public void flipLike(String username, String reviewId) {
        Connection dbConnection = dbHandler.getConnection();
        if (dbConnection == null) {
            System.out.println("Unable to connect to database.");
            return;
        }

        try {
            PreparedStatement statement;
            if (checkUserLikesReview(username, reviewId)) {
                statement = dbConnection.prepareStatement(PreparedStatements.DELETE_USER_LIKE);
            } else {
                statement = dbConnection.prepareStatement(PreparedStatements.ADD_USER_LIKE);
            }
            statement.setString(1, username);
            statement.setString(2, reviewId);
            statement.executeUpdate();
        } catch (SQLException e) {
            if (e.getErrorCode() == DatabaseErrorCodes.TABLE_DOES_NOT_EXIST) {
                System.out.println("Table favorites does not exist.");
            } else {
                System.out.println(e.getMessage());
                System.out.println("An error occurred.");
            }
        }
    }

    public int checkNumLikes(String reviewId) {
        Connection dbConnection = dbHandler.getConnection();
        if (dbConnection == null) {
            System.out.println("Unable to connect to database.");
            return 0;
        }

        try {
            PreparedStatement statement = dbConnection.prepareStatement(PreparedStatements.CHECK_NUM_LIKES);
            statement.setString(1, reviewId);
            ResultSet result = statement.executeQuery();
            if (result.next()) {
                return result.getInt(1);
            }

        } catch (SQLException e) {
            if (e.getErrorCode() == DatabaseErrorCodes.TABLE_DOES_NOT_EXIST) {
                System.out.println("Table favorites does not exist.");
            } else {
                System.out.println("checkNumLikes");
                System.out.println(e.getMessage());
                System.out.println("An error occurred.");
            }
        }

        return 0;
    }

    /**
     * Get a hashed review ID
     * @param username is the target username, used in hash
     * @param hotelId is the target hotel ID, used in hash
     * @param reviewDate is the time the review is added, used in hash
     * @return a hashed String representing a unique review ID
     */
    private String getReviewId(String username, int hotelId, LocalDateTime reviewDate) {
        String target = username + hotelId + reviewDate.toString();
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

    /**
     * Helper method for hashing
     */
    private static String encodeHex(byte[] bytes) {
        BigInteger bigInt = new BigInteger(1, bytes);
        String hex = String.format("%0" + 64 + "X", bigInt);
        assert hex.length() == 64;
        return hex;
    }

    /**
     * Test method
     */
    private void testReview() {
        try {
            PreparedStatement statement = dbHandler.getConnection().prepareStatement(PreparedStatements.ADD_REVIEW);
            statement.setString(1, "asdfasdfasdf");
            statement.setInt(2, 123);
            statement.setString(3, "Bad Experience");
            statement.setString(4, "This was our third use of this facility and again we enjoyed our stay.  The facility is comfortable and kept well.  We  are retired and the price and  'ease of use' are important for us.  Not only does the hotel meet our needs, there's attractive shopping and dinning within a few blocks.  The proximity to the Bay Bridge is appealing.  There are a number of historic and entertaining site near this location, i.e., Jack London Square (Oakland); Pier 39 (SF); and Rosie the Rivitor NP (Richmond).");
            statement.setString(5, "someone");
            statement.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
            statement.setInt(7, 4);
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            System.out.println(e.getErrorCode());
            System.out.println(e.getMessage());
        }
    }

    /**
     * Test method
     */
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
//        reviewHandler.dropReviewAndRatingTable();
//        reviewHandler.createReviewAndRatingTable();
//        LoadReviews.loadReviewsToDB("input/reviews");
        reviewHandler.dropLikeReviewTable();
        reviewHandler.createLikeReviewTable();
    }
}
