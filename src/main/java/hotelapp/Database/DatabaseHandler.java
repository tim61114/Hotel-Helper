package hotelapp.Database;

import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseHandler {
    private static final DatabaseHandler databaseHandler = new DatabaseHandler("database.properties");
    private final Properties config;
    private final String URI;


    private DatabaseHandler(String properties) {
        this.config = loadConfig(properties);
        this.URI = "jdbc:mysql://"+ config.getProperty("hostname") + "/" + config.getProperty("database");
    }

    /**
     * A singleton design pattern
     * @return the databaseHandler object
     */
    public static DatabaseHandler getInstance() { return databaseHandler; }


    /**
     * Connect to the database
     * @return a Connection object if success, otherwise null
     */
    public Connection getConnection() {
        try {
            return DriverManager.getConnection(URI, config.getProperty("username"), config.getProperty("password"));
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            System.out.println("Unable to connect to db");
        }
        return null;
    }

    /**
     * Load database config file
     * @param propertyFile is the property file
     * @return a Properties object
     */
    private Properties loadConfig(String propertyFile) {
        Properties config = new Properties();
        try (FileReader reader = new FileReader(propertyFile)) {
            config.load(reader);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return config;
    }

}
