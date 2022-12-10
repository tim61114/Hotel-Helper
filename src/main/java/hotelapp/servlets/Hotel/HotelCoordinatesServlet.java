package hotelapp.servlets.Hotel;

import com.google.gson.JsonObject;
import hotelapp.Database.HotelDatabaseHandler;
import hotelapp.Model.Hotel;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;

public class HotelCoordinatesServlet extends HttpServlet {
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

        HotelDatabaseHandler hotelHandler = new HotelDatabaseHandler();
        Hotel hotel = hotelHandler.getHotelById(Integer.parseInt(currentHotel)).orElseThrow();

        JsonObject coordinates = new JsonObject();
        coordinates.addProperty("lat", hotel.lat());
        coordinates.addProperty("lng", hotel.lng());

        PrintWriter out = response.getWriter();
        out.println(coordinates);

    }
}
