package hotelapp.servlets.LoginAndRegistration;

import com.google.gson.JsonObject;
import hotelapp.Database.UserDatabaseHandler;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDateTime;

public class LogoutServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html");
        HttpSession session = request.getSession();
        UserDatabaseHandler userDB = new UserDatabaseHandler();
        JsonObject loginInfo = (JsonObject) session.getAttribute("loginInfo");
        String username = loginInfo.get("username").getAsString();
        LocalDateTime time = LocalDateTime.parse(loginInfo.get("time").getAsString());
        userDB.updatePreviousLogin(username, time);
        session.removeAttribute("loginInfo");
        response.sendRedirect("/login");
    }
}
