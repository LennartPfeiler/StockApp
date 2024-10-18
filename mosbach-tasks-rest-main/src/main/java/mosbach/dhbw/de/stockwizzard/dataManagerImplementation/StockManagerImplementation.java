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

    //Create Session database table
    public void createStockTable() {
        Statement stmt = null;
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(dbUrl, username, password);
            stmt = connection.createStatement();
            String dropStockTableSQL = "DROP TABLE IF EXISTS group12stock";
            stmt.executeUpdate(dropStockTableSQL);

            String createStockTableSQL = "CREATE TABLE group12stock (" +
                     "symbol VARCHAR(100) PRIMARY KEY, " +
                     "stockPrice DOUBLE PRECISION NOT NULL, " +
                     "name VARCHAR(100) NOT NULL)";

            stmt.executeUpdate(createStockTableSQL);
        } catch (Exception e) {
            Logger.getLogger("CreateStockTableLogger").log(Level.SEVERE, "Stock table cannot be created. Error: {0}", e);
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                Logger.getLogger("CreateStockTableLogger").log(Level.SEVERE, "Error when closing the resource. Error: {0}", e);
                e.printStackTrace();
            }
        }
    }

    //Get stock data by symbol
    public Stock getStock(String symbol) {
        Statement stmt = null;
        Connection connection = null;
        Stock stock = null;
        Logger.getLogger("GetStockLogger").log(Level.INFO, "Start getStock-method");
        try {
            connection = DriverManager.getConnection(dbUrl, username, password);
            stmt = connection.createStatement();

            String selectStockSQL = "SELECT * FROM group12stock WHERE symbol = '" + symbol + "'";

            ResultSet rs = stmt.executeQuery(selectStockSQL);
            if (rs.next()) {
                stock = new Stock();
                stock.setSymbol(rs.getString("symbol"));
                stock.setName(rs.getString("name"));
                stock.setStockPrice(Double.parseDouble(rs.getString("stockprice")));
            }
            rs.close();
        } catch (SQLException e) {
            Logger.getLogger("GetStockLogger").log(Level.SEVERE, "Error when getting a stock. Error: {0}", e);
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                Logger.getLogger("GetStockLogger").log(Level.SEVERE, "Error when closing the resource. Error: {0}", e);
                e.printStackTrace();
            }
        }
        return stock;
    }

    //Add a new stock to the database
    public void addStock(Stock stock) {
        Statement stmt = null;
        Connection connection = null;
        Logger.getLogger("SetNewStockWriter").log(Level.INFO, "Start addStock-method");
        try {
            connection = DriverManager.getConnection(dbUrl, username, password);
            stmt = connection.createStatement();

            String insertStockSQL = "INSERT INTO group12stock (symbol, stockprice, name) VALUES (" +
                   "'" + stock.getSymbol() + "', " +
                   stock.getStockPrice() + ", " +
                   "'" + stock.getName() + "')";

            stmt.executeUpdate(insertStockSQL);     
        } catch (SQLException e) {
            Logger.getLogger("SetNewStockWriter").log(Level.SEVERE, "Error when adding a stock. Error: {0}", e);
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                Logger.getLogger("SetNewStockWriter").log(Level.SEVERE, "Error when closing the resource. Error: {0}", e);
                e.printStackTrace();
            }
        }
    }
}