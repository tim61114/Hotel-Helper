package hotelapp.servlets.LoginAndRegistration;

import hotelapp.Database.UserDatabaseHandler;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class RegistrationServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html");
        HttpSession session = request.getSession();
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        UserDatabaseHandler userDB = new UserDatabaseHandler();
        if (userDB.registerUser(username, password)) {
            session.setAttribute("status", LoginStatusCodes.ACCOUNT_CREATED);
            response.sendRedirect("/login");
        } else {
            session.setAttribute("status", LoginStatusCodes.REGISTRATION_FAILED);
            response.sendRedirect("/login");
        }

    }
}
