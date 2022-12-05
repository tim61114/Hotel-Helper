package hotelapp.servlets.Hotel;

import com.google.gson.JsonObject;
import hotelapp.Database.HotelDatabaseHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDateTime;

public class VisitExpediaServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        JsonObject userJson = (JsonObject) session.getAttribute("loginInfo");
        if (userJson == null) {
            response.sendRedirect("/login");
            return;
        }

        String hotelId = request.getParameter("hotelId");
        HotelDatabaseHandler hotelHandler = new HotelDatabaseHandler();
        String username = userJson.get("username").getAsString();
        hotelHandler.addUserExpediaHistory(username, Integer.parseInt(hotelId), LocalDateTime.now());
        response.sendRedirect("https://www.expedia.com/h" + hotelId + ".Hotel-Information");

    }
}
