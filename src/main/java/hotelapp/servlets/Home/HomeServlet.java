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
        JsonObject loginInfo = (JsonObject) session.getAttribute("loginInfo");

        VelocityContext context = contextHandler(loginInfo, hotelTable, pages);
        StringWriter writer = new StringWriter();
        template.merge(context, writer);

        out.write(writer.toString());
    }

    /**
     * redirect the page to designated state, handles not logged in cases and pagination in home.
     * @return true if redirection is needed
     * @throws IOException if unable to redirect
     */
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

    /**
     * Sets the current page to be shown in a list
     * @param session to read the current page info
     * @return a formatted String HTML form
     */
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

    /**
     * Helper method to create a VelocityContext object
     * @param loginInfo is the current login info of the user
     * @param hotelTable is the paginated hotel data
     * @param pages is the row of pages
     * @return the VelocityContext to be merged
     */
    private VelocityContext contextHandler(JsonObject loginInfo, List<String> hotelTable, List<String> pages) {
        VelocityContext context = new VelocityContext();
        String username = loginInfo.get("username").getAsString();
        String previousLogin = loginInfo.get("previousLogin").getAsString();
        context.put("username", username);
        context.put("previousLogin", previousLogin.equals("null") ? null : previousLogin.replace('T', ' '));
        context.put("hotels", hotelTable);
        context.put("pages", pages);
        return context;
    }
}
