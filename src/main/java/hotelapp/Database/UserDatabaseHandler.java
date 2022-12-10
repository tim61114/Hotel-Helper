package hotelapp.Database;

import javax.xml.crypto.Data;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

public class UserDatabaseHandler {
    private final DatabaseHandler dbHandler = DatabaseHandler.getInstance();
    private final Random random = new Random();

    /**
     * Create a user table
     * @return true if successfully created, otherwise false
     */
    public boolean createUserTable() {
        Connection dbConnection = dbHandler.getConnection();
        if (dbConnection == null) {
            System.out.println("Unable to connect to database.");
            return false;
        }
        try {
            Statement statement = dbConnection.createStatement();
            statement.executeUpdate(PreparedStatements.CREATE_USER_TABLE);
        } catch (SQLException e) {
            if (e.getErrorCode() == DatabaseErrorCodes.TABLE_EXISTS) {
                System.out.println("User table already exists.");
            } else {
                System.out.println("An error occurred.");
            }
            return false;
        }
        return true;
    }

    /**
     * Drop the user table
     * @return true if successfully dropped, otherwise false
     */
    public boolean dropUserTable() {
        Connection dbConnection = dbHandler.getConnection();
        if (dbConnection == null) {
            System.out.println("Unable to connect to database.");
            return false;
        }
        try {
            Statement statement = dbConnection.createStatement();
            statement.executeUpdate(PreparedStatements.DROP_USER_TABLE);
        } catch (SQLException e) {
            if (e.getErrorCode() == DatabaseErrorCodes.TABLE_DOES_NOT_EXIST) {
                System.out.println("Table users does not exist. Aborted.");
            } else {
                System.out.println("An error occurred.");
            }
            return false;
        }
        return true;
    }

    /**
     * Store the user into the database
     * @param username is the username to be stored
     * @param password is the password of the user
     * @return a value indicating success or failure
     */
    public int registerUser(String username, String password) {
        byte[] saltBytes = new byte[16];
        random.nextBytes(saltBytes);

        String userSalt = encodeHex(saltBytes, 32);
        String hashedPassword = getHash(password, userSalt);

        Connection dbConnection = dbHandler.getConnection();
        if (dbConnection == null) {
            return DatabaseErrorCodes.CANNOT_CONNECT_TO_SQL_SERVER;
        }

        try {
            PreparedStatement statement = dbConnection.prepareStatement(PreparedStatements.REGISTER_SQL);
            statement.setString(1, username);
            statement.setString(2, hashedPassword);
            statement.setString(3, userSalt);
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            if (e.getErrorCode() == DatabaseErrorCodes.USER_EXISTS) {
                System.out.println("User already exists");
                return DatabaseErrorCodes.USER_EXISTS;
            } else {
                System.out.println("Unable to create user.");
                return DatabaseErrorCodes.UNSTATED_ERROR;
            }
        }
        return DatabaseErrorCodes.SUCCESS;
    }

    /**
     * Check if the login credential matches a user in the database
     * @param username is the logged-in username
     * @param password is the logged-in password
     * @return true if success, otherwise false
     */
    public boolean authenticateUser(String username, String password) {
        Connection dbConnection = dbHandler.getConnection();
        if (dbConnection == null) {
            System.out.println("Unable to connect to database.");
            return false;
        }

        try {
            PreparedStatement statement = dbConnection.prepareStatement(PreparedStatements.AUTH_SQL);
            String userSalt = getSalt(dbConnection, username).orElse("");
            String hashedPassword = getHash(password, userSalt);
            if (hashedPassword == null) {
                throw new Exception("Password hash failed");
            }

            statement.setString(1, username);
            statement.setString(2, hashedPassword);
            ResultSet results = statement.executeQuery();

            return results.next();
        } catch (SQLException e) {
            System.out.println(e.getErrorCode());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

    public boolean createLoginInfoTable() {
        Connection dbConnection = dbHandler.getConnection();
        if (dbConnection == null) {
            System.out.println("Unable to connect to database.");
            return false;
        }
        try {
            Statement statement = dbConnection.createStatement();
            statement.executeUpdate(PreparedStatements.CREATE_LOGIN_INFO_TABLE);
        } catch (SQLException e) {
            if (e.getErrorCode() == DatabaseErrorCodes.TABLE_EXISTS) {
                System.out.println("Login_info table already exists.");
            } else {
                System.out.println("An error occurred.");
            }
            return false;
        }
        return true;
    }

    public boolean dropLoginInfoTable() {
        Connection dbConnection = dbHandler.getConnection();
        if (dbConnection == null) {
            System.out.println("Unable to connect to database.");
            return false;
        }
        try {
            Statement statement = dbConnection.createStatement();
            statement.executeUpdate(PreparedStatements.DROP_LOGIN_INFO_TABLE);
        } catch (SQLException e) {
            if (e.getErrorCode() == DatabaseErrorCodes.TABLE_DOES_NOT_EXIST) {
                System.out.println("Table login_info does not exist. Aborted.");
            } else {
                System.out.println("An error occurred.");
            }
            return false;
        }
        return true;
    }

    public LocalDateTime getPreviousLogin(String username) {
        Connection dbConnection = dbHandler.getConnection();
        if (dbConnection == null) {
            System.out.println("Unable to connect to database.");
            return null;
        }

        try {
            PreparedStatement statement = dbConnection.prepareStatement(PreparedStatements.GET_PREVIOUS_LOGIN_BY_USERNAME);
            statement.setString(1, username);
            ResultSet results = statement.executeQuery();
            if (results.next()) {
                return results.getTimestamp(1).toLocalDateTime();
            }
        } catch (SQLException e) {
            System.out.println(e.getErrorCode());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    /**
     * Update the previous login time of the current user
     * @param username is the current user
     * @param time is the login time
     */
    public void updatePreviousLogin(String username, LocalDateTime time) {
        Connection dbConnection = dbHandler.getConnection();
        if (dbConnection == null) {
            System.out.println("Unable to connect to database.");
            return;
        }

        try {
            PreparedStatement statement = dbConnection.prepareStatement(PreparedStatements.INSERT_TO_LOGIN_INFO);
            statement.setString(1, username);
            statement.setTimestamp(2, Timestamp.valueOf(time));
            statement.setTimestamp(3, Timestamp.valueOf(time));
            statement.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getErrorCode());
            System.out.println("An error occurred.");
        }
    }

    /**
     * Get the salt string of a user
     * @param connection is a database connection
     * @param username is the target user
     * @return an Optional of a String, optional will be empty if failed to find.
     */
    private static Optional<String> getSalt(Connection connection, String username) {
        String salt = null;
        try (PreparedStatement statement = connection.prepareStatement(PreparedStatements.SALT_SQL)) {
            statement.setString(1, username);
            ResultSet results = statement.executeQuery();
            if (results.next()) {
                salt = results.getString("usersalt");
            }
        }
        catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return Optional.ofNullable(salt);
    }

    /**
     * Helper method of hashing
     */
    private static String encodeHex(byte[] bytes, int length) {
        BigInteger bigInt = new BigInteger(1, bytes);
        String hex = String.format("%0" + length + "X", bigInt);
        assert hex.length() == length;
        return hex;
    }

    /**
     * Get hashed password
     * @param password is the target password to be hashed
     * @param salt is the user's salt String
     * @return the hashed String
     */
    private static String getHash(String password, String salt) {
        String salted = salt + password;
        String hashed = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salted.getBytes());
            hashed = encodeHex(md.digest(), 64);
        } catch (Exception ex) {
            System.out.println("Password hash failed");
        }

        return hashed;
    }

    public static void main(String[] args) {
        UserDatabaseHandler userDB = new UserDatabaseHandler();
        //System.out.println(userDB.dropUserTable());
        //System.out.println(userDB.createUserTable());
//        System.out.println(userDB.authenticateUser("test", "testtest"));
//        System.out.println(userDB.registerUser("testuser", "test"));
//        System.out.println(userDB.authenticateUser("testuser", "test"));
        userDB.createLoginInfoTable();
    }
}
