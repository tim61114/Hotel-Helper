package hotelapp.servlets.Reviews;

import hotelapp.Database.ReviewDatabaseHandler;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class DeleteReviewServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html");
        HttpSession session = request.getSession();
        String reviewId = request.getParameter("reviewId");

        ReviewDatabaseHandler reviewDB = new ReviewDatabaseHandler();
        reviewDB.removeReviewById(reviewId);

        int hotelId = Integer.parseInt((String) session.getAttribute("currentHotel"));
        response.sendRedirect("/hotel?hotelId=" + hotelId);
    }
}
