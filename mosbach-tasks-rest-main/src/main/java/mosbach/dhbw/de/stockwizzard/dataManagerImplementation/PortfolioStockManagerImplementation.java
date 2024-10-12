package mosbach.dhbw.de.stockwizzard.dataManagerImplementation;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import mosbach.dhbw.de.stockwizzard.dataManager.IPortfolioStockManager;
import mosbach.dhbw.de.stockwizzard.model.Portfolio;
import mosbach.dhbw.de.stockwizzard.model.PortfolioStock;

public class PortfolioStockManagerImplementation implements IPortfolioStockManager{

    String databaseConnectionnUrl = "postgresql://mhartwig:BE1yEbCLMjy7r2ozFRGHZaE6jHZUx0fFadiuqgW7TtVs1k15XZVwPSBkPLZVTle6@b8b0e4b9-8325-4a3f-be73-74f20266cd1a.postgresql.eu01.onstackit.cloud:5432/stackit";
    URI dbUri;
    String username = "";
    String password = "";
    String dbUrl = "";

    static PortfolioStockManagerImplementation databaseUser = null;

    private PortfolioStockManagerImplementation(){
        try {
            dbUri = new URI(databaseConnectionnUrl);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        username = dbUri.getUserInfo().split(":")[0];
        password = dbUri.getUserInfo().split(":")[1];
        dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath() + "?sslmode=require";
    }

    static public PortfolioStockManagerImplementation getPortfolioStockManager() {
        if (databaseUser == null)
            databaseUser = new PortfolioStockManagerImplementation();
        return databaseUser;
    }

    public void createPortfolioStockTable() {

        Statement stmt = null;
        Connection connection = null;

        try {
            connection = DriverManager.getConnection(dbUrl, username, password);
            stmt = connection.createStatement();
            String dropTable = "DROP TABLE IF EXISTS group12portfolioStock";
            stmt.executeUpdate(dropTable);

            String createTable = "CREATE TABLE group12portfolioStock (" +
                     "portfolioid SERIAL NOT NULL, " +
                     "symbol VARCHAR(10) NOT NULL, " +
                     "stockamount DOUBLE PRECISION NOT NULL, " +
                     "boughtvalue DOUBLE PRECISION NOT NULL, " +
                     "currentvalue DOUBLE PRECISION NOT NULL, " +
                     "PRIMARY KEY (portfolioid, symbol), " +
                     "FOREIGN KEY (portfolioid) REFERENCES group12portfolio(portfolioid) ON DELETE CASCADE, " +
                     "FOREIGN KEY (symbol) REFERENCES group12stock(symbol) ON DELETE CASCADE)";


            stmt.executeUpdate(createTable);
        } catch (Exception e) {
            Logger.getLogger("CreatePortfolioStockTableLogger").log(Level.INFO, "PortfolioStock table cannot be created. Error: {0}", e);
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

    public void addPortfolioStock(Integer portfolioId, String symbol, Double stockAmount, Double pricePerStock){
        Statement stmt = null;
    Connection connection = null;
    Logger.getLogger("UpdatePortfolioStockLogger").log(Level.INFO, "Start updatePortfolioStock-method");

    try {
        // Stelle die Verbindung zur Datenbank her
        connection = DriverManager.getConnection(dbUrl, username, password);
        stmt = connection.createStatement();

        // SQL-Abfrage zum Überprüfen, ob der Stock bereits im Portfolio vorhanden ist
        String checkSQL = "SELECT * FROM group12portfolioStock WHERE portfolioid = " + portfolioId + " AND symbol = '" + symbol + "'";
        ResultSet rs = stmt.executeQuery(checkSQL);

        if (rs.next()) {
            // Wenn der Stock bereits vorhanden ist, aktualisiere die Menge
            Double existingAmount = rs.getDouble("stockAmount");
            Double existingboughtValue = rs.getDouble("boughtValue");
            Double existingCurrentValue = rs.getDouble("currentValue");
            Double newAmount = existingAmount + stockAmount; // neue Menge berechnen
            Double newBoughtValue = existingboughtValue + stockAmount * pricePerStock;
            Double newCurrentValue = existingCurrentValue + stockAmount * pricePerStock;

            String updateSQL = "UPDATE group12portfolioStock SET " +
                   "stockAmount = " + newAmount + ", " +
                   "boughtValue = " + newBoughtValue + ", " +
                   "currentValue = " + newCurrentValue + " " + // Leerzeichen vor dem WHERE
                   "WHERE portfolioid = " + portfolioId + " AND symbol = '" + symbol + "'";
            stmt.executeUpdate(updateSQL);
            Logger.getLogger("UpdatePortfolioStockLogger").log(Level.INFO, "Stock updated successfully.");

        } else {
            // Wenn der Stock nicht vorhanden ist, füge ihn hinzu
            Double value = stockAmount * pricePerStock; // Berechnung des Wertes

            // Wenn der Stock nicht vorhanden ist, füge ihn hinzu
            String insertSQL = "INSERT INTO group12portfolioStock (portfolioid, symbol, stockAmount, boughtValue, currentValue) VALUES (" +
                            portfolioId + ", '" + // Portfolio-ID
                            symbol + "', " + // Symbol
                            stockAmount + ", " + // Stock amount
                            value + ", " + // Bought value
                            value + ")"; // Current value
            stmt.executeUpdate(insertSQL);
            Logger.getLogger("UpdatePortfolioStockLogger").log(Level.INFO, "Stock added successfully.");

        }

        // Schließe ResultSet
        rs.close();
        } catch (SQLException e) {
            Logger.getLogger("UpdatePortfolioStockLogger").log(Level.SEVERE, "Fehler beim Aktualisieren des Portfolio-Stock.", e);
        } finally {
            try {
                // Schließen von Statement und Connection, um Ressourcen freizugeben
                if (stmt != null) stmt.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                Logger.getLogger("UpdatePortfolioStockLogger").log(Level.SEVERE, "Error beim Schließen der Ressourcen. Error: {0}", e);
            }
        }
    }

    public void editPortfolioStock(Integer portfolioId, String symbol, Integer transactionType){

    }

    public void deletePortfolioStock(Integer portfolioId, String symbol){

    }
}