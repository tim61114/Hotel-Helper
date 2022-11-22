package hotelapp;

import hotelapp.servlets.Home.HomeServlet;
import hotelapp.servlets.LoginAndRegistration.LoginServiceServlet;
import hotelapp.servlets.LoginAndRegistration.LogoutServlet;
import hotelapp.servlets.LoginAndRegistration.RegistrationServlet;
import hotelapp.servlets.Search.SearchServlet;
import org.apache.velocity.app.VelocityEngine;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;

public class HotelServer {

	public static final int PORT = 8080;

	public static void main(String[] args) throws Exception {
		Server server = new Server(PORT);

		ServletContextHandler handler = new ServletContextHandler(ServletContextHandler.SESSIONS);

		VelocityEngine velocity = new VelocityEngine();
		velocity.init();

		handler.setAttribute("templateEngine", velocity);
		handler.addServlet(LoginServiceServlet.class, "/login");
		handler.addServlet(HomeServlet.class, "/home");
		handler.addServlet(LogoutServlet.class, "/logout");
		handler.addServlet(RegistrationServlet.class, "/register");
		handler.addServlet(SearchServlet.class, "/search");
		server.setHandler(handler);


		try {
			server.start();
			server.join();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
}