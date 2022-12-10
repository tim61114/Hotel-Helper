package hotelapp;

import hotelapp.servlets.Booking.BookingServlet;
import hotelapp.servlets.Booking.DeleteBookingServlet;
import hotelapp.servlets.Home.HomeServlet;
import hotelapp.servlets.Hotel.*;
import hotelapp.servlets.LoginAndRegistration.LoginServiceServlet;
import hotelapp.servlets.LoginAndRegistration.LogoutServlet;
import hotelapp.servlets.LoginAndRegistration.RegistrationServlet;
import hotelapp.servlets.Reviews.*;
import hotelapp.servlets.Search.SearchServlet;
import org.apache.velocity.app.VelocityEngine;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;

public class HotelServer {

	public static final int PORT = 8080;

	public static void main(String[] args) throws Exception {
		Server server = new Server(PORT);
		init(server);

		try {
			server.start();
			server.join();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	/**
	 * Helper method to load servlet classes to server
	 * @param server is the server to add servlets to
	 */
	private static void init(Server server) {
		ServletContextHandler servletHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);

		VelocityEngine velocity = new VelocityEngine();
		velocity.init();

		servletHandler.setAttribute("templateEngine", velocity);
		servletHandler.addServlet(LoginServiceServlet.class, "/login");
		servletHandler.addServlet(HomeServlet.class, "/home");
		servletHandler.addServlet(LogoutServlet.class, "/logout");
		servletHandler.addServlet(RegistrationServlet.class, "/register");
		servletHandler.addServlet(SearchServlet.class, "/search");
		servletHandler.addServlet(HotelServlet.class, "/hotel");
		servletHandler.addServlet(AddReviewServlet.class, "/review");
		servletHandler.addServlet(EditReviewServlet.class, "/edit");
		servletHandler.addServlet(DeleteReviewServlet.class, "/delete");
		servletHandler.addServlet(BookingServlet.class, "/booking");
		servletHandler.addServlet(DeleteBookingServlet.class, "/delete_booking");
		servletHandler.addServlet(VisitExpediaServlet.class, "/expedia");
		servletHandler.addServlet(ExpediaHistoryServlet.class, "/expedia_history");
		servletHandler.addServlet(HotelCoordinatesServlet.class, "/coor");
		servletHandler.addServlet(FavoritesServlet.class, "/favorites");
		servletHandler.addServlet(CheckIsFavoriteServlet.class, "/check_favorite");
		servletHandler.addServlet(DeleteFavoriteServlet.class, "/delete_favorites");
		servletHandler.addServlet(LikeReviewServlet.class, "/like_review");
		servletHandler.addServlet(ReviewLikeNumServlet.class, "/review_num_likes");

		ResourceHandler resourceHandler = new ResourceHandler(); // a handler for serving static pages
		resourceHandler.setDirectoriesListed(true);
		resourceHandler.setResourceBase("templates");

		HandlerList handlers = new HandlerList();
		handlers.setHandlers(new Handler[] { resourceHandler, servletHandler});


		server.setHandler(handlers);
	}
}