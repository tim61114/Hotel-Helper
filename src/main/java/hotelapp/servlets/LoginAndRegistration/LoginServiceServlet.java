package hotelapp.servlets.LoginAndRegistration;

import com.google.gson.JsonObject;
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
import java.time.LocalDateTime;

public class LoginServiceServlet extends HttpServlet {

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
        Template template = v.getTemplate("templates/login.html");
        int errorCode = session.getAttribute("status") == null ? 0 : (int) session.getAttribute("status");

        switch (errorCode) {
            case LoginStatusCodes.LOGIN_FAILED -> context.put("loginError", "Login failed, please check your username/password.");
            case LoginStatusCodes.REGISTRATION_FAILED -> context.put("registerError", "Unable to register your account, try again.");
            case LoginStatusCodes.PASSWORD_TOO_EASY -> context.put("registerError", "Password should contain alphabets, digits and a length over 8.");
            case LoginStatusCodes.ACCOUNT_CREATED -> context.put("accountRegisterSuccess", "Please login with your account.");
        }
        session.setAttribute("status", LoginStatusCodes.SUCCESS);

        StringWriter writer = new StringWriter();
        template.merge(context, writer);

        out.write(writer.toString());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html");
        HttpSession session = request.getSession();
        if (session.getAttribute("loginInfo") != null) {
            response.sendRedirect("/home");
        }
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        UserDatabaseHandler userDB = new UserDatabaseHandler();
        if (userDB.authenticateUser(username, password)) {
            String timestamp = LocalDateTime.now().toString();
            JsonObject loginInfo = new JsonObject();
            loginInfo.addProperty("username", username);
            loginInfo.addProperty("time", timestamp);
            session.setAttribute("loginInfo", loginInfo);
            session.setAttribute("status", LoginStatusCodes.SUCCESS);
            response.sendRedirect("/home");
        } else {
            session.setAttribute("status", LoginStatusCodes.LOGIN_FAILED);
            response.sendRedirect("/login");
        }

    }
}
