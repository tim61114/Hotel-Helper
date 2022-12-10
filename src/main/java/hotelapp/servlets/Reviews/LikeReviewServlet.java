package hotelapp.servlets.Reviews;

import com.google.gson.JsonObject;
import hotelapp.Database.ReviewDatabaseHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;

public class LikeReviewServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        JsonObject userJson = (JsonObject) session.getAttribute("loginInfo");
        if (userJson == null) {
            response.sendRedirect("/login");
            return;
        }

        String reviewId = request.getParameter("reviewId");
        ReviewDatabaseHandler reviewHandler = new ReviewDatabaseHandler();
        String username = userJson.get("username").getAsString();

        PrintWriter out = response.getWriter();

        JsonObject userLikes = new JsonObject();
        userLikes.addProperty("userLikes",reviewHandler.checkUserLikesReview(username, reviewId));
        out.println(userLikes);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        String reviewId = request.getParameter("reviewId");
        String username = ((JsonObject) session.getAttribute("loginInfo")).get("username").getAsString();

        ReviewDatabaseHandler reviewHandler = new ReviewDatabaseHandler();
        reviewHandler.flipLike(username, reviewId);
    }
}
