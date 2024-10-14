package mosbach.dhbw.de.stockwizzard.dataManagerImplementation;

import java.io.*;
import mosbach.dhbw.de.stockwizzard.dataManager.IUserManager;
import mosbach.dhbw.de.stockwizzard.dataManagerImplementation.PasswordManagerImplementation;
import mosbach.dhbw.de.stockwizzard.model.User;
import mosbach.dhbw.de.stockwizzard.model.EditRequest;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.hibernate.Session;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.*;

public class UserManagerImplementation implements IUserManager{

    String databaseConnectionnUrl = "postgresql://mhartwig:BE1yEbCLMjy7r2ozFRGHZaE6jHZUx0fFadiuqgW7TtVs1k15XZVwPSBkPLZVTle6@b8b0e4b9-8325-4a3f-be73-74f20266cd1a.postgresql.eu01.onstackit.cloud:5432/stackit";
    URI dbUri;
    String username = "";
    String password = "";
    String dbUrl = "";
    
    PasswordManagerImplementation passwordManager = PasswordManagerImplementation.getPasswordManager();

    static UserManagerImplementation databaseUser = null;

    private UserManagerImplementation(){
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
    //user
    public void createUserTable() {

        Statement stmt = null;
        Connection connection = null;

        try {
            connection = DriverManager.getConnection(dbUrl, username, password);
            stmt = connection.createStatement();
            String dropTable = "DROP TABLE IF EXISTS group12user";
            stmt.executeUpdate(dropTable);

            String createTable = "CREATE TABLE group12user (" +
                    "email VARCHAR(100) NOT NULL PRIMARY KEY, " +
                    "firstname VARCHAR(100) NOT NULL, " +
                    "lastname VARCHAR(100) NOT NULL, " +
                    "password VARCHAR(100) NOT NULL, " +
                    "budget DOUBLE PRECISION NOT NULL)";

            stmt.executeUpdate(createTable);
        } catch (Exception e) {
            Logger.getLogger("CreateUserTableLogger").log(Level.INFO, "User table cannot be created. Error: {0}", e);
        } finally {
            try {
                // Schließen von Statement und Connection, um Ressourcen freizugeben
                if (stmt != null) stmt.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                // Fehler beim Schließen protokollieren
                Logger.getLogger("CreateUserTableLogger").log(Level.SEVERE, "Error beim Schließen der Ressourcen. Error: {0}", e);
            }
        }
    }

    public Boolean CheckIfEnoughBudgetLeft(Double needed, User currentUser){
        // Überprüfen, ob das benötigte Budget größer ist als das verfügbare Budget
        if(needed > currentUser.getBudget()){
            return false; // Nicht genug Budget vorhanden
        }
    
        // Ansonsten: Genug Budget vorhanden
        return true;
    }

    public Boolean isEmailAlreadyRegistered(String email) {
        User user = getUserProfile(email);
        if(user == null){
            return false;
        }
        else{
            return true;
        }
    }    

    public User getUserProfile(String email) {
        Statement stmt = null;
        Connection connection = null;
        User user = null;
        Logger.getLogger("GetUserByEmail").log(Level.INFO, "Start getUserProfile-method");
        
        try {
            // Verbindung zur Datenbank herstellen
            connection = DriverManager.getConnection(dbUrl, username, password);
            stmt = connection.createStatement();

            // SQL-Abfrage definieren
            String selectSQL = "SELECT * FROM group12user WHERE email = '" + email + "'";

            // Ausführen der SELECT-Abfrage
            ResultSet rs = stmt.executeQuery(selectSQL);

            // Prüfen, ob ein Ergebnis zurückgegeben wurde
            if (rs.next()) {
                // Benutzerobjekt basierend auf den Ergebnissen erstellen
                user = new User();
                user.setEmail(rs.getString("email"));
                user.setFirstName(rs.getString("firstname"));
                user.setLastName(rs.getString("lastname"));
                user.setPassword(rs.getString("password"));
                user.setBudget(rs.getDouble("budget"));
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                // Schließen von Statement und Connection, um Ressourcen freizugeben
                if (stmt != null) stmt.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                // Fehler beim Schließen protokollieren
                Logger.getLogger("GetUserByEmail").log(Level.SEVERE, "Error beim Schließen der Ressourcen. Error: {0}", e);
            }
        }
        return user;
    }

    public void addUser(User user) {
        Statement stmt = null;
        Connection connection = null;
        Logger.getLogger("SetNewUserWriter").log(Level.INFO, "Start addUser-method");
        try {
            connection = DriverManager.getConnection(dbUrl, username, password);
            stmt = connection.createStatement();
            String udapteSQL = "INSERT into group12user (email, firstname, lastname, password, budget) VALUES (" +
                    "'" + user.getEmail() + "', " +
                    "'" + user.getFirstName() + "', " +
                    "'" + user.getLastName() + "', " +
                    "'" + passwordManager.hashPassword(user.getPassword()) + "', " +
                    "'" + user.getBudget() + "')";

            stmt.executeUpdate(udapteSQL);     
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                // Schließen von Statement und Connection, um Ressourcen freizugeben
                if (stmt != null) stmt.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                // Fehler beim Schließen protokollieren
                Logger.getLogger("SetNewUserWriter").log(Level.SEVERE, "Error beim Schließen der Ressourcen. Error: {0}", e);
            }
        }
    }

    public void editUserBudget(String email, Double oldValue, Double bougthValue, Integer transactionType){
        Statement stmt = null;
        Connection connection = null;
        Logger.getLogger("UpdatePortfolioValueLogger").log(Level.INFO, "Start updatePortfolioValue method");

        try {
            // Stelle die Verbindung zur Datenbank her
            connection = DriverManager.getConnection(dbUrl, username, password);
            stmt = connection.createStatement();

            if(transactionType == 1){
                String updateSQL = "UPDATE group12user SET budget = " + (oldValue - bougthValue) +
                            " WHERE email = '" + email + "'";
            }
            else{
                String updateSQL = "UPDATE group12user SET budget = " + (oldValue + bougthValue) +
                            " WHERE email = '" + email + "'";
            }

            // Führe die SQL-Anweisung aus
            stmt.executeUpdate(updateSQL);
        } catch (SQLException e) {
            Logger.getLogger("UpdatePortfolioValueLogger").log(Level.SEVERE, "Error updating portfolio value.", e);
        } finally {
            try {
                // Schließen von Statement und Connection, um Ressourcen freizugeben
                if (stmt != null) stmt.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                // Fehler beim Schließen protokollieren
                Logger.getLogger("UpdatePortfolioValueLogger").log(Level.SEVERE, "Error closing resources.", e);
            }
        }
    }

    public void editUser(String currentEmail, User user){
        Logger.getLogger("EditUserLogger").log(Level.WARNING, "Start editUser-method");
        Statement stmt = null;
        Connection connection = null;
        // Prüfen, ob die E-Mail geändert werden muss
        String newEmail = user.getEmail();
        boolean emailChanged = !newEmail.equals(currentEmail);
        User currentUser = getUserProfile(currentEmail);
        Double oldBudget = currentUser.getBudget();

        if(emailChanged == false){
            try{
                connection = DriverManager.getConnection(dbUrl, username, password);
                stmt = connection.createStatement();

                // SQL-Anweisung für das Aktualisieren des Portfolio-Wertes
                String updateSQL = "UPDATE group12user SET " +
                   "firstname = '" + user.getFirstName() + "', " +
                   "lastname = '" + user.getLastName() + "', " +
                   "budget = " + (user.getBudget() + oldBudget) +
                   " WHERE email = '" + currentEmail + "'";

                stmt.executeUpdate(updateSQL);
            } catch (SQLException e) {
                Logger.getLogger("EditUserLogger").log(Level.SEVERE, "Error editing User .", e);
            } finally {
                try {
                    // Schließen von Statement und Connection, um Ressourcen freizugeben
                    if (stmt != null) stmt.close();
                    if (connection != null) connection.close();
                } catch (SQLException e) {
                    // Fehler beim Schließen protokollieren
                    Logger.getLogger("EditUserLogger").log(Level.SEVERE, "Error closing resources.", e);
                }
            }
        }
        else{
            try{
                connection = DriverManager.getConnection(dbUrl, username, password);
                stmt = connection.createStatement();

                // SQL-Anweisung für das Aktualisieren des Portfolio-Wertes
                if(isEmailAlreadyRegistered(newEmail) == false){
                String newUserSQL = "INSERT into group12user (email, firstname, lastname, password, budget) VALUES (" +
                    "'" + user.getEmail() + "', " +
                    "'" + user.getFirstName() + "', " +
                    "'" + user.getLastName() + "', " +
                    "'" + passwordManager.hashPassword(user.getPassword()) + "', " +
                     + (user.getBudget() + oldBudget) + ")";
                String updateSessionsSQL = "UPDATE group12session SET email = '" + user.getEmail() + "' WHERE email = '" + currentEmail + "'";
                String updateTransactionsSQL = "UPDATE group12transaction SET email = '" + user.getEmail() + "' WHERE email = '" + currentEmail + "'";
                String updatePortfolioSQL = "UPDATE group12portfolio SET email = '" + user.getEmail() + "' WHERE email = '" + currentEmail + "'";
                String deleteOldUserSQL = "DELETE FROM group12user WHERE email= '" + currentEmail + "'";

                stmt.executeUpdate(newUserSQL);
                stmt.executeUpdate(updateSessionsSQL);
                stmt.executeUpdate(updateTransactionsSQL);
                stmt.executeUpdate(updatePortfolioSQL);
                stmt.executeUpdate(deleteOldUserSQL);
                }
            } catch (SQLException e) {
                Logger.getLogger("EditUserLogger").log(Level.SEVERE, "Error editing User .", e);
            } finally {
                try {
                    // Schließen von Statement und Connection, um Ressourcen freizugeben
                    if (stmt != null) stmt.close();
                    if (connection != null) connection.close();
                } catch (SQLException e) {
                    // Fehler beim Schließen protokollieren
                    Logger.getLogger("EditUserLogger").log(Level.SEVERE, "Error closing resources.", e);
                }
            }
        }
    }

    public User resetProfile(String email){
        Statement stmt = null;
        Connection connection = null;
        Logger.getLogger("ResetProfileLogger").log(Level.INFO, "Start resetProfile method");

        try {
            // Stelle die Verbindung zur Datenbank her
            connection = DriverManager.getConnection(dbUrl, username, password);
            stmt = connection.createStatement();

            String deletePortfolioStocks = "DELETE FROM group12portfoliostock WHERE portfolioid = (SELECT portfolioid FROM group12portfolio WHERE email= '" + email + "')";
            String deleteTransactions = "DELETE FROM group12transaction WHERE email= '" + email + "'";
            String updatePortfolio = "UPDATE group12portfolio SET value = (SELECT startvalue from group12portfolio WHERE email = '" + email + "') WHERE email = '" + email + "' "; 
            String updateUser = "UPDATE group12user SET budget = (SELECT startvalue from group12portfolio WHERE email = '" + email + "') WHERE email = '" + email + "' "; 

            // Führe die SQL-Anweisung au
            stmt.executeUpdate(deletePortfolioStocks);
            stmt.executeUpdate(deleteTransactions);
            stmt.executeUpdate(updatePortfolio);
            stmt.executeUpdate(updateUser);
        } catch (SQLException e) {
            Logger.getLogger("ResetProfileLogger").log(Level.SEVERE, "Error resetting user.", e);
        } finally {
            try {
                // Schließen von Statement und Connection, um Ressourcen freizugeben
                if (stmt != null) stmt.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                // Fehler beim Schließen protokollieren
                Logger.getLogger("ResetProfileLogger").log(Level.SEVERE, "Error closing resources.", e);
            }
        }
        return getUserProfile(email);
    }


    //@Override
    public void deleteUser(String email) {
        Statement stmt = null;
        Connection connection = null;
        Logger.getLogger("DeleteProfileLogger").log(Level.INFO, "Start deleteProfile method");

        try {
            // Stelle die Verbindung zur Datenbank her
            connection = DriverManager.getConnection(dbUrl, username, password);
            stmt = connection.createStatement();

            String deleteProfile = "DELETE FROM group12user WHERE email= '" + email + "'";
            
            // Führe die SQL-Anweisung au
            stmt.executeUpdate(deleteProfile);
        } catch (SQLException e) {
            Logger.getLogger("DeleteProfileLogger").log(Level.SEVERE, "Error deleting user.", e);
        } finally {
            try {
                // Schließen von Statement und Connection, um Ressourcen freizugeben
                if (stmt != null) stmt.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                // Fehler beim Schließen protokollieren
                Logger.getLogger("DeleteProfileLogger").log(Level.SEVERE, "Error closing resources.", e);
            }
        }
    }

}
