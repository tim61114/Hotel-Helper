package hotelapp.servlets.Hotel;

import com.google.gson.JsonObject;
import hotelapp.Database.HotelDatabaseHandler;
import hotelapp.Database.ReviewDatabaseHandler;
import hotelapp.Model.Hotel;
import hotelapp.Model.Rating;
import hotelapp.Model.Review;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

public class HotelServlet extends HttpServlet {

    private final HotelDatabaseHandler hotelHandler = new HotelDatabaseHandler();
    private final ReviewDatabaseHandler reviewHandler = new ReviewDatabaseHandler();
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        if (redirectHandler(request, response)) {
            return;
        }

        String hotelId = request.getParameter("hotelId");

        Hotel hotel;
        try {
            hotel = hotelHandler.getHotelById(Integer.parseInt(hotelId))
                    .orElseThrow(IllegalArgumentException::new);
        } catch (IllegalArgumentException e) {
            response.sendRedirect("/home");
            return;
        }

        String bookingStatus = (String) session.getAttribute("BookingStatus");

        session.setAttribute("currentHotel", hotelId);

        boolean ratingError = session.getAttribute("ratingError") != null;
        session.removeAttribute("ratingError");

        Review reviewToBeEdited = getReviewToBeEdited(session);

        Rating rating = reviewHandler.getRatingByHotelId(hotel.hotelId())
                .orElse(new Rating(0, 0,0,0,0,0,0,0));

        String username = ((JsonObject) session.getAttribute("loginInfo")).get("username").getAsString();
        List<Review> reviews = reviewHandler.getProcessedReviewsByHotelId(hotel.hotelId());
        List<String> parsedReviews = processReviews(reviews, username, reviewToBeEdited != null);

        double averageRating = reviewHandler.getAverageRatingByHotelId(hotel.hotelId());

        PrintWriter out = response.getWriter();
        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);

        VelocityEngine v = (VelocityEngine) request.getServletContext().getAttribute("templateEngine");
        VelocityContext context = contextHandler(hotel, averageRating, rating, parsedReviews, ratingError, reviewToBeEdited, bookingStatus);

        Template template = v.getTemplate("templates/hotel.html");

        StringWriter writer = new StringWriter();
        template.merge(context, writer);

        out.write(writer.toString());

    }

    private boolean redirectHandler(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        JsonObject userJson = (JsonObject) session.getAttribute("loginInfo");
        if (userJson == null) {
            response.sendRedirect("/login");
            return true;
        }

        String hotelId = request.getParameter("hotelId");
        if (hotelId == null) {
            response.sendRedirect("/home");
            return true;
        }

        return false;
    }

    private VelocityContext contextHandler(Hotel hotel, double averageRating, Rating rating, List<String> reviews,
                                           boolean ratingError, Review review, String bookingStatus) {
        VelocityContext context = new VelocityContext();
        context.put("rating", rating);
        context.put("hotel", hotel);
        context.put("reviews", reviews);
        context.put("avgRating", averageRating);
        context.put("Expedia", "<a href=\"https://www.expedia.com/h" + hotel.hotelId() + ".Hotel-Information\" target=\"_blank\">Expedia</a>");
        if (ratingError) {
            context.put("RatingError", "Rating should be a number between 0 to 5");
        }

        if (review != null) {
            context.put("comments", review.reviewText());
        } else {
            context.put("comments", "Your comments here");
        }

        if (bookingStatus != null) {
            switch (Integer.parseInt(bookingStatus)) {
                case 0 -> context.put("BookingSuccess", "Booking Success!");
                case 1 -> context.put("BookingError", "Rooms are not fully available in the time being.");
                case 2 -> context.put("BookingError", "Start date should be not later than end date.");
                case 3 -> context.put("BookingError", "Too many rooms. (We only have at most three rooms available)");
            }
        }

        return context;
    }

    private List<String> processReviews(List<Review> reviews, String currentUser, boolean edit) {
        return reviews.stream()
                .map(
                review -> {
                    String res;
                    res = "<tr>" +
                            "<td>" + review.title() + "</td>" +
                            "<td> Time submitted: " + review.reviewDate().toString().replaceAll("T", " ") + "</td>" +
                            "<td>Rating: " + review.rating() + "/5</td>" +
                            "<td>" + review.username() + "</td>" +
                            "</tr>" +
                            "<tr>" +
                            "<td colspan=\"2\">" + review.reviewText() + "</td>";
                    if (!edit && review.username().equals(currentUser)) {
                        res +=  "<td><form method=\"post\" action=\"/edit?reviewId=" + review.reviewId() + "\">" +
                                "<input type=\"submit\" value=\"Edit\">" +
                                "</form></td>" +
                                "<td><form method=\"post\" action=\"/delete?reviewId="+ review.reviewId() + "\">" +
                                "<input type=\"submit\" value=\"Delete\">";
                    }
                    res += "</tr>";
                    return res;
                }
        )
                .toList();
    }

    private Review getReviewToBeEdited(HttpSession session) {
        String editReviewId = (String) session.getAttribute("EditReview");
        Review review = null;
        if (editReviewId != null) {
            review = reviewHandler.getReviewByReviewId(editReviewId);
        }
        return review;
    }
}
