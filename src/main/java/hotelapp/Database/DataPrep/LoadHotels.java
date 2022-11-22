package hotelapp.Database.DataPrep;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import hotelapp.Database.DatabaseHandler;
import hotelapp.Database.PreparedStatements;

import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class LoadHotels {
    public static void LoadHotelsToDB(String hotelJson) {
        Connection dbConnection = DatabaseHandler.getInstance().getConnection();
        if (dbConnection == null) {
            System.out.println("Unable to connect to database");
            return;
        }

        try (FileReader reader = new FileReader(hotelJson)) {
            JsonObject rawData = (JsonObject) JsonParser.parseReader(reader);
            JsonArray hotelArr = rawData.getAsJsonArray("sr");

            for (JsonElement hotelElem: hotelArr) {
                JsonObject currentHotelJson = hotelElem.getAsJsonObject();
                String name = currentHotelJson.get("f").getAsString();
                int id = Integer.parseInt(currentHotelJson.get("id").getAsString());
                JsonObject coordinatesJson = currentHotelJson.get("ll").getAsJsonObject();
                double lat = coordinatesJson.get("lat").getAsDouble();
                double lng = coordinatesJson.get("lng").getAsDouble();
                String address = currentHotelJson.get("ad") != null ? currentHotelJson.get("ad").getAsString() : "";
                String city = currentHotelJson.get("ci") != null ? currentHotelJson.get("ci").getAsString() : "";
                String state = currentHotelJson.get("pr") != null ? currentHotelJson.get("pr").getAsString() : "";
                String country = currentHotelJson.get("c") != null ? currentHotelJson.get("c").getAsString() : "";

                PreparedStatement statement = dbConnection.prepareStatement(PreparedStatements.ADD_HOTEL);
                statement.setString(1, name);
                statement.setInt(2, id);
                statement.setDouble(3, lat);
                statement.setDouble(4, lng);
                statement.setString(5, address);
                statement.setString(6, city);
                statement.setString(7, state);
                statement.setString(8, country);
                statement.executeUpdate();
                statement.close();
            }
        } catch (IOException e) {
            System.out.println("Error: File not found");
        } catch (SQLException e) {
            System.out.println(e.getErrorCode());
            System.out.println(e.getMessage());
        }
    }
}
