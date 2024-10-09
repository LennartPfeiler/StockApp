package mosbach.dhbw.de.stockwizzard.dataManagerImplementation;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.*;

import mosbach.dhbw.de.stockwizzard.dataManager.IPortfolioManager;
import mosbach.dhbw.de.stockwizzard.model.Portfolio;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PortfolioManagerImplementation implements IPortfolioManager{

    String databaseConnectionnUrl = "postgresql://mhartwig:BE1yEbCLMjy7r2ozFRGHZaE6jHZUx0fFadiuqgW7TtVs1k15XZVwPSBkPLZVTle6@b8b0e4b9-8325-4a3f-be73-74f20266cd1a.postgresql.eu01.onstackit.cloud:5432/stackit";
    URI dbUri;
    String username = "";
    String password = "";
    String dbUrl = "";

    static PortfolioManagerImplementation databaseUser = null;

    private PortfolioManagerImplementation(){
        try {
            dbUri = new URI(databaseConnectionnUrl);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        username = dbUri.getUserInfo().split(":")[0];
        password = dbUri.getUserInfo().split(":")[1];
        dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath() + "?sslmode=require";
    }

    static public PortfolioManagerImplementation getPortfolioManager() {
        if (databaseUser == null)
            databaseUser = new PortfolioManagerImplementation();
        return databaseUser;
    }

    public void createPortfolioTable() {
        Statement stmt = null;
        Connection connection = null;

        try {
            connection = DriverManager.getConnection(dbUrl, username, password);
            stmt = connection.createStatement();
            String dropTable = "DROP TABLE IF EXISTS group12portfolio";
            stmt.executeUpdate(dropTable);

            String createTable = "CREATE TABLE group12portfolio (" +
                     "portfolioid SERIAL PRIMARY KEY, " +
                     "value DOUBLE PRECISION NOT NULL, " +
                     "email VARCHAR(100) NOT NULL, " +
                     "FOREIGN KEY (email) REFERENCES group12user(email) ON DELETE CASCADE)";


            stmt.executeUpdate(createTable);
        } catch (Exception e) {
            Logger.getLogger("CreatePortfolioTableLogger").log(Level.INFO, "Portfolio table cannot be created. Error: {0}", e);
        } finally {
            try {
                // Schließen von Statement und Connection, um Ressourcen freizugeben
                if (stmt != null) stmt.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                // Fehler beim Schließen protokollieren
                Logger.getLogger("CreatePortfolioTableLogger").log(Level.SEVERE, "Error beim Schließen der Ressourcen. Error: {0}", e);
            }
        }
    }
    
    public void addPortfolio(Portfolio portfolioData) {
        Statement stmt = null;
        Connection connection = null;
        Logger.getLogger("SetNewPortfolioWriter").log(Level.INFO, "Start addPortfolio-method");
        try {
            // Stelle die Verbindung zur Datenbank her
            connection = DriverManager.getConnection(dbUrl, username, password);
            stmt = connection.createStatement();
    
            // SQL-Anweisung für das Einfügen eines neuen Portfolios
            String insertSQL = "INSERT INTO group12portfolio (value, email) VALUES (" +
                    portfolioData.getValue() + ", " +
                    "'" + portfolioData.getEmail() + "')";
    
            // Führe die SQL-Anweisung aus
            stmt.executeUpdate(insertSQL);
            
            Logger.getLogger("SetNewPortfolioWriter").log(Level.INFO, "Portfolio erfolgreich hinzugefügt: Value = {0}, Email = {1}",
                    new Object[]{portfolioData.getValue(), portfolioData.getEmail()});
            
            // Schließe das Statement und die Verbindung
        } catch (SQLException e) {
            Logger.getLogger("SetNewPortfolioWriter").log(Level.SEVERE, "Fehler beim Hinzufügen des Portfolios.", e);
        } finally {
            try {
                // Schließen von Statement und Connection, um Ressourcen freizugeben
                if (stmt != null) stmt.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                // Fehler beim Schließen protokollieren
                Logger.getLogger("SetNewPortfolioWriter").log(Level.SEVERE, "Error beim Schließen der Ressourcen. Error: {0}", e);
            }
        }
    }

    public Portfolio getUserPortfolio(String email) {
        Statement stmt = null;
        Connection connection = null;
        Portfolio portfolio = null;
        
        try {
            // Stelle die Verbindung zur Datenbank her
            connection = DriverManager.getConnection(dbUrl, username, password);
            stmt = connection.createStatement();

            // SQL-Query für das Abrufen des Portfolios basierend auf der E-Mail
            String selectSQL = "SELECT * FROM group12portfolio WHERE email = '" + email + "'";
            
            // Führe die SQL-Query aus und speichere das Ergebnis in einem ResultSet
            ResultSet rs = stmt.executeQuery(selectSQL);

            // Überprüfe, ob ein Portfolio gefunden wurde
            if (rs.next()) {
                // Extrahiere die Werte aus dem ResultSet
                int portfolioID = rs.getInt("portfolioid");
                double value = rs.getDouble("value");
                String userEmail = rs.getString("email");

                // Erstelle ein neues Portfolio-Objekt
                portfolio = new Portfolio(portfolioID, value, userEmail);
                Logger.getLogger("GetPortfolioReader").log(Level.INFO, "Portfolio gefunden: {0}", portfolio);
            } else {
                Logger.getLogger("GetPortfolioReader").log(Level.INFO, "Kein Portfolio für die E-Mail {0} gefunden.", email);
            }

            // Schließe das ResultSet, das Statement und die Verbindung
            rs.close();
        } catch (SQLException e) {
            Logger.getLogger("GetPortfolioReader").log(Level.SEVERE, "Fehler beim Abrufen des Portfolios aus der Datenbank.", e);
        } finally {
            try {
                // Schließen von Statement und Connection, um Ressourcen freizugeben
                if (stmt != null) stmt.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                // Fehler beim Schließen protokollieren
                Logger.getLogger("GetPortfolioReader").log(Level.SEVERE, "Error beim Schließen der Ressourcen. Error: {0}", e);
            }
        }
        return portfolio;
    }
}