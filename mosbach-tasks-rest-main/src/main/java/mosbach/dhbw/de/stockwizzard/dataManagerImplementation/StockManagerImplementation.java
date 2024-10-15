package mosbach.dhbw.de.stockwizzard.dataManagerImplementation;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.*;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.UUID;
import mosbach.dhbw.de.stockwizzard.dataManager.IStockManager;
import mosbach.dhbw.de.stockwizzard.model.Stock;
import mosbach.dhbw.de.stockwizzard.model.User;

public class StockManagerImplementation implements IStockManager{

    String databaseConnectionnUrl = "postgresql://mhartwig:BE1yEbCLMjy7r2ozFRGHZaE6jHZUx0fFadiuqgW7TtVs1k15XZVwPSBkPLZVTle6@b8b0e4b9-8325-4a3f-be73-74f20266cd1a.postgresql.eu01.onstackit.cloud:5432/stackit";
    URI dbUri;
    String username = "";
    String password = "";
    String dbUrl = "";

    static StockManagerImplementation databaseUser = null;

    private StockManagerImplementation(){
        try {
            dbUri = new URI(databaseConnectionnUrl);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        username = dbUri.getUserInfo().split(":")[0];
        password = dbUri.getUserInfo().split(":")[1];
        dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath() + "?sslmode=require";
    }

    static public StockManagerImplementation getStockManager() {
        if (databaseUser == null)
            databaseUser = new StockManagerImplementation();
        return databaseUser;
    }

    public void createStockTable() {
        Statement stmt = null;
        Connection connection = null;

        try {
            connection = DriverManager.getConnection(dbUrl, username, password);
            stmt = connection.createStatement();
            String dropTable = "DROP TABLE IF EXISTS group12stock";
            stmt.executeUpdate(dropTable);

            String createTable = "CREATE TABLE group12stock (" +
                     "symbol VARCHAR(100) PRIMARY KEY, " +
                     "stockPrice DOUBLE PRECISION NOT NULL, " +
                     "name VARCHAR(100) NOT NULL)";


            stmt.executeUpdate(createTable);
        } catch (Exception e) {
            Logger.getLogger("CreateStockTableLogger").log(Level.INFO, "Stock table cannot be created. Error: {0}", e);
        } finally {
            try {
                // Schließen von Statement und Connection, um Ressourcen freizugeben
                if (stmt != null) stmt.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                // Fehler beim Schließen protokollieren
                Logger.getLogger("CreateStockTableLogger").log(Level.SEVERE, "Error beim Schließen der Ressourcen. Error: {0}", e);
            }
        }
    }

    public Stock getStock(String symbol) {
        Statement stmt = null;
        Connection connection = null;
        Stock stock = null;
        Logger.getLogger("GetStockLogger").log(Level.INFO, "Start getStock-method");
        
        try {
            // Verbindung zur Datenbank herstellen
            connection = DriverManager.getConnection(dbUrl, username, password);
            stmt = connection.createStatement();

            // SQL-Abfrage definieren
            String selectSQL = "SELECT * FROM group12stock WHERE symbol = '" + symbol + "'";

            // Ausführen der SELECT-Abfrage
            ResultSet rs = stmt.executeQuery(selectSQL);

            // Prüfen, ob ein Ergebnis zurückgegeben wurde
            if (rs.next()) {
                // Benutzerobjekt basierend auf den Ergebnissen erstellen
                stock = new Stock();
                stock.setSymbol(rs.getString("symbol"));
                stock.setName(rs.getString("name"));
                stock.setStockPrice(Double.parseDouble(rs.getString("stockprice")));
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
                Logger.getLogger("GetStockLogger").log(Level.SEVERE, "Error beim Schließen der Ressourcen. Error: {0}", e);
            }
        }
        return stock;
    }

    public void addStock(Stock stock) {
        Statement stmt = null;
        Connection connection = null;
        Logger.getLogger("SetNewStockWriter").log(Level.INFO, "Start addStock-method");
        Logger.getLogger("SetNewStockWriter").log(Level.INFO, "{0}", stock);
        Logger.getLogger("SetNewStockWriter").log(Level.INFO, "{0}", stock.getStockPrice());
        try {
            connection = DriverManager.getConnection(dbUrl, username, password);
            stmt = connection.createStatement();
            String insertSQL = "INSERT INTO group12stock (symbol, stockprice, name) VALUES (" +
                   "'" + stock.getSymbol() + "', " +
                   stock.getStockPrice() + ", " +
                   "'" + stock.getName() + "')";


            stmt.executeUpdate(insertSQL);     
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                // Schließen von Statement und Connection, um Ressourcen freizugeben
                if (stmt != null) stmt.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                // Fehler beim Schließen protokollieren
                Logger.getLogger("SetNewStockWriter").log(Level.SEVERE, "Error beim Schließen der Ressourcen. Error: {0}", e);
            }
        }
    }
}