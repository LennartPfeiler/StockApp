package mosbach.dhbw.de.stockwizzard.dataManagerImplementation;

import mosbach.dhbw.de.stockwizzard.dataManager.IUserManager;
import mosbach.dhbw.de.stockwizzard.model.User;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.*;

public class UserManagerImplementation implements IUserManager {

    private String databaseConnectionnUrl = "postgresql://mhartwig:BE1yEbCLMjy7r2ozFRGHZaE6jHZUx0fFadiuqgW7TtVs1k15XZVwPSBkPLZVTle6@b8b0e4b9-8325-4a3f-be73-74f20266cd1a.postgresql.eu01.onstackit.cloud:5432/stackit";
    private URI dbUri;
    private String username = "";
    private String password = "";
    private String dbUrl = "";

    PasswordManagerImplementation passwordManager = PasswordManagerImplementation.getPasswordManager();

    private static UserManagerImplementation databaseUser = null;

    private UserManagerImplementation() {
        try {
            dbUri = new URI(databaseConnectionnUrl);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        username = dbUri.getUserInfo().split(":")[0];
        password = dbUri.getUserInfo().split(":")[1];
        dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath() + "?sslmode=require";
    }

    static public UserManagerImplementation getUserManager() {
        if (databaseUser == null)
            databaseUser = new UserManagerImplementation();
        return databaseUser;
    }

    // Create User database table
    public void createUserTable() {
        Statement stmt = null;
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(dbUrl, username, password);
            stmt = connection.createStatement();
            String dropUserTableSQL = "DROP TABLE IF EXISTS group12user";
            stmt.executeUpdate(dropUserTableSQL);

            String createUserTableSQL = "CREATE TABLE group12user (" +
                    "email VARCHAR(100) NOT NULL PRIMARY KEY, " +
                    "firstname VARCHAR(100) NOT NULL, " +
                    "lastname VARCHAR(100) NOT NULL, " +
                    "password VARCHAR(100) NOT NULL, " +
                    "budget DOUBLE PRECISION NOT NULL)";

            stmt.executeUpdate(createUserTableSQL);
        } catch (Exception e) {
            Logger.getLogger("CreateUserTableLogger").log(Level.SEVERE, "User table cannot be created. Error: {0}", e);
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null)
                    stmt.close();
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                Logger.getLogger("CreateUserTableLogger").log(Level.SEVERE,
                        "Error when closing the resource. Error: {0}", e);
                e.printStackTrace();
            }
        }
    }

    // Compare user budget with a number value
    public Boolean checkIfEnoughBudgetLeft(Double needed, User currentUser) {
        if (needed > currentUser.getBudget()) {
            return false;
        }
        return true;
    }

    // Check if an email is registered
    public Boolean isEmailAlreadyRegistered(String email) {
        User user = getUserProfile(email);
        if (user == null) {
            return false;
        } else {
            return true;
        }
    }

    // Get a user profile
    public User getUserProfile(String email) {
        Statement stmt = null;
        Connection connection = null;
        User user = null;
        Logger.getLogger("GetUserByEmail").log(Level.INFO, "Start getUserProfile-method");

        try {
            connection = DriverManager.getConnection(dbUrl, username, password);
            stmt = connection.createStatement();

            String selectUserSQL = "SELECT * FROM group12user WHERE email = '" + email + "'";

            ResultSet rs = stmt.executeQuery(selectUserSQL);
            if (rs.next()) {
                user = new User();
                user.setEmail(rs.getString("email"));
                user.setFirstName(rs.getString("firstname"));
                user.setLastName(rs.getString("lastname"));
                user.setPassword(rs.getString("password"));
                user.setBudget(rs.getDouble("budget"));
            }
            rs.close();
        } catch (SQLException e) {
            Logger.getLogger("GetUserByEmail").log(Level.SEVERE, "Error when getting an user. Error: {0}", e);
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null)
                    stmt.close();
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                Logger.getLogger("GetUserByEmail").log(Level.SEVERE, "Error when closing the resource. Error: {0}", e);
                e.printStackTrace();
            }
        }
        return user;
    }

    // Add a new user
    public void addUser(User user) {
        Statement stmt = null;
        Connection connection = null;
        Logger.getLogger("SetNewUserWriter").log(Level.INFO, "Start addUser-method");
        try {
            connection = DriverManager.getConnection(dbUrl, username, password);
            stmt = connection.createStatement();

            String insertUserSQL = "INSERT into group12user (email, firstname, lastname, password, budget) VALUES (" +
                    "'" + user.getEmail() + "', " +
                    "'" + user.getFirstName() + "', " +
                    "'" + user.getLastName() + "', " +
                    "'" + passwordManager.hashPassword(user.getPassword()) + "', " +
                    +user.getBudget() + ")";

            stmt.executeUpdate(insertUserSQL);
        } catch (SQLException e) {
            Logger.getLogger("SetNewUserWriter").log(Level.SEVERE, "Error when setting a new user. Error: {0}", e);
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null)
                    stmt.close();
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                Logger.getLogger("SetNewUserWriter").log(Level.SEVERE, "Error when closing the resource. Error: {0}",
                        e);
                e.printStackTrace();
            }
        }
    }

    // Edit budget of an user
    public void editUserBudget(String email, Double oldValue, Double bougthValue, Integer transactionType) {
        Statement stmt = null;
        Connection connection = null;
        String updateUserBudgetSQL = "";
        Logger.getLogger("UpdateUserBudgetLogger").log(Level.INFO, "Start editUserBudget method");
    
        try {
            connection = DriverManager.getConnection(dbUrl, username, password);
            stmt = connection.createStatement();
            double newValue;
    
            if (transactionType == 1) {
                // Subtracting the bought value from the old value
                newValue = oldValue - bougthValue;
            } else {
                // Adding the bought value to the old value
                newValue = oldValue + bougthValue;
            }
    
            // Round the result to 2 decimal places
            newValue = Math.round(newValue * 100.0) / 100.0;
    
            updateUserBudgetSQL = "UPDATE group12user SET budget = " + newValue +
                    " WHERE email = '" + email + "'";
    
            stmt.executeUpdate(updateUserBudgetSQL);
        } catch (SQLException e) {
            Logger.getLogger("UpdateUserBudgetLogger").log(Level.SEVERE, "Error updating user budget.", e);
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null)
                    stmt.close();
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                Logger.getLogger("UpdateUserBudgetLogger").log(Level.SEVERE, "Error when closing the resource.", e);
                e.printStackTrace();
            }
        }
    }
    

    // Edit an user profile
    public void editProfile(User currentUser, User newUserData) {
        Logger.getLogger("EditUserLogger").log(Level.WARNING, "Start editProfile-method");
        Statement stmt = null;
        Connection connection = null;
        Double oldBudget = currentUser.getBudget();
        try {
            connection = DriverManager.getConnection(dbUrl, username, password);
            stmt = connection.createStatement();

            String updateUserSQL = "UPDATE group12user SET " +
                    "firstname = '" + newUserData.getFirstName() + "', " +
                    "lastname = '" + newUserData.getLastName() + "', " +
                    "budget = " + (newUserData.getBudget() + oldBudget) +
                    " WHERE email = '" + currentUser.getEmail() + "'";

            stmt.executeUpdate(updateUserSQL);
        } catch (SQLException e) {
            Logger.getLogger("EditUserLogger").log(Level.SEVERE, "Error when editing an user .", e);
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null)
                    stmt.close();
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                Logger.getLogger("EditUserLogger").log(Level.SEVERE, "Error when closing the resource.", e);
                e.printStackTrace();
            }
        }
    }

    // Reset an user profile
    public void resetProfile(String email) {
        Statement stmt = null;
        Connection connection = null;
        Logger.getLogger("ResetProfileLogger").log(Level.INFO, "Start resetProfile method");
        try {
            connection = DriverManager.getConnection(dbUrl, username, password);
            stmt = connection.createStatement();

            String resetUserSQL = "UPDATE group12user SET budget = (SELECT startvalue from group12portfolio WHERE email = '"
                    + email + "') WHERE email = '" + email + "' ";
            stmt.executeUpdate(resetUserSQL);
        } catch (SQLException e) {
            Logger.getLogger("ResetProfileLogger").log(Level.SEVERE, "Error when resetting an user.", e);
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null)
                    stmt.close();
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                Logger.getLogger("ResetProfileLogger").log(Level.SEVERE, "Error when closing the resource.", e);
                e.printStackTrace();
            }
        }
    }

    // Delete an user
    public void deleteUser(String email) {
        Statement stmt = null;
        Connection connection = null;
        Logger.getLogger("DeleteProfileLogger").log(Level.INFO, "Start deleteProfile method");

        try {
            connection = DriverManager.getConnection(dbUrl, username, password);
            stmt = connection.createStatement();

            String deleteUserSQL = "DELETE FROM group12user WHERE email= '" + email + "'";

            stmt.executeUpdate(deleteUserSQL);
        } catch (SQLException e) {
            Logger.getLogger("DeleteProfileLogger").log(Level.SEVERE, "Error when deleting an user.", e);
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null)
                    stmt.close();
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                Logger.getLogger("DeleteProfileLogger").log(Level.SEVERE, "Error when closing the resource.", e);
                e.printStackTrace();
            }
        }
    }

    public int getUserCount() {
        Statement stmt = null;
        Connection connection = null;
        int userCount = 0;
        Logger.getLogger("GetUserCountLogger").log(Level.INFO, "Start getUserCount method");
    
        try {
            connection = DriverManager.getConnection(dbUrl, username, password);
            stmt = connection.createStatement();
    
            String countUsersSQL = "SELECT COUNT(*) AS total FROM group12user";
            ResultSet rs = stmt.executeQuery(countUsersSQL);
    
            if (rs.next()) {
                userCount = rs.getInt("total");
            }
            rs.close();
        } catch (SQLException e) {
            Logger.getLogger("GetUserCountLogger").log(Level.SEVERE, "Fehler beim Zählen der Benutzer.", e);
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null)
                    stmt.close();
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                Logger.getLogger("GetUserCountLogger").log(Level.SEVERE, "Fehler beim Schließen der Ressourcen.", e);
                e.printStackTrace();
            }
        }
        return userCount;
    }
    

}
