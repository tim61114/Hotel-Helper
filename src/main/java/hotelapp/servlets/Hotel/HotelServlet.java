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
import java.util.ArrayList;
import java.util.List;

public class HotelServlet extends HttpServlet {

    private static final int REVIEWS_PER_PAGE = 10;
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

        List<Review> reviews = paginationHandler(session);
        int size = reviewHandler.getNumReviewsForHotel(hotel.hotelId());
        List<String> pages = new ArrayList<>();
        for (int i = 1; i <= (size - 1) / REVIEWS_PER_PAGE + 1; i++) {
            pages.add(request.getRequestURL().append('?').append(request.getQueryString()).append("&page=").append(i).toString());
        }

        double averageRating = reviewHandler.getAverageRatingByHotelId(hotel.hotelId());

        PrintWriter out = response.getWriter();
        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);

        VelocityEngine v = (VelocityEngine) request.getServletContext().getAttribute("templateEngine");
        String currentPage = (String) session.getAttribute("reviewPage");
        session.setAttribute("reviewPage", "1");
        VelocityContext context = contextHandler(hotel, averageRating, rating, ratingError, reviewToBeEdited, bookingStatus, reviews, pages, currentPage);
        session.removeAttribute("BookingStatus");

        Template template = v.getTemplate("templates/hotel.html");

        StringWriter writer = new StringWriter();
        template.merge(context, writer);

        out.write(writer.toString());

    }

    /**
     * Handles the case if the user is not logged in or if user tried a bad hotelId from URL
     * @return true if redirection is needed
     * @throws IOException if sendRedirect fails
     */
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

        String page = request.getParameter("page");
        if (page != null) {
            session.setAttribute("reviewPage", page);
            response.sendRedirect(request.getRequestURL().append('?').append("hotelId=").append(hotelId).toString());
            return true;
        }

        return false;

    }

    /**
     * Gets the Review for the current page.
     * @param session is the session object storing data in the current session
     * @return the list of reviews in the current page
     */
    private List<Review> paginationHandler(HttpSession session) {
        String pageNumString = (String) session.getAttribute("reviewPage");
        String username = ((JsonObject) session.getAttribute("loginInfo")).get("username").getAsString();
        String hotelId = (String) session.getAttribute("currentHotel");
        int pageNum = 1;
        if (pageNumString != null) {
            pageNum = Integer.parseInt(pageNumString);
        }
        return reviewHandler.getReviewsByHotelIdWithOffset(
                Integer.parseInt(hotelId), username, REVIEWS_PER_PAGE, REVIEWS_PER_PAGE * (pageNum - 1));
    }

    /**
     * Helper method for handling all context to be put into page
     * @param hotel is the Hotel data
     * @param averageRating is the average rating of the hotel
     * @param rating is the Rating object (Currently not used)
     * @param ratingError is a flag for sending ratingError messages
     * @param review is the Review to be edited if needed
     * @param bookingStatus is the bookingStatus of the current session
     * @return the VelocityContext to be merged
     */
    private VelocityContext contextHandler(Hotel hotel, double averageRating, Rating rating, boolean ratingError,
                                           Review review, String bookingStatus, List<Review> reviews, List<String> pages, String pageNum) {
        VelocityContext context = new VelocityContext();
        context.put("rating", rating);
        context.put("hotel", hotel);
        context.put("avgRating", averageRating);
        context.put("Expedia", "<a href=\"/expedia?hotelId=" + hotel.hotelId() +"\" target=\"_blank\">Expedia</a>");
        context.put("reviews", reviews);
        context.put("editMode", review != null);
        context.put("pages", pages);
        context.put("currentPage", pageNum == null ? 1 : Integer.parseInt(pageNum));
        if (ratingError) {
            context.put("RatingError", "Rating should be a number between 0 to 5");
        }

        if (review != null) {
            context.put("comments", review.reviewText());
            context.put("title", "\"" + review.title() + "\"");
            context.put("prev_rating", review.rating());
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

    /**
     * Helper method to get the review data to be edited
     * @param session is the current session
     * @return the Review if an edit is requested
     */
    private Review getReviewToBeEdited(HttpSession session) {
        String editReviewId = (String) session.getAttribute("EditReview");
        Review review = null;
        if (editReviewId != null) {
            review = reviewHandler.getReviewByReviewId(editReviewId);
        }
        return review;
    }
}
