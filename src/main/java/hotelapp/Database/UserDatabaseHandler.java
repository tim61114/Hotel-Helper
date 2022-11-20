package hotelapp.Database;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.sql.*;
import java.util.Optional;
import java.util.Random;

public class UserDatabaseHandler {
    DatabaseHandler dbHandler = DatabaseHandler.getInstance();
    private final Random random = new Random();

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

    public boolean registerUser(String username, String password) {
        byte[] saltBytes = new byte[16];
        random.nextBytes(saltBytes);

        String userSalt = encodeHex(saltBytes, 32);
        String hashedPassword = getHash(password, userSalt);

        Connection dbConnection = dbHandler.getConnection();
        if (dbConnection == null) {
            return false;
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
            } else {
                System.out.println("Unable to create user.");
            }
            return false;
        }
        return true;
    }

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

    private static String encodeHex(byte[] bytes, int length) {
        BigInteger bigInt = new BigInteger(1, bytes);
        String hex = String.format("%0" + length + "X", bigInt);
        assert hex.length() == length;
        return hex;
    }

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
        System.out.println(userDB.registerUser("testuser", "test"));
        System.out.println(userDB.authenticateUser("testuser", "test"));
        System.out.println(userDB.authenticateUser("test", "testtest"));
    }
}
