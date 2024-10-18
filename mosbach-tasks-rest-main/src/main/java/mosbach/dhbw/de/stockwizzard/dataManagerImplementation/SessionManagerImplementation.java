package mosbach.dhbw.de.stockwizzard.dataManagerImplementation;

import java.io.*;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.UUID;
import mosbach.dhbw.de.stockwizzard.dataManager.ISessionManager;
import mosbach.dhbw.de.stockwizzard.model.Portfolio;
import mosbach.dhbw.de.stockwizzard.model.Session;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.*;

public class SessionManagerImplementation implements ISessionManager{

    String databaseConnectionnUrl = "postgresql://mhartwig:BE1yEbCLMjy7r2ozFRGHZaE6jHZUx0fFadiuqgW7TtVs1k15XZVwPSBkPLZVTle6@b8b0e4b9-8325-4a3f-be73-74f20266cd1a.postgresql.eu01.onstackit.cloud:5432/stackit";
    URI dbUri;
    String username = "";
    String password = "";
    String dbUrl = "";

    static SessionManagerImplementation databaseUser = null;

    private SessionManagerImplementation(){
        try {
            dbUri = new URI(databaseConnectionnUrl);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        username = dbUri.getUserInfo().split(":")[0];
        password = dbUri.getUserInfo().split(":")[1];
        dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath() + "?sslmode=require";
    }

    static public SessionManagerImplementation getSessionManager() {
        if (databaseUser == null)
            databaseUser = new SessionManagerImplementation();
        return databaseUser;
    }

    public void createSessionTable() {
        Statement stmt = null;
        Connection connection = null;

        try {
            connection = DriverManager.getConnection(dbUrl, username, password);
            stmt = connection.createStatement();
            String dropTable = "DROP TABLE IF EXISTS group12session";
            stmt.executeUpdate(dropTable);

            String createTable = "CREATE TABLE group12session (" +
                     "token VARCHAR(100) NOT NULL PRIMARY KEY, " +
                     "email VARCHAR(100) NOT NULL, " +
                     "FOREIGN KEY (email) REFERENCES group12user(email) ON DELETE CASCADE)";

            stmt.executeUpdate(createTable);
        } catch (Exception e) {
            Logger.getLogger("CreateSessionTableLogger").log(Level.INFO, "Session table cannot be created. Error: {0}", e);
        } finally {
            try {
                // Schließen von Statement und Connection, um Ressourcen freizugeben
                if (stmt != null) stmt.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                // Fehler beim Schließen protokollieren
                Logger.getLogger("CreateSessionTableLogger").log(Level.SEVERE, "Error beim Schließen der Ressourcen. Error: {0}", e);
            }
        }
    }

    public void createSession(String email, String token){
        Statement stmt = null;
        Connection connection = null;
        Logger.getLogger("SetNewSessionWriter").log(Level.INFO, "Start createSession-method");
        try {
            connection = DriverManager.getConnection(dbUrl, username, password);
            stmt = connection.createStatement();
            String udapteSQL = "INSERT into group12session (token, email) VALUES (" +
                    "'" + token + "', " +
                    "'" + email + "')";

            stmt.executeUpdate(udapteSQL);     
        } catch (SQLException e) {
            Logger.getLogger("SetNewSessionWriter").log(Level.SEVERE, "Fehler beim Hinzufügen der Sessiondaten in die Datenbank.", e);
        } finally {
            try {
                // Schließen von Statement und Connection, um Ressourcen freizugeben
                if (stmt != null) stmt.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                // Fehler beim Schließen protokollieren
                Logger.getLogger("SetNewSessionWriter").log(Level.SEVERE, "Error beim Schließen der Ressourcen. Error: {0}", e);
            }
        }
    }
    
    public Session getSession(String email) {
        Statement stmt = null;
        Connection connection = null;
        Session session = null;
        Logger.getLogger("GetSessionByEmail").log(Level.INFO, "Start getSession-method");
        
        try {
            // Verbindung zur Datenbank herstellen
            connection = DriverManager.getConnection(dbUrl, username, password);
            stmt = connection.createStatement();

            // SQL-Abfrage definieren
            String selectSQL = "SELECT * FROM group12session WHERE email = '" + email + "'";

            // Ausführen der SELECT-Abfrage
            ResultSet rs = stmt.executeQuery(selectSQL);

            // Prüfen, ob ein Ergebnis zurückgegeben wurde
            if (rs.next()) {
                // Benutzerobjekt basierend auf den Ergebnissen erstellen
                session = new Session();
                session.setToken(rs.getString("token"));
                session.setEmail(rs.getString("email"));
            }
            rs.close();
        } catch (SQLException e) {
            Logger.getLogger("GetSessionByEmail").log(Level.SEVERE, "Fehler beim Abrufen der Sessiondaten aus der Datenbank.", e);
        } finally {
            try {
                // Schließen von Statement und Connection, um Ressourcen freizugeben
                if (stmt != null) stmt.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                // Fehler beim Schließen protokollieren
                Logger.getLogger("GetSessionByEmail").log(Level.SEVERE, "Error beim Schließen der Ressourcen. Error: {0}", e);
            }
        }
        return session;
    }

    public void deleteSession(String email, String token){
        Statement stmt = null;
        Connection connection = null;
        Logger.getLogger("DeleteSessionWriter").log(Level.INFO, "Start deleteSession-method");
        try {
            connection = DriverManager.getConnection(dbUrl, username, password);
            stmt = connection.createStatement();
            String deleteSQL = "DELETE FROM group12session WHERE email = '" + email + "' AND token = '" + token + "'";
            stmt.executeUpdate(deleteSQL);     
        } catch (SQLException e) {
            Logger.getLogger("DeleteSessionWriter").log(Level.SEVERE, "Fehler beim Löschen der Session aus der Datenbank.", e);
        } finally {
            try {
                // Schließen von Statement und Connection, um Ressourcen freizugeben
                if (stmt != null) stmt.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                // Fehler beim Schließen protokollieren
                Logger.getLogger("DeleteSessionWriter").log(Level.SEVERE, "Error beim Schließen der Ressourcen. Error: {0}", e);
            }
        }
    }

    public boolean validToken(String token, String email){
        Statement stmt = null;
        Connection connection = null;
        Logger.getLogger("ValidTokenLogger").log(Level.INFO, "Start validToken-method");

        try {
            // Verbindung zur Datenbank herstellen
            connection = DriverManager.getConnection(dbUrl, username, password);
            stmt = connection.createStatement();

            // SQL-Abfrage definieren
            String selectSQL = "SELECT * FROM group12session WHERE token = '" + token + "'";

            // Ausführen der SELECT-Abfrage
            ResultSet rs = stmt.executeQuery(selectSQL);

            // Prüfen, ob ein Ergebnis zurückgegeben wurde
            if (rs.next()) {
                // Benutzerobjekt basierend auf den Ergebnissen erstellen
                String currentEmail = rs.getString("email");
                rs.close();
                return currentEmail.equals(email);
            } else {
                rs.close();
                return false; // Token nicht gefunden
            }
            
        } catch (SQLException e) {
            Logger.getLogger("ValidTokenLogger").log(Level.SEVERE, "Fehler beim Abrufen der Sessiondaten aus der Datenbank.", e);
            return false;
        } finally {
            try {
                // Schließen von Statement und Connection, um Ressourcen freizugeben
                if (stmt != null) stmt.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                // Fehler beim Schließen protokollieren
                Logger.getLogger("ValidTokenLogger").log(Level.SEVERE, "Error beim Schließen der Ressourcen. Error: {0}", e);
            }
        }
    }

    public void editSession(String email, String newEmail){
        Statement stmt = null;
        Connection connection = null;
        Logger.getLogger("UpdateSessionLogger").log(Level.INFO, "Start editSession method");

        try {
            // Stelle die Verbindung zur Datenbank her
            connection = DriverManager.getConnection(dbUrl, username, password);
            stmt = connection.createStatement();

            // SQL-Anweisung für das Aktualisieren des Portfolio-Wertes
            String updateSessionsSQL = "UPDATE group12session SET email = '" + newEmail + "' WHERE email = '" + email + "'";
            stmt.executeUpdate(updateSessionsSQL);
        } catch (SQLException e) {
            Logger.getLogger("UpdateSessionLogger").log(Level.SEVERE, "Error updating session.", e);
        } finally {
            try {
                // Schließen von Statement und Connection, um Ressourcen freizugeben
                if (stmt != null) stmt.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                // Fehler beim Schließen protokollieren
                Logger.getLogger("UpdateSessionLogger").log(Level.SEVERE, "Error closing resources.", e);
            }
        }
    }
}