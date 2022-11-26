package hotelapp.Database.LoadData;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import hotelapp.Database.DatabaseHandler;
import hotelapp.Database.PreparedStatements;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.Queue;

public class LoadReviews {
    public static void loadReviewsToDB(String directory) {
        try {
            Path p = Paths.get(directory);
            Queue<Path> queue = new ArrayDeque<>();
            queue.add(p);

            while (!queue.isEmpty()) {
                DirectoryStream<Path> dirContents = Files.newDirectoryStream(queue.remove());
                for (Path path : dirContents) {
                    if (!Files.isDirectory(path) && path.toString().endsWith(".json")) {
                        loadReviewAndRating(path.toString());
                    } else {
                        queue.add(path);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error: File not found");
        }
    }

    private static void loadReviewAndRating(String reviewJson) {
        Connection dbConnection = DatabaseHandler.getInstance().getConnection();
        if (dbConnection == null) {
            System.out.println("Unable to connect to database");
            return;
        }

        try (FileReader reader = new FileReader(reviewJson)) {
            JsonObject rawData = (JsonObject) JsonParser.parseReader(reader);
            JsonArray reviewArr = rawData
                    .getAsJsonObject("reviewDetails")
                    .getAsJsonObject("reviewCollection")
                    .getAsJsonArray("review");
            loadReview(dbConnection, reviewArr);

            JsonObject ratingObject = rawData
                    .getAsJsonObject("reviewDetails")
                    .getAsJsonObject("reviewSummaryCollection")
                    .getAsJsonArray("reviewSummary")
                    .get(0).getAsJsonObject();
            loadRating(dbConnection, ratingObject);

        } catch (IOException e) {
            System.out.println("Error: File not found");
        } catch (SQLException e) {
            System.out.println(e.getErrorCode());
            System.out.println(e.getMessage());
        }
    }

    private static void loadReview(Connection dbConnection, JsonArray reviewArr) throws IOException, SQLException {
        for (JsonElement reviewElem: reviewArr) {
            JsonObject currentUserReview = reviewElem.getAsJsonObject();
            String reviewId = currentUserReview.get("reviewId").getAsString();
            int hotelId = currentUserReview.get("hotelId").getAsInt();
            String title = currentUserReview.get("title") != null ? currentUserReview.get("title").getAsString() : "";
            String reviewText = currentUserReview.get("reviewText") != null ? currentUserReview.get("reviewText").getAsString() : "";
            String userNickname = currentUserReview.get("userNickname").getAsString().equals("") ? "Anonymous" : currentUserReview.get("userNickname").getAsString();
            LocalDateTime reviewDate = LocalDateTime.parse(currentUserReview.get("reviewSubmissionTime").getAsString().substring(0, 19));
            int ratingOverall = currentUserReview.get("ratingOverall").getAsInt();

            PreparedStatement statement = dbConnection.prepareStatement(PreparedStatements.ADD_REVIEW);
            statement.setString(1, reviewId);
            statement.setInt(2, hotelId);
            statement.setString(3, title);
            statement.setString(4, reviewText);
            statement.setString(5, userNickname);
            statement.setTimestamp(6, Timestamp.valueOf(reviewDate));
            statement.setInt(7, ratingOverall);
            statement.executeUpdate();
            statement.close();
        }
    }

    private static void loadRating(Connection dbConnection, JsonObject ratingObject) throws IOException, SQLException {

        int hotelId = ratingObject.get("hotelId").getAsInt();
        int numReviews = ratingObject.get("totalReviewCnt").getAsInt();
        double avgRating = ratingObject.get("avgOverallRating").getAsDouble();
        double cleanliness = ratingObject.get("cleanliness").getAsDouble();
        double service = ratingObject.get("serviceAndStaff").getAsDouble();
        double roomComfort = ratingObject.get("roomComfort").getAsDouble();
        double hotelCondition = ratingObject.get("hotelCondition").getAsDouble();
        double convenience = ratingObject.get("convenienceOfLocation").getAsDouble();
        double neighborhood = ratingObject.get("neighborhoodSatisfaction").getAsDouble();

        PreparedStatement statement = dbConnection.prepareStatement(PreparedStatements.ADD_RATING);
        statement.setInt(1, hotelId);
        statement.setInt(2, numReviews);
        statement.setDouble(3, avgRating);
        statement.setDouble(4, cleanliness);
        statement.setDouble(5, service);
        statement.setDouble(6, roomComfort);
        statement.setDouble(7, hotelCondition);
        statement.setDouble(8, convenience);
        statement.setDouble(9, neighborhood);
        statement.executeUpdate();
        statement.close();
    }

    public static void main(String[] args) {
        loadReviewsToDB("input/reviews");
    }
}
