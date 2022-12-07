package hotelapp.servlets.Hotel;

import com.google.gson.JsonObject;
import hotelapp.Database.FavoritesDatabaseHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class DeleteFavoriteServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        HttpSession session = request.getSession();
        String username = ((JsonObject) session.getAttribute("loginInfo")).get("username").getAsString();
        FavoritesDatabaseHandler favoritesHandler = new FavoritesDatabaseHandler();
        favoritesHandler.removeAllFavorites(username);

        response.sendRedirect("/home");
    }
}
