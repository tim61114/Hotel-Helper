package hotelapp.servlets.Booking;

import com.google.gson.JsonObject;
import hotelapp.Database.BookingDatabaseHandler;
import hotelapp.Database.HotelDatabaseHandler;
import hotelapp.Database.UserDatabaseHandler;
import hotelapp.Model.Booking;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BookingServlet extends HttpServlet {

    HashMap<Integer, HashMap<LocalDate, Integer>> hotelDateMap = initMap();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        JsonObject userJson = (JsonObject) session.getAttribute("loginInfo");
        if (userJson == null) {
            response.sendRedirect("/login");
            return;
        }

        BookingDatabaseHandler bookingHandler = new BookingDatabaseHandler();
        List<Booking> bookingList = bookingHandler.getUserBooking(userJson.get("username").getAsString());
        List<String> formattedBookingList = getFormattedBookingList(bookingList);

        PrintWriter out = response.getWriter();
        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);

        VelocityEngine v = (VelocityEngine) request.getServletContext().getAttribute("templateEngine");
        Template template = v.getTemplate("templates/booking.html");

        VelocityContext context = new VelocityContext();
        StringWriter writer = new StringWriter();
        context.put("bookings", formattedBookingList);
        context.put("username", userJson.get("username").getAsString());
        template.merge(context, writer);

        out.write(writer.toString());

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        LocalDate startDate = LocalDate.parse(request.getParameter("startDate"));
        LocalDate endDate = LocalDate.parse(request.getParameter("endDate"));
        int numRooms = Integer.parseInt(request.getParameter("numDays"));
        int hotelId = Integer.parseInt((String) session.getAttribute("currentHotel"));
        String username = ((JsonObject) session.getAttribute("loginInfo")).get("username").getAsString();

        int error;
        if ((error = isValidBooking(hotelId, startDate, endDate, numRooms)) != 0) {
            session.setAttribute("BookingStatus", "" + error);
        } else {
            createBooking(new Booking(hotelId, startDate, endDate, numRooms, username, LocalDateTime.now()));
            session.setAttribute("BookingStatus", "0");
        }
        response.sendRedirect("/hotel?hotelId=" + hotelId);

    }

    private List<String> getFormattedBookingList(List<Booking> bookingList) {
        HotelDatabaseHandler hotelHandler = new HotelDatabaseHandler();
        List<String> formattedList = new ArrayList<>();
        bookingList.forEach(booking -> {
            Hotel curHotel = hotelHandler.getHotelById(booking.hotelId()).get();
            String str =
                    "<tr><td></a href=\"/hotel?hotelId=" + curHotel.hotelId() + "\">" + curHotel.name() + "</td>" +
                    "<td>" + booking.startDate() + "</td>" +
                    "<td>" + booking.endDate() + "</td>" +
                    "<td>" + booking.numRooms() + "</td>" +
                    "<td>" + booking.timeBooked() + "</td></tr>";
            formattedList.add(str);
        });
        return formattedList;
    }

    private HashMap<Integer, HashMap<LocalDate, Integer>> initMap() {
        HashMap<Integer, HashMap<LocalDate, Integer>> hotelDateMap = new HashMap<>();
        BookingDatabaseHandler bookingHandler = new BookingDatabaseHandler();
        HotelDatabaseHandler hotelHandler = new HotelDatabaseHandler();
        hotelHandler.getHotelIdList().forEach(id -> hotelDateMap.put(id, new HashMap<>()));
        bookingHandler.getAllBookings().forEach(booking -> {
            HashMap<LocalDate, Integer> curMap = hotelDateMap.get(booking.hotelId());
            LocalDate start = booking.startDate(), end = booking.endDate();
            while (start.isBefore(end.plusDays(1))) {
                curMap.put(start, curMap.getOrDefault(start, 0) + booking.numRooms());
                start = start.plusDays(1);
            }
        });

        return hotelDateMap;
    }

    private int isValidBooking(int hotelId, LocalDate startDate, LocalDate endDate, int numRooms) {
        if (numRooms > 3) {
            return 3; // Too many rooms
        }
        if (startDate.isAfter(endDate)) {
            return 2; // Bad date
        }

        if (!isAvailable(hotelId, startDate, endDate, numRooms)) {
            return 1; // No room available
        }

        return 0;
    }

   private boolean isAvailable(int hotelId, LocalDate startDate, LocalDate endDate, int numRooms) {
        LocalDate start = startDate, end = endDate;
        while (start.isBefore(end.plusDays(1))) {
            if (hotelDateMap.get(hotelId).getOrDefault(start, 0) + numRooms > 3 ) {
                return false;
            }
            start = start.plusDays(1);
        }
        return true;
   }

   private void createBooking(Booking booking) {
       BookingDatabaseHandler bookingHandler = new BookingDatabaseHandler();
       LocalDate start = booking.startDate(), end = booking.endDate();
       HashMap<LocalDate, Integer> curMap = hotelDateMap.get(booking.hotelId());
       while (start.isBefore(end.plusDays(1))) {
            curMap.put(start, curMap.getOrDefault(start, 0) + booking.numRooms());
            start = start.plusDays(1);
        }

        bookingHandler.addBooking(booking);
   }
}
