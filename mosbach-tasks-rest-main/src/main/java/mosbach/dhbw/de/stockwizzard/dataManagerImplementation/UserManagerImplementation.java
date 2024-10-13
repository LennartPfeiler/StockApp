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

    public void editUserBudget(String email, Double oldValue, Double bougthValue){
        Statement stmt = null;
        Connection connection = null;
        Logger.getLogger("UpdatePortfolioValueLogger").log(Level.INFO, "Start updatePortfolioValue method");

        try {
            // Stelle die Verbindung zur Datenbank her
            connection = DriverManager.getConnection(dbUrl, username, password);
            stmt = connection.createStatement();

            // SQL-Anweisung für das Aktualisieren des Portfolio-Wertes
            String updateSQL = "UPDATE group12user SET budget = " + (oldValue - bougthValue) +
                            " WHERE email = '" + email + "'";

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

    // public boolean editUser(String currentEmail, User user) {
    //     Logger.getLogger("EditUser").log(Level.WARNING, "Start editUser-method");

    //     // Prüfen, ob die E-Mail geändert werden muss
    //     String newEmail = user.getEmail();
    //     boolean emailChanged = !newEmail.equals(currentEmail);

    //     try (Connection connection = DriverManager.getConnection(dbUrl, username, password)) {
    //         connection.setAutoCommit(false); // Beginne Transaktion

    //         // 1. Aktualisiere group12user-Tabelle (ohne Email)
    //         try (PreparedStatement stmtUpdateUser = connection.prepareStatement(
    //                 "UPDATE group12user SET firstname = ?, lastname = ?, budget = ? WHERE email = ?")) {

    //             stmtUpdateUser.setString(1, user.getFirstName());
    //             stmtUpdateUser.setString(2, user.getLastName());
    //             stmtUpdateUser.setDouble(3, user.getBudget());
    //             stmtUpdateUser.setString(4, currentEmail); // Alte E-Mail als Bedingung

    //             int rowsAffected = stmtUpdateUser.executeUpdate();
    //             if (rowsAffected == 0) {
    //                 // Kein Benutzer mit der angegebenen E-Mail gefunden, Abbruch
    //                 connection.rollback();
    //                 return false;
    //             }
    //         }

    //         // 2. Aktualisiere andere Tabellen, falls die E-Mail geändert wurde
    //         if (emailChanged) {
    //             try (PreparedStatement stmtUpdatePortfolio = connection.prepareStatement(
    //                     "UPDATE group12portfolio SET email = ? WHERE email = ?");
    //                 PreparedStatement stmtUpdateSession = connection.prepareStatement(
    //                     "UPDATE group12session SET email = ? WHERE email = ?");
    //                 PreparedStatement stmtUpdateTransaction = connection.prepareStatement(
    //                     "UPDATE group12transaction SET email = ? WHERE email = ?")) {

    //                 // Setze Parameter für alle Fremdschlüssel-Tabellen
    //                 stmtUpdatePortfolio.setString(1, newEmail);
    //                 stmtUpdatePortfolio.setString(2, currentEmail);
    //                 stmtUpdateSession.setString(1, newEmail);
    //                 stmtUpdateSession.setString(2, currentEmail);
    //                 stmtUpdateTransaction.setString(1, newEmail);
    //                 stmtUpdateTransaction.setString(2, currentEmail);

    //                 // Führe die Updates aus
    //                 stmtUpdatePortfolio.executeUpdate();
    //                 stmtUpdateSession.executeUpdate();
    //                 stmtUpdateTransaction.executeUpdate();
    //             }

    //             // 3. Aktualisiere die Email in der group12user-Tabelle
    //             try (PreparedStatement stmtUpdateUserEmail = connection.prepareStatement(
    //                     "UPDATE group12user SET email = ? WHERE email = ?")) {

    //                 stmtUpdateUserEmail.setString(1, newEmail);
    //                 stmtUpdateUserEmail.setString(2, currentEmail);
    //                 stmtUpdateUserEmail.executeUpdate();
    //             }
    //         }

    //         // Alle Updates erfolgreich, commit
    //         connection.commit();
    //         return true;

    //     } catch (SQLException e) {
    //         Logger.getLogger("EditUser").log(Level.SEVERE, "Error editing user", e);
    //         try {
    //             if (connection != null) {
    //                 connection.rollback(); // Rollback bei Fehler
    //             }
    //         } catch (SQLException rollbackEx) {
    //             Logger.getLogger("EditUser").log(Level.SEVERE, "Error during rollback", rollbackEx);
    //         }
    //         return false;
    //     }
    // }

    public void editUser(String currentEmail, User user){
        Logger.getLogger("EditUserLogger").log(Level.WARNING, "Start editUser-method");

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
                   "budget = " + user.getBudget() + oldBudget;
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
                String newUserSQL = "INSERT into group12user (email, firstname, lastname, password, budget) VALUES (" +
                    "'" + user.getEmail() + "', " +
                    "'" + user.getFirstName() + "', " +
                    "'" + user.getLastName() + "', " +
                    "'" + passwordManager.hashPassword(user.getPassword()) + "', " +
                    "'" + user.getBudget() + oldBudget + "')";
                String updateSessionsSQL = "UPDATE group12session SET email = '" + user.getEmail() + "' WHERE email = '" + currentEmail + "'";
                String updateTransactionsSQL = "UPDATE group12transaction SET email = '" + user.getEmail() + "' WHERE email = '" + currentEmail + "'";
                String updatePortfolioSQL = "UPDATE group12portfolio SET email = '" + user.getEmail() + "' WHERE email = '" + currentEmail + "'";
                String deleteOldUserSQL = "DELETE FROM group12user WHERE email= '" + currentEmail + "'";

                stmt.executeUpdate(newUserSQL);
                stmt.executeUpdate(updateSessionsSQL);
                stmt.executeUpdate(updateTransactionsSQL);
                stmt.executeUpdate(updatePortfolioSQL);
                stmt.executeUpdate(deleteOldUserSQL);
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

    //@Override
    public boolean deleteUser(int userID) {
        boolean deleted = true;
        // sql strings to delete the user and the connections, if he uses carpools (foreign key)
        // String deleteUser_database_query = "DELETE FROM Users" +
        //         "       WHERE UserID = " + userID + ";";
        //Delete from database
        return deleted;
    }

}
