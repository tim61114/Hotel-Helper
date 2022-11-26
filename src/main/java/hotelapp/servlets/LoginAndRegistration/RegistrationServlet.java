package hotelapp.servlets.LoginAndRegistration;

import com.mysql.cj.log.Log;
import hotelapp.Database.DatabaseErrorCodes;
import hotelapp.Database.UserDatabaseHandler;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegistrationServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        if (session.getAttribute("loginInfo") != null) {
            response.sendRedirect("/home");
        }
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        VelocityEngine v = (VelocityEngine) request.getServletContext().getAttribute("templateEngine");
        VelocityContext context = new VelocityContext();
        Template template = v.getTemplate("templates/register.html");
        int errorCode = session.getAttribute("status") == null ? 0 : (int) session.getAttribute("status");

        switch (errorCode) {
            case LoginStatusCodes.REGISTRATION_FAILED -> context.put("registerError", "Username already exists.");
            case LoginStatusCodes.PASSWORD_TOO_EASY -> context.put("registerError", "Password should contain alphabets, digits and a length over 8.");
        }

        session.setAttribute("status", LoginStatusCodes.SUCCESS);

        StringWriter writer = new StringWriter();
        template.merge(context, writer);

        out.write(writer.toString());
    }

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
        switch (userDB.registerUser(username, password)) {
            case DatabaseErrorCodes.SUCCESS -> redirect(request, response, LoginStatusCodes.ACCOUNT_CREATED);
            case DatabaseErrorCodes.USER_EXISTS -> redirect(request, response, LoginStatusCodes.REGISTRATION_FAILED);
            default -> redirect(request, response, LoginStatusCodes.CONNECTION_ERROR);
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
        if (status != LoginStatusCodes.ACCOUNT_CREATED) {
            response.sendRedirect("/register");
            return;
        }
        response.sendRedirect("/login");
    }
}
