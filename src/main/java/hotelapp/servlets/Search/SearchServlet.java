package hotelapp.servlets.Search;

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

public class SearchServlet extends HttpServlet {

    private static final int HOTEL_PER_PAGE = 20;
    private final HotelDatabaseHandler hotelHandler = new HotelDatabaseHandler();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        JsonObject userJson = (JsonObject) session.getAttribute("loginInfo");
        if (userJson == null) {
            response.sendRedirect("/login");
            return;
        }

        String keyword = request.getParameter("keyword");
        if (keyword == null) {
            response.sendRedirect("/home");
        }

        String searchPage = request.getParameter("searchPage");
        if (searchPage != null) {
            session.setAttribute("searchPage", searchPage);
            response.sendRedirect("/search?keyword=" + keyword);
        }

        String searchPageNumString = (String) session.getAttribute("searchPage");
        int searchPageNum = 1;
        if (searchPageNumString != null) {
            searchPageNum = Integer.parseInt(searchPageNumString);
        }

        List<String> searchResult = hotelHandler.getProcessedHotels(keyword);
        int totalPages = searchResult.size() / HOTEL_PER_PAGE;

        List<String> resultTable = searchResult.stream()
                .skip((long) (searchPageNum - 1) * HOTEL_PER_PAGE)
                .limit(HOTEL_PER_PAGE)
                .toList();

        List<String> pages = new ArrayList<>();
        for (int i = 1; i <= totalPages + 1; i++) {
            //pages.add("<a href=\"/search?keyword=" + keyword + "&searchPage=" + i + "\">" + i + "</a>");
            pages.add("/search?keyword=" + keyword + "&searchPage=" + i);
        }
        PrintWriter out = response.getWriter();
        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);

        VelocityEngine v = (VelocityEngine) request.getServletContext().getAttribute("templateEngine");
        VelocityContext context = new VelocityContext();
        context.put("currentPage", searchPageNum);
        context.put("keyword", keyword);
        context.put("hotels", resultTable);
        context.put("pages", pages);
        Template template = v.getTemplate("templates/search.html");

        StringWriter writer = new StringWriter();
        template.merge(context, writer);

        out.write(writer.toString());

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html");
        String keyword = request.getParameter("searchColumn");
        if (keyword.equals("")) {
            response.sendRedirect("/home");
            return;
        }
        response.sendRedirect("/search?keyword=" + keyword.replaceAll(" ", "+"));
    }

}
