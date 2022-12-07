package hotelapp.servlets.Hotel;

import com.google.gson.JsonObject;
import hotelapp.Database.FavoritesDatabaseHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;

public class CheckIsFavoriteServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        JsonObject userJson = (JsonObject) session.getAttribute("loginInfo");
        if (userJson == null) {
            response.sendRedirect("/login");
            return;
        }
        String currentHotel = (String) session.getAttribute("currentHotel");
        if (currentHotel == null) {
            response.sendRedirect("/home");
            return;
        }

        FavoritesDatabaseHandler favoritesHandler = new FavoritesDatabaseHandler();
        String username = userJson.get("username").getAsString();
        int hotelId = Integer.parseInt(currentHotel);

//        System.out.println(currentHotel);
//        System.out.println(favoritesHandler.isFavorite(username, hotelId));

        JsonObject result = new JsonObject();
        result.addProperty("isFavorite", favoritesHandler.isFavorite(username, hotelId));
        PrintWriter out = response.getWriter();
        out.println(result);
    }
}
