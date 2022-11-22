package hotelapp.servlets.Hotel;

import com.google.gson.JsonObject;
import hotelapp.Database.HotelDatabaseHandler;
import hotelapp.Model.Hotel;
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

public class HotelServlet extends HttpServlet {

    private final HotelDatabaseHandler hotelHandler = new HotelDatabaseHandler();

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

        PrintWriter out = response.getWriter();
        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);

        VelocityEngine v = (VelocityEngine) request.getServletContext().getAttribute("templateEngine");
        VelocityContext context = new VelocityContext();
        context.put("name", hotel.name());
        context.put("addr", hotel.addr());
        context.put("city", hotel.city());
        context.put("state", hotel.state());
        Template template = v.getTemplate("templates/hotel.html");

        StringWriter writer = new StringWriter();
        template.merge(context, writer);

        out.write(writer.toString());

    }
}
