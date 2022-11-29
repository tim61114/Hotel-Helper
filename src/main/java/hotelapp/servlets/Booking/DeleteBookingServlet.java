package hotelapp.servlets.Booking;

import hotelapp.Database.BookingDatabaseHandler;
import hotelapp.Model.Booking;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class DeleteBookingServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        String bookingId = request.getParameter("booking_id");
        BookingDatabaseHandler bookingHandler = new BookingDatabaseHandler();
        Booking booking = bookingHandler.getBookingByBookingId(bookingId);
        // Add session element to track removal status. 0 for successful deletion, 1 for unsuccessful.
        if (bookingHandler.deleteBooking(bookingId)) {
            session.setAttribute("removeStatus", "0");
            BookingServlet.removeFromMap(booking);
        } else {
            session.setAttribute("removeStatus", "1");
        }
        response.sendRedirect("/booking");
    }
}
