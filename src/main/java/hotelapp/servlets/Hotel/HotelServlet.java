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
import java.util.stream.Collectors;

public class HotelServlet extends HttpServlet {

    private final HotelDatabaseHandler hotelHandler = new HotelDatabaseHandler();
    private final ReviewDatabaseHandler reviewHandler = new ReviewDatabaseHandler();
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        JsonObject userJson = (JsonObject) session.getAttribute("loginInfo");
        if (userJson == null) {
            response.sendRedirect("/login");
            return;
        }

        String hotelId = request.getParameter("hotelId");
        if (hotelId == null) {
            response.sendRedirect("/home");
            return;
        }
        Hotel hotel;
        try {
            hotel = hotelHandler.getHotelById(Integer.parseInt(hotelId))
                    .orElseThrow(IllegalArgumentException::new);
        } catch (IllegalArgumentException e) {
            response.sendRedirect("/home");
            return;
        }

        session.setAttribute("currentHotel", hotelId);
        boolean commentError = session.getAttribute("ratingError") != null;
        session.removeAttribute("ratingError");

        String editReviewId = (String) session.getAttribute("EditReview");
        Review review = null;
        if (editReviewId != null) {
            review = reviewHandler.getReviewByReviewId(editReviewId);
        }

        Rating rating = reviewHandler.getRatingByHotelId(hotel.hotelId())
                .orElse(new Rating(0, 0,0,0,0,0,0,0));

        List<Review> reviews = reviewHandler.getProcessedReviewsByHotelId(hotel.hotelId());
        List<String> parsedReviews = processReviews(reviews, userJson.get("username").getAsString(), editReviewId != null);

        PrintWriter out = response.getWriter();
        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);

        VelocityEngine v = (VelocityEngine) request.getServletContext().getAttribute("templateEngine");
        VelocityContext context = fillContext(hotel, rating, parsedReviews, commentError, review);

        Template template = v.getTemplate("templates/hotel.html");

        StringWriter writer = new StringWriter();
        template.merge(context, writer);

        out.write(writer.toString());

    }

    private VelocityContext fillContext(Hotel hotel, Rating rating, List<String> reviews, boolean ratingError, Review review) {
        VelocityContext context = new VelocityContext();
        context.put("numReviews", rating.numReviews());
        context.put("avgRating", rating.avgRating());
        context.put("cleanliness", rating.cleanliness());
        context.put("service", rating.service());
        context.put("roomComfort", rating.roomComfort());
        context.put("hotelCondition", rating.hotelCondition());
        context.put("convenience", rating.convenience());
        context.put("neighborhood", rating.neighborhood());
        context.put("name", hotel.name());
        context.put("addr", hotel.addr());
        context.put("city", hotel.city());
        context.put("state", hotel.state());
        context.put("reviews", reviews);
        if (ratingError) {
            context.put("RatingError", "Rating should be a number between 0 to 5");
        }

        if (review != null) {
            context.put("comments", review.reviewText());
        } else {
            context.put("comments", "Your comments here");
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
}
