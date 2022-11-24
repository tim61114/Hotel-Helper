package hotelapp.servlets.Home;

import com.google.gson.JsonObject;
import hotelapp.Database.HotelDatabaseHandler;
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

public class HomeServlet extends HttpServlet {

    private static final int HOTEL_PER_PAGE = 20;
    private final HotelDatabaseHandler hotelHandler = new HotelDatabaseHandler();
    private final List<String> hotelData = hotelHandler.getProcessedHotels("");
    private final int TOTAL_HOTELS = hotelData.size();
    private final int PAGES = TOTAL_HOTELS / HOTEL_PER_PAGE;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (redirectHandler(request, response)) {
            return;
        }
        HttpSession session = request.getSession();

        List<String> hotelTable = paginationHandler(session);
        List<String> pages = new ArrayList<>();
        for (int i = 1; i <= PAGES + 1; i++) {
            pages.add("<a href=\"/home?page=" + i + "\">" + i + "</a>");
        }

        PrintWriter out = response.getWriter();
        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);

        VelocityEngine v = (VelocityEngine) request.getServletContext().getAttribute("templateEngine");
        Template template = v.getTemplate("templates/home.html");
        String username = ((JsonObject) session.getAttribute("loginInfo")).get("username").getAsString();

        VelocityContext context = contextHandler(username, hotelTable, pages);
        StringWriter writer = new StringWriter();
        template.merge(context, writer);

        out.write(writer.toString());
    }

    private boolean redirectHandler(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        JsonObject userJson = (JsonObject) session.getAttribute("loginInfo");
        if (userJson == null) {
            response.sendRedirect("/login");
            return true;
        }

        String page = request.getParameter("page");
        if (page != null) {
            session.setAttribute("page", page);
            response.sendRedirect("/home");
            return true;
        }
        return false;
    }

    private List<String> paginationHandler(HttpSession session) {
        String pageNumString = (String) session.getAttribute("page");
        int pageNum = 1;
        if (pageNumString != null) {
            pageNum = Integer.parseInt(pageNumString);
        }

        return hotelData.stream()
                .skip((long) (pageNum - 1) * HOTEL_PER_PAGE)
                .limit(HOTEL_PER_PAGE)
                .toList();
    }

    private VelocityContext contextHandler(String username, List<String> hotelTable, List<String> pages) {
        VelocityContext context = new VelocityContext();
        context.put("username", username);
        context.put("hotels", hotelTable);
        context.put("pages", pages);
        return context;
    }
}
