package hotelapp.servlets.LoginAndRegistration;

import hotelapp.Database.UserDatabaseHandler;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegistrationServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html");
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        if (!passwordDifficultyCheck(password)) {
            redirect(request, response, LoginStatusCodes.PASSWORD_TOO_EASY);
            return;
        }

        UserDatabaseHandler userDB = new UserDatabaseHandler();
        if (userDB.registerUser(username, password)) {
            redirect(request, response, LoginStatusCodes.ACCOUNT_CREATED);
        } else {
            redirect(request, response, LoginStatusCodes.REGISTRATION_FAILED);
        }
    }

    private boolean passwordDifficultyCheck(String password) {
        String regex = "(?=.*[a-zA-Z])(?=.*\\d)(?=.{8,})";
        Pattern p = Pattern.compile(regex);
        Matcher matcher = p.matcher(password);
        return matcher.find();
    }

    private void redirect(HttpServletRequest request, HttpServletResponse response, int status) throws IOException {
        HttpSession session = request.getSession();
        session.setAttribute("status", status);
        response.sendRedirect("/login");
    }
}
