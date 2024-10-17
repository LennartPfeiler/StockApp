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
import mosbach.dhbw.de.stockwizzard.model.PortfolioStockValue;
import mosbach.dhbw.de.stockwizzard.model.Transaction;

public class PortfolioStockManagerImplementation implements IPortfolioStockManager{

    TransactionManagerImplementation transactionManager = TransactionManagerImplementation.getTransactionManager();

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

    public void addPortfolioStock(Integer portfolioId, String symbol, Double stockAmount, Double totalPrice){
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
                Double newBoughtValue = existingboughtValue + totalPrice;
                Double newCurrentValue = existingCurrentValue + totalPrice;

                String updateSQL = "UPDATE group12portfolioStock SET " +
                    "stockamount = " + newAmount + ", " +
                    "boughtvalue = " + newBoughtValue + ", " +
                    "currentvalue = " + newCurrentValue + " " + // Leerzeichen vor dem WHERE
                    "WHERE portfolioid = " + portfolioId + " AND symbol = '" + symbol + "'";
                stmt.executeUpdate(updateSQL);
                Logger.getLogger("AddPortfolioStockLogger").log(Level.INFO, "Stock updated successfully.");

            } else {
                // Wenn der Stock nicht vorhanden ist, füge ihn hinzu
                String insertSQL = "INSERT INTO group12portfolioStock (portfolioid, symbol, stockamount, boughtvalue, currentvalue) VALUES (" +
                                portfolioId + ", '" + // Portfolio-ID
                                symbol + "', " + // Symbol
                                stockAmount + ", " + // Stock amount
                                totalPrice + ", " + // Bought value
                                totalPrice + ")"; // Current value
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

    public void deletePortfolioStock(Integer portfolioId, String symbol, Double stockAmount, Double totalPrice, PortfolioStockValue portfolioStockValues, List<Transaction> transactionsInPortfolio){
        Statement stmt = null;
        Connection connection = null;
        //IN PORTFOLIO MIT REINNEHMEN
        final double EPSILON = 0.000001;
        if (Math.abs(totalPrice - portfolioStockValues.getCurrentValue()) < EPSILON) {
            try {
                connection = DriverManager.getConnection(dbUrl, username, password);
                stmt = connection.createStatement();
                //works
                String deletePortfolioStock = "DELETE FROM group12portfoliostock WHERE symbol = '" + symbol + "' AND portfolioid=" + portfolioId;
                stmt.executeUpdate(deletePortfolioStock);
                //works
                for (Transaction transaction : transactionsInPortfolio) {
                    transactionManager.updateLeftinPortfolio(transaction.getTransactionID(), 0.0);
                }
    
            } catch (SQLException e) {
                Logger.getLogger("DeletePortfolioStockLogger").log(Level.SEVERE, "Fehler beim Löschen des Portfolio-Stock.", e);
            } finally {
                try {
                    if (stmt != null) stmt.close();
                    if (connection != null) connection.close();
                } catch (SQLException e) {
                    Logger.getLogger("DeletePortfolioStockLogger").log(Level.SEVERE, "Error beim Schließen der Ressourcen. Error: {0}", e);
                }
            }
        }
        else {
            try {
                connection = DriverManager.getConnection(dbUrl, username, password);
                stmt = connection.createStatement();
            
                Double remainingAmount = totalPrice;
                Double totalBoughtValueReduction = 0.0;
                Logger.getLogger("DeletePortfolioStockLogger").log(Level.SEVERE, "in else drin: {0}", remainingAmount);
                // Gehe die Transaktionen durch, bis die zu verkaufende Menge vollständig abgedeckt ist
                for (Transaction transaction : transactionsInPortfolio) {
                    if (remainingAmount <= 0) {
                        break;
                    }
            
                    Double leftInTransaction = transaction.getLeftInPortfolio();
                    Double transactionBoughtValue = transaction.getTotalPrice(); // Gesamtwert der Transaktion
                    Integer transactionId = transaction.getTransactionID();
            
                    if (remainingAmount >= leftInTransaction) {
                        // Verkaufe den gesamten verbleibenden Betrag dieser Transaktion
                        remainingAmount -= leftInTransaction;
                        totalBoughtValueReduction += transactionBoughtValue;
                        transactionManager.updateLeftinPortfolio(transactionId, 0.0);
                    } else {
                        // Verkaufe einen Teil des verbleibenden Betrags dieser Transaktion
                        Double proportion = remainingAmount / leftInTransaction;
                        Double reductionInBoughtValue = transactionBoughtValue * proportion;
                        totalBoughtValueReduction += reductionInBoughtValue;
            
                        Double newLeftInTransaction = leftInTransaction - remainingAmount;
                        remainingAmount = 0.0;
                        transactionManager.updateLeftinPortfolio(transactionId, newLeftInTransaction);
                    }
                }
            
                // Aktualisiere den PortfolioStock-Eintrag
                Double newCurrentValue = portfolioStockValues.getCurrentValue() - totalPrice;
                Double newBoughtValue = portfolioStockValues.getBoughtValue() - totalBoughtValueReduction;
            
                String updatePortfolioStockSQL = "UPDATE group12portfoliostock SET currentvalue = " + newCurrentValue + 
                                                 ", boughtvalue = " + newBoughtValue + 
                                                 ", stockamount = stockamount - " + stockAmount + 
                                                 " WHERE portfolioid = " + portfolioId + 
                                                 " AND symbol = '" + symbol + "'";
                stmt.executeUpdate(updatePortfolioStockSQL);
            
            } catch (SQLException e) {
                Logger.getLogger("DeletePortfolioStockLogger").log(Level.SEVERE, "Fehler beim Aktualisieren des Portfolio-Stock.", e);
            } finally {
                try {
                    if (stmt != null) stmt.close();
                    if (connection != null) connection.close();
                } catch (SQLException e) {
                    Logger.getLogger("DeletePortfolioStockLogger").log(Level.SEVERE, "Error beim Schließen der Ressourcen. Error: {0}", e);
                }
            }
            
        }
        
    }

    public PortfolioStockValue checkPortfolioStockValue(Double sellRequestAmount, String email, String symbol) {   
        Connection connection = null;
        Statement stmt = null;
        ResultSet rs = null;
        Logger logger = Logger.getLogger("CheckIfPortfolioStockAmountIsSufficientLogger");
        logger.log(Level.INFO, "Start CheckIfPortfolioStockAmountIsSufficient-method");
    
        try {
            // Verbindung zur Datenbank herstellen
            connection = DriverManager.getConnection(dbUrl, username, password);
            stmt = connection.createStatement();
    
            // SQL-Abfrage zum Überprüfen, ob der Stock bereits im Portfolio vorhanden ist
            String checkSQL = "SELECT boughtvalue, currentvalue FROM group12portfoliostock WHERE symbol = '" + symbol + 
                              "' AND portfolioid = (SELECT portfolioid FROM group12portfolio WHERE email = '" + email + "')";
            rs = stmt.executeQuery(checkSQL);
    
            if (rs.next()) {
                logger.log(Level.INFO, "Stock is in Portfolio.");
                Double boughtValue = rs.getDouble("boughtvalue");
                Double currentValue = rs.getDouble("currentvalue");
                if (currentValue >= sellRequestAmount) {
                    return new PortfolioStockValue(currentValue, boughtValue);
                } else {
                    return new PortfolioStockValue(-1.0, -1.0); // Nicht genug Wert im Portfolio
                }
            } else {
                // Wenn der Stock nicht vorhanden ist, return null
                logger.log(Level.INFO, "Stock is not in Portfolio.");
                return null;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Fehler beim Abrufen des currentValues des Portfolio-Stock.", e);
            return null; // Fehlerfall: null zurückgeben
        } finally {
            // Ressourcen freigeben
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Fehler beim Schließen der Ressourcen.", e);
            }
        }
    }
    
}