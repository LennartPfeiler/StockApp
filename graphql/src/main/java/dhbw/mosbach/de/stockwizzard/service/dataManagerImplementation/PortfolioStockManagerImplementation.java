package dhbw.mosbach.de.stockwizzard.service.dataManagerImplementation;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import dhbw.mosbach.de.stockwizzard.service.dataManager.IPortfolioStockManager;
import dhbw.mosbach.de.stockwizzard.model.Portfolio;
import dhbw.mosbach.de.stockwizzard.model.PortfolioStock;
import dhbw.mosbach.de.stockwizzard.model.PortfolioStockValue;
import dhbw.mosbach.de.stockwizzard.model.Transaction;

public class PortfolioStockManagerImplementation implements IPortfolioStockManager{

    private TransactionManagerImplementation transactionManager = TransactionManagerImplementation.getTransactionManager();

    private String databaseConnectionnUrl = "postgresql://mhartwig:BE1yEbCLMjy7r2ozFRGHZaE6jHZUx0fFadiuqgW7TtVs1k15XZVwPSBkPLZVTle6@b8b0e4b9-8325-4a3f-be73-74f20266cd1a.postgresql.eu01.onstackit.cloud:5432/stackit";
    private URI dbUri;
    private String username = "";
    private String password = "";
    private String dbUrl = "";

    private static PortfolioStockManagerImplementation databaseUser = null;

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

    //Create PortfolioStock database table
    public void createPortfolioStockTable() {
        Statement stmt = null;
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(dbUrl, username, password);
            stmt = connection.createStatement();
            String dropPortfolioStockTableSQL = "DROP TABLE IF EXISTS group12portfolioStock";
            stmt.executeUpdate(dropPortfolioStockTableSQL);

            String createPortfolioStockTableSQL = "CREATE TABLE group12portfolioStock (" +
                     "portfolioid SERIAL NOT NULL, " +
                     "symbol VARCHAR(10) NOT NULL, " +
                     "stockamount DOUBLE PRECISION NOT NULL, " +
                     "boughtvalue DOUBLE PRECISION NOT NULL, " +
                     "currentvalue DOUBLE PRECISION NOT NULL, " +
                     "PRIMARY KEY (portfolioid, symbol), " +
                     "FOREIGN KEY (portfolioid) REFERENCES group12portfolio(portfolioid) ON DELETE CASCADE, " +
                     "FOREIGN KEY (symbol) REFERENCES group12stock(symbol) ON DELETE CASCADE)";

            stmt.executeUpdate(createPortfolioStockTableSQL);
        } catch (Exception e) {
            Logger.getLogger("CreatePortfolioStockTableLogger").log(Level.SEVERE, "PortfolioStock table cannot be created. Error: {0}", e);
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                Logger.getLogger("CreateUserTableLogger").log(Level.SEVERE, "Error when closing the resource. Error: {0}", e);
                e.printStackTrace();
            }
        }
    }

    //Add a new or update an existing PortfolioStock 
    public void increasePortfolioStock(Integer portfolioId, String symbol, Double stockAmount, Double totalPrice){
        Statement stmt = null;
        Connection connection = null;
        Logger.getLogger("AddPortfolioStockLogger").log(Level.INFO, "Start addPortfolioStock-method");
        try {
            connection = DriverManager.getConnection(dbUrl, username, password);
            stmt = connection.createStatement();

            String checkSQL = "SELECT * FROM group12portfolioStock WHERE portfolioid = " + portfolioId + " AND symbol = '" + symbol + "'";
            ResultSet rs = stmt.executeQuery(checkSQL);

            if (rs.next()) {
                Double existingAmount = rs.getDouble("stockamount");
                Double existingboughtValue = rs.getDouble("boughtvalue");
                Double existingCurrentValue = rs.getDouble("currentvalue");
                Double newAmount = existingAmount + stockAmount;
                Double newBoughtValue = existingboughtValue + totalPrice;
                Double newCurrentValue = existingCurrentValue + totalPrice;

                String updatePortfolioStockSQL = "UPDATE group12portfolioStock SET " +
                    "stockamount = " + newAmount + ", " +
                    "boughtvalue = " + newBoughtValue + ", " +
                    "currentvalue = " + newCurrentValue + " " + 
                    "WHERE portfolioid = " + portfolioId + " AND symbol = '" + symbol + "'";
                stmt.executeUpdate(updatePortfolioStockSQL);
                Logger.getLogger("AddPortfolioStockLogger").log(Level.INFO, "Stock updated successfully.");

            } else {
                String insertPortfolioStockSQL = "INSERT INTO group12portfolioStock (portfolioid, symbol, stockamount, boughtvalue, currentvalue) VALUES (" +
                                portfolioId + ", '" + 
                                symbol + "', " + 
                                stockAmount + ", " + 
                                totalPrice + ", " + 
                                totalPrice + ")";
                stmt.executeUpdate(insertPortfolioStockSQL);
                Logger.getLogger("AddPortfolioStockLogger").log(Level.INFO, "Stock added successfully.");
            }
            rs.close();
        } catch (SQLException e) {
            Logger.getLogger("AddPortfolioStockLogger").log(Level.SEVERE, "Error when increasing a portfolio stock.", e);
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                Logger.getLogger("AddPortfolioStockLogger").log(Level.SEVERE, "Error when closing the resource. Error: {0}", e);
                e.printStackTrace();
            }
        }
    }

    //Get all portfolio stocks of an user sorted by the parameter sortby
    public List<PortfolioStock> getAllPortfolioStocks(String email, String sortby){
        List<PortfolioStock> portfolioStocks = new ArrayList<>();
        Statement stmt = null;
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(dbUrl, username, password);
            stmt = connection.createStatement();

            String getPortfolioStocksSQL = "SELECT * FROM group12portfoliostock WHERE portfolioid =" + 
            "(SELECT portfolioid FROM group12portfolio WHERE email = '" + email + "') ORDER BY " + sortby + " ASC";

            ResultSet rs = stmt.executeQuery(getPortfolioStocksSQL);
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
            Logger.getLogger("GetPortfolioStocksLogger").log(Level.INFO, "Error when getting all portfolio stocks. Error: {0}", e);
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                Logger.getLogger("GetPortfolioStocksLogger").log(Level.SEVERE, "Error when closing the resource. Error: {0}", e);
                e.printStackTrace();
            }
        }
        return portfolioStocks;
    }

    //Delete oder update a portfolio stock 
    public void decreasePortfolioStock(Integer portfolioId, String symbol, Double stockAmount, Double totalPrice, PortfolioStockValue portfolioStockValues, List<Transaction> transactionsInPortfolio){
        Statement stmt = null;
        Connection connection = null;
        final double EPSILON = 0.000001;
        if (Math.abs(totalPrice - portfolioStockValues.getCurrentValue()) < EPSILON) {
            try {
                connection = DriverManager.getConnection(dbUrl, username, password);
                stmt = connection.createStatement();

                String deletePortfolioStockSQL = "DELETE FROM group12portfoliostock WHERE symbol = '" + symbol + "' AND portfolioid=" + portfolioId;

                stmt.executeUpdate(deletePortfolioStockSQL);
                for (Transaction transaction : transactionsInPortfolio) {
                    transactionManager.editLeftinPortfolio(transaction.getTransactionID(), 0.0);
                }
            } catch (SQLException e) {
                Logger.getLogger("DeletePortfolioStockLogger").log(Level.SEVERE, "Error when deleting a portfolio stock.", e);
                e.printStackTrace();
            } finally {
                try {
                    if (stmt != null) stmt.close();
                    if (connection != null) connection.close();
                } catch (SQLException e) {
                    Logger.getLogger("DeletePortfolioStockLogger").log(Level.SEVERE, "Error when closing the resource. Error: {0}", e);
                    e.printStackTrace();
                }
            }
        }
        else {
            try {
                connection = DriverManager.getConnection(dbUrl, username, password);
                stmt = connection.createStatement();
            
                Double remainingAmount = totalPrice;
                Double totalBoughtValueReduction = 0.0;
                for (Transaction transaction : transactionsInPortfolio) {
                    if (remainingAmount <= 0) {
                        break;
                    }
            
                    Double leftInTransaction = transaction.getLeftInPortfolio();
                    Double transactionBoughtValue = transaction.getTotalPrice();
                    Integer transactionId = transaction.getTransactionID();
            
                    if (remainingAmount >= leftInTransaction) {
                        remainingAmount -= leftInTransaction;
                        totalBoughtValueReduction += transactionBoughtValue;
                        transactionManager.editLeftinPortfolio(transactionId, 0.0);
                    } else {
                        Double proportion = remainingAmount / leftInTransaction;
                        Double reductionInBoughtValue = transactionBoughtValue * proportion;
                        totalBoughtValueReduction += reductionInBoughtValue;
            
                        Double newLeftInTransaction = leftInTransaction - remainingAmount;
                        remainingAmount = 0.0;
                        transactionManager.editLeftinPortfolio(transactionId, newLeftInTransaction);
                    }
                }
            
                Double newCurrentValue = portfolioStockValues.getCurrentValue() - totalPrice;
                Double newBoughtValue = portfolioStockValues.getBoughtValue() - totalBoughtValueReduction;
            
                String updatePortfolioStockSQL = "UPDATE group12portfoliostock SET currentvalue = " + newCurrentValue + 
                                                 ", boughtvalue = " + newBoughtValue + 
                                                 ", stockamount = stockamount - " + stockAmount + 
                                                 " WHERE portfolioid = " + portfolioId + 
                                                 " AND symbol = '" + symbol + "'";
                stmt.executeUpdate(updatePortfolioStockSQL);
            
            } catch (SQLException e) {
                Logger.getLogger("DeletePortfolioStockLogger").log(Level.SEVERE, "Error when decreasing a portfolio stock.", e);
                e.printStackTrace();
            } finally {
                try {
                    if (stmt != null) stmt.close();
                    if (connection != null) connection.close();
                } catch (SQLException e) {
                    Logger.getLogger("DeletePortfolioStockLogger").log(Level.SEVERE, "Error when closing the resource. Error: {0}", e);
                    e.printStackTrace();
                }
            }
        }
    }

    //Get the bought and current value of a portfolio stock
    public PortfolioStockValue getPortfolioStockValues(Double sellRequestAmount, String email, String symbol) {   
        Connection connection = null;
        Statement stmt = null;
        ResultSet rs = null;
        Logger.getLogger("GetPortfolioStockValuesLogger").log(Level.INFO, "Start CheckIfPortfolioStockAmountIsSufficient-method");
        try {
            connection = DriverManager.getConnection(dbUrl, username, password);
            stmt = connection.createStatement();
    
            String checkPortfolioStockSQL = "SELECT boughtvalue, currentvalue FROM group12portfoliostock WHERE symbol = '" + symbol + 
                              "' AND portfolioid = (SELECT portfolioid FROM group12portfolio WHERE email = '" + email + "')";
            rs = stmt.executeQuery(checkPortfolioStockSQL);
    
            if (rs.next()) {
                Double boughtValue = rs.getDouble("boughtvalue");
                Double currentValue = rs.getDouble("currentvalue");
                if (currentValue >= sellRequestAmount) {
                    return new PortfolioStockValue(currentValue, boughtValue);
                } else {
                    return new PortfolioStockValue(-1.0, -1.0);
                }
            } else {
                return null;
            }
        } catch (SQLException e) {
            Logger.getLogger("GetPortfolioStockValuesLogger").log(Level.INFO, "Error when fetching value data from portfolio stock", e);
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                Logger.getLogger("DeletePortfolioStockLogger").log(Level.SEVERE, "Error when closing the resource. Error: {0}", e);
                e.printStackTrace();
            }
        }
    }
    
    //Delete all portfolio stocks of an user
    public void deleteAllPortfolioStocks(String email) {
        Statement stmt = null;
        Connection connection = null;
        Logger.getLogger("DeleteAllPortfolioStocksLogger").log(Level.INFO, "Start deleteAllPortfolioStocks method");
        try {
            connection = DriverManager.getConnection(dbUrl, username, password);
            stmt = connection.createStatement();

            String deletePortfolioStocksSQL = "DELETE FROM group12portfoliostock WHERE portfolioid = (SELECT portfolioid FROM group12portfolio WHERE email= '" + email + "')";
            
            stmt.executeUpdate(deletePortfolioStocksSQL);
        } catch (SQLException e) {
            Logger.getLogger("DeleteAllPortfolioStocksLogger").log(Level.SEVERE, "Error when deleting all portfolio stocks.", e);
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                Logger.getLogger("DeleteAllPortfolioStocksLogger").log(Level.SEVERE, "Error when closing the resource.", e);
                e.printStackTrace();
            }
        }
    }

}