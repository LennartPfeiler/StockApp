package dhbw.mosbach.de.stockwizzard.service.dataManagerImplementation;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.UUID;
import dhbw.mosbach.de.stockwizzard.service.dataManager.ISessionManager;
import dhbw.mosbach.de.stockwizzard.model.Portfolio;
import dhbw.mosbach.de.stockwizzard.model.Session;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.*;

public class SessionManagerImplementation implements ISessionManager {

    private String databaseConnectionnUrl = "postgresql://mhartwig:BE1yEbCLMjy7r2ozFRGHZaE6jHZUx0fFadiuqgW7TtVs1k15XZVwPSBkPLZVTle6@b8b0e4b9-8325-4a3f-be73-74f20266cd1a.postgresql.eu01.onstackit.cloud:5432/stackit";
    private URI dbUri;
    private String username = "";
    private String password = "";
    private String dbUrl = "";

    private static SessionManagerImplementation databaseUser = null;

    public SessionManagerImplementation() {
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

    // Create Session database table
    public void createSessionTable() {
        Statement stmt = null;
        Connection connection = null;

        try {
            connection = DriverManager.getConnection(dbUrl, username, password);
            stmt = connection.createStatement();
            String dropSessionTableSQL = "DROP TABLE IF EXISTS group12session";
            stmt.executeUpdate(dropSessionTableSQL);

            String createSessionTableSQL = "CREATE TABLE group12session (" +
                    "token VARCHAR(100) NOT NULL PRIMARY KEY, " +
                    "email VARCHAR(100) NOT NULL, " +
                    "FOREIGN KEY (email) REFERENCES group12user(email) ON DELETE CASCADE)";

            stmt.executeUpdate(createSessionTableSQL);
        } catch (Exception e) {
            Logger.getLogger("CreateSessionTableLogger").log(Level.SEVERE,
                    "Session table cannot be created. Error: {0}", e);
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null)
                    stmt.close();
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                Logger.getLogger("CreateSessionTableLogger").log(Level.SEVERE,
                        "Error when closing the resource. Error: {0}", e);
                e.printStackTrace();
            }
        }
    }

    // Create a new user session after a successful login
    public void createSession(String email, String token) {
        Statement stmt = null;
        Connection connection = null;
        Logger.getLogger("SetNewSessionWriter").log(Level.INFO, "Start createSession-method");
        try {
            connection = DriverManager.getConnection(dbUrl, username, password);
            stmt = connection.createStatement();

            String insertSessionSQL = "INSERT into group12session (token, email) VALUES (" +
                    "'" + token + "', " +
                    "'" + email + "')";

            stmt.executeUpdate(insertSessionSQL);
        } catch (SQLException e) {
            Logger.getLogger("SetNewSessionWriter").log(Level.SEVERE, "Error when creating a session.", e);
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null)
                    stmt.close();
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                Logger.getLogger("SetNewSessionWriter").log(Level.SEVERE, "Error when closing the resource. Error: {0}",
                        e);
                e.printStackTrace();
            }
        }
    }

    // Get a existing session by email
    public Session getSession(String email) {
        Statement stmt = null;
        Connection connection = null;
        Session session = null;
        Logger.getLogger("GetSessionByEmail").log(Level.INFO, "Start getSession-method");

        try {
            connection = DriverManager.getConnection(dbUrl, username, password);
            stmt = connection.createStatement();

            String selectSessionSQL = "SELECT * FROM group12session WHERE email = '" + email + "'";

            ResultSet rs = stmt.executeQuery(selectSessionSQL);
            if (rs.next()) {
                session = new Session();
                session.setToken(rs.getString("token"));
                session.setEmail(rs.getString("email"));
            }
            rs.close();
        } catch (SQLException e) {
            Logger.getLogger("GetSessionByEmail").log(Level.SEVERE, "Error when getting a session.", e);
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null)
                    stmt.close();
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                Logger.getLogger("GetSessionByEmail").log(Level.SEVERE, "Error when closing the resource. Error: {0}",
                        e);
                e.printStackTrace();
            }
        }
        return session;
    }

    // Delete a session after a successful logout
    public void deleteSession(String email, String token) {
        Statement stmt = null;
        Connection connection = null;
        Logger.getLogger("DeleteSessionWriter").log(Level.INFO, "Start deleteSession-method");
        try {
            connection = DriverManager.getConnection(dbUrl, username, password);
            stmt = connection.createStatement();

            String deleteSessionSQL = "DELETE FROM group12session WHERE email = '" + email + "' AND token = '" + token
                    + "'";

            stmt.executeUpdate(deleteSessionSQL);
        } catch (SQLException e) {
            Logger.getLogger("DeleteSessionWriter").log(Level.SEVERE, "Error when deleting a session.", e);
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null)
                    stmt.close();
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                Logger.getLogger("DeleteSessionWriter").log(Level.SEVERE, "Error when closing the resource. Error: {0}",
                        e);
                e.printStackTrace();
            }
        }
    }

    // Check if email and token are valid
    public Boolean validToken(String token, String email) {
        Statement stmt = null;
        Connection connection = null;
        Logger.getLogger("ValidTokenLogger").log(Level.INFO, "Start validToken-method");
        try {
            connection = DriverManager.getConnection(dbUrl, username, password);
            stmt = connection.createStatement();

            String selectValidSessionSQL = "SELECT * FROM group12session WHERE token = '" + token + "'";

            ResultSet rs = stmt.executeQuery(selectValidSessionSQL);
            if (rs.next()) {
                String currentEmail = rs.getString("email");
                rs.close();
                return currentEmail.equals(email);
            } else {
                rs.close();
                return false;
            }
        } catch (SQLException e) {
            Logger.getLogger("ValidTokenLogger").log(Level.SEVERE, "Error when getting session data.", e);
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (stmt != null)
                    stmt.close();
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                Logger.getLogger("ValidTokenLogger").log(Level.SEVERE, "Error when closing the resource. Error: {0}",
                        e);
                e.printStackTrace();
            }
        }
    }

    // Edit session when the user changed email
    public void editSession(String email, String newEmail) {
        Statement stmt = null;
        Connection connection = null;
        Logger.getLogger("UpdateSessionLogger").log(Level.INFO, "Start editSession method");
        try {
            connection = DriverManager.getConnection(dbUrl, username, password);
            stmt = connection.createStatement();

            String updateSessionsSQL = "UPDATE group12session SET email = '" + newEmail + "' WHERE email = '" + email
                    + "'";

            stmt.executeUpdate(updateSessionsSQL);
        } catch (SQLException e) {
            Logger.getLogger("UpdateSessionLogger").log(Level.SEVERE, "Error when updating a session.", e);
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null)
                    stmt.close();
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                Logger.getLogger("UpdateSessionLogger").log(Level.SEVERE, "Error when closing the resource.", e);
                e.printStackTrace();
            }
        }
    }
}