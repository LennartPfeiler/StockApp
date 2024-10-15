package mosbach.dhbw.de.stockwizzard.dataManagerImplementation;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
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
        Logger.getLogger("AddPortfolioStockLogger").log(Level.INFO, "Start addPortfolioStock-method");

        try {
            // Stelle die Verbindung zur Datenbank her
            connection = DriverManager.getConnection(dbUrl, username, password);
            stmt = connection.createStatement();

            // SQL-Abfrage zum Überprüfen, ob der Stock bereits im Portfolio vorhanden ist
            String checkSQL = "SELECT * FROM group12portfolioStock WHERE portfolioid = " + portfolioId + " AND symbol = '" + symbol + "'";
            ResultSet rs = stmt.executeQuery(checkSQL);

            if (rs.next()) {
                // Wenn der Stock bereits vorhanden ist, aktualisiere die Menge
                Double existingAmount = rs.getDouble("stockamount");
                Double existingboughtValue = rs.getDouble("boughtvalue");
                Double existingCurrentValue = rs.getDouble("currentvalue");
                Double newAmount = existingAmount + stockAmount; // neue Menge berechnen
                Double newBoughtValue = existingboughtValue + stockAmount * pricePerStock;
                Double newCurrentValue = existingCurrentValue + stockAmount * pricePerStock;

                String updateSQL = "UPDATE group12portfolioStock SET " +
                    "stockamount = " + newAmount + ", " +
                    "boughtvalue = " + newBoughtValue + ", " +
                    "currentvalue = " + newCurrentValue + " " + // Leerzeichen vor dem WHERE
                    "WHERE portfolioid = " + portfolioId + " AND symbol = '" + symbol + "'";
                stmt.executeUpdate(updateSQL);
                Logger.getLogger("AddPortfolioStockLogger").log(Level.INFO, "Stock updated successfully.");

            } else {
                // Wenn der Stock nicht vorhanden ist, füge ihn hinzu
                Double value = stockAmount * pricePerStock; // Berechnung des Wertes

                // Wenn der Stock nicht vorhanden ist, füge ihn hinzu
                String insertSQL = "INSERT INTO group12portfolioStock (portfolioid, symbol, stockamount, boughtvalue, currentvalue) VALUES (" +
                                portfolioId + ", '" + // Portfolio-ID
                                symbol + "', " + // Symbol
                                stockAmount + ", " + // Stock amount
                                value + ", " + // Bought value
                                value + ")"; // Current value
                stmt.executeUpdate(insertSQL);
                Logger.getLogger("AddPortfolioStockLogger").log(Level.INFO, "Stock added successfully.");
            }

            // Schließe ResultSet
            rs.close();
        } catch (SQLException e) {
            Logger.getLogger("AddPortfolioStockLogger").log(Level.SEVERE, "Fehler beim Aktualisieren des Portfolio-Stock.", e);
        } finally {
            try {
                // Schließen von Statement und Connection, um Ressourcen freizugeben
                if (stmt != null) stmt.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                Logger.getLogger("AddPortfolioStockLogger").log(Level.SEVERE, "Error beim Schließen der Ressourcen. Error: {0}", e);
            }
        }
    }

    public List<PortfolioStock> getAllPortfolioStocks(String email, String sortby){
        List<PortfolioStock> portfolioStocks = new ArrayList<>();
        Statement stmt = null;
        Connection connection = null;

        try {
            connection = DriverManager.getConnection(dbUrl, username, password);
            stmt = connection.createStatement();

            String getPortfolioStocks = "SELECT * FROM group12portfoliostock WHERE portfolioid =" + 
            "(SELECT portfolioid FROM group12portfolio WHERE email = '" + email + "') ORDER BY " + sortby + " ASC";


            ResultSet rs = stmt.executeQuery(getPortfolioStocks);
            while (rs.next()) {
                portfolioStocks.add(
                        new PortfolioStock(
                                Integer.parseInt(rs.getString("portfolioid")),
                                rs.getString("symbol"),
                                Double.parseDouble(rs.getString("stockamount")),
                                Double.parseDouble(rs.getString("boughtvalue")),
                                Double.parseDouble(rs.getString("currentvalue"))
                                )
                );
            }
            rs.close();
        } catch (Exception e) {
            Logger.getLogger("GetPortfolioStocksLogger").log(Level.INFO, "Can't get all portfolioStocks. Error: {0}", e);
        } finally {
            try {
                // Schließen von Statement und Connection, um Ressourcen freizugeben
                if (stmt != null) stmt.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                // Fehler beim Schließen protokollieren
                Logger.getLogger("GetPortfolioStocksLogger").log(Level.SEVERE, "Error beim Schließen der Ressourcen. Error: {0}", e);
            }
        }
        return portfolioStocks;
    }


    public void deletePortfolioStock(Integer portfolioId, String symbol, Double stockAmount, Double pricePerStock){
        Statement stmt = null;
        Connection connection = null;
        Logger.getLogger("DeletePortfolioStockLogger").log(Level.INFO, "Start deletePortfolioStock-method");

        try {
            // Stelle die Verbindung zur Datenbank her
            connection = DriverManager.getConnection(dbUrl, username, password);
            stmt = connection.createStatement();

            // SQL-Abfrage zum Überprüfen, ob der Stock bereits im Portfolio vorhanden ist
            String checkSQL = "SELECT * FROM group12portfolioStock WHERE portfolioid = " + portfolioId + " AND symbol = '" + symbol + "'";
            ResultSet rs = stmt.executeQuery(checkSQL);

            if (rs.next()) {
                // Wenn der Stock bereits vorhanden ist, aktualisiere die Menge
               //TODO
                Logger.getLogger("DeletePortfolioStockLogger").log(Level.INFO, "Stock updated successfully.");

            } else {
                // Wenn der Stock nicht vorhanden ist, füge ihn hinzu
                //TODO
                Logger.getLogger("DeletePortfolioStockLogger").log(Level.INFO, "Stock deleted successfully.");
            }

            // Schließe ResultSet
            rs.close();
        } catch (SQLException e) {
            Logger.getLogger("DeletePortfolioStockLogger").log(Level.SEVERE, "Fehler beim Aktualisieren des Portfolio-Stock.", e);
        } finally {
            try {
                // Schließen von Statement und Connection, um Ressourcen freizugeben
                if (stmt != null) stmt.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                Logger.getLogger("DeletePortfolioStockLogger").log(Level.SEVERE, "Error beim Schließen der Ressourcen. Error: {0}", e);
            }
        }
    }

    // public Boolean CheckIfPortfolioStockAmountIsSufficient(Double sellRequestAmount, String email, String symbol){   
    //     return true;
    //     Statement stmt = null;
    //     Connection connection = null;
    //     Logger.getLogger("CheckIfPortfolioStockAmountIsSufficientLogger").log(Level.INFO, "Start CheckIfPortfolioStockAmountIsSufficient-method");

    //     try {
    //         // Stelle die Verbindung zur Datenbank her
    //         connection = DriverManager.getConnection(dbUrl, username, password);
    //         stmt = connection.createStatement();

    //         // SQL-Abfrage zum Überprüfen, ob der Stock bereits im Portfolio vorhanden ist
    //         String checkSQL = "SELECT currentvalue FROM group12portfoliostock WHERE symbol = '"+ symbol + "' AND portfolioid = (SELECT portfolioid from group12portfolio WHERE email = '" + email + "')";
    //         ResultSet rs = stmt.executeQuery(checkSQL);

    //         if (rs.next()) {
    //             Logger.getLogger("CheckIfPortfolioStockAmountIsSufficientLogger").log(Level.INFO, "Stock is in Portfolio.");
    //             Double value = rs.getDouble("currentvalue");
    //             if(value >= sellRequestAmount){
    //                 return true;
    //             }
    //             else{
    //                 return false;
    //             }
    //         } else {
    //             // Wenn der Stock nicht vorhanden ist, return null
    //             return null;
    //             Logger.getLogger("CheckIfPortfolioStockAmountIsSufficientLogger").log(Level.INFO, "Stock deleted successfully.");
    //         }
    //         // Schließe ResultSet
    //         rs.close();
    //     } catch (SQLException e) {
    //         Logger.getLogger("CheckIfPortfolioStockAmountIsSufficientLogger").log(Level.SEVERE, "Fehler beim Abrufen des currentValues des Portfolio-Stock.", e);
    //     } finally {
    //         try {
    //             // Schließen von Statement und Connection, um Ressourcen freizugeben
    //             if (stmt != null) stmt.close();
    //             if (connection != null) connection.close();
    //         } catch (SQLException e) {
    //             Logger.getLogger("CheckIfPortfolioStockAmountIsSufficientLogger").log(Level.SEVERE, "Error beim Schließen der Ressourcen. Error: {0}", e);
    //         }
    //     }
    // }
}