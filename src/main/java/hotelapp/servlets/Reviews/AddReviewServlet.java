package hotelapp.servlets.Reviews;

import com.google.gson.JsonObject;
import hotelapp.Database.ReviewDatabaseHandler;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDateTime;

public class AddReviewServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html");
        HttpSession session = request.getSession();

        String title = request.getParameter("title");
        int rating;
        String reviewText = request.getParameter("comment");
        LocalDateTime reviewDate = LocalDateTime.now();

        JsonObject userJson = (JsonObject) session.getAttribute("loginInfo");
        String username = userJson.get("username").getAsString();
        int hotelId = Integer.parseInt((String) session.getAttribute("currentHotel"));
        try {
            rating = Integer.parseInt(request.getParameter("rating"));
            if (rating > 5) {
                throw new IllegalArgumentException();
            }
        } catch (IllegalArgumentException e){
            session.setAttribute("ratingError", true);
            response.sendRedirect("/hotel?hotelId=" + hotelId);
            return;
        }

        ReviewDatabaseHandler reviewDB = new ReviewDatabaseHandler();
        String reviewId = (String) session.getAttribute("EditReview");
        if (reviewId != null) {
            reviewDB.removeReviewById(reviewId);
            session.removeAttribute("EditReview");
        }
        reviewDB.addReview(hotelId, title, reviewText, username, reviewDate, rating);
        response.sendRedirect("/hotel?hotelId=" + hotelId);

    }
}
