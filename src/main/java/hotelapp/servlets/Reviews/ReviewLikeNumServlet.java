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

public class ReviewLikeNumServlet extends HttpServlet {
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

        PrintWriter out = response.getWriter();

        JsonObject numLikes = new JsonObject();
        numLikes.addProperty("numLikes",reviewHandler.checkNumLikes(reviewId));
        out.println(numLikes);
    }
}
