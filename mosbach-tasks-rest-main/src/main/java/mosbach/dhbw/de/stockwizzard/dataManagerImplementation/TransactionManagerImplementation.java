package mosbach.dhbw.de.stockwizzard.dataManagerImplementation;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.*;

import mosbach.dhbw.de.stockwizzard.dataManager.ITransactionManager;
import mosbach.dhbw.de.stockwizzard.dataManager.IUserManager;
import mosbach.dhbw.de.stockwizzard.model.PortfolioStock;
import mosbach.dhbw.de.stockwizzard.model.Transaction;
import mosbach.dhbw.de.stockwizzard.model.TransactionContent;
import mosbach.dhbw.de.stockwizzard.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TransactionManagerImplementation implements ITransactionManager{

    String databaseConnectionnUrl = "postgresql://mhartwig:BE1yEbCLMjy7r2ozFRGHZaE6jHZUx0fFadiuqgW7TtVs1k15XZVwPSBkPLZVTle6@b8b0e4b9-8325-4a3f-be73-74f20266cd1a.postgresql.eu01.onstackit.cloud:5432/stackit";
    URI dbUri;
    String username = "";
    String password = "";
    String dbUrl = "";

    static TransactionManagerImplementation databaseUser = null;

    private TransactionManagerImplementation(){
        try {
            dbUri = new URI(databaseConnectionnUrl);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        username = dbUri.getUserInfo().split(":")[0];
        password = dbUri.getUserInfo().split(":")[1];
        dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath() + "?sslmode=require";
    }

    static public TransactionManagerImplementation getTransactionManager() {
        if (databaseUser == null)
            databaseUser = new TransactionManagerImplementation();
        return databaseUser;
    }

    public void createTransactionTable() {
        Statement stmt = null;
        Connection connection = null;

        try {
            connection = DriverManager.getConnection(dbUrl, username, password);
            stmt = connection.createStatement();
            String dropTable = "DROP TABLE IF EXISTS group12transaction";
            stmt.executeUpdate(dropTable);

            String createTable = "CREATE TABLE group12transaction (" +
                     "transactionID SERIAL PRIMARY KEY, " +
                     "transactionType INT NOT NULL, " +
                     "stockAmount DOUBLE PRECISION NOT NULL, " +
                     "date TIMESTAMP NOT NULL, " +
                     "symbol VARCHAR(100) NOT NULL, " +
                     "pricePerStock DOUBLE PRECISION NOT NULL, " +
                     "totalPrice DOUBLE PRECISION NOT NULL, " +
                     "email VARCHAR(100) NOT NULL, " +
                     "FOREIGN KEY (email) REFERENCES group12user(email) ON DELETE CASCADE," +
                     "FOREIGN KEY (symbol) REFERENCES group12stock(symbol) ON DELETE CASCADE)";

            stmt.executeUpdate(createTable);
        } catch (Exception e) {
            Logger.getLogger("CreateTransactionTableLogger").log(Level.INFO, "Transaction table cannot be created. Error: {0}", e);
        } finally {
            try {
                // Schließen von Statement und Connection, um Ressourcen freizugeben
                if (stmt != null) stmt.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                // Fehler beim Schließen protokollieren
                Logger.getLogger("CreateTransactionTableLogger").log(Level.SEVERE, "Error beim Schließen der Ressourcen. Error: {0}", e);
            }
        }
    }

    //CHANGE DATE DATATYPE TO DATE INSTEAD OF STRING
    public void addTransaction(TransactionContent transactionContent){
        Statement stmt = null;
        Connection connection = null;
        Logger.getLogger("SetNewTransactionWriter").log(Level.INFO, "Start addTransaction-method");
        try {
            // Stelle die Verbindung zur Datenbank her
            connection = DriverManager.getConnection(dbUrl, username, password);
            stmt = connection.createStatement();
    
            // SQL-Anweisung für das Einfügen eines neuen Portfolios
            String insertSQL = "INSERT INTO group12transaction (transactionType, stockAmount, date, symbol, pricePerStock, totalPrice, email) VALUES (" +
                    transactionContent.getTransactionType() + ", " +
                    transactionContent.getStockAmount() + ", " +
                    "'" + transactionContent.getDate() + "', " +
                    "'" + transactionContent.getSymbol() + "', " +
                    transactionContent.getPricePerStock() + ", " +
                    transactionContent.getTotalPrice() + ", " +
                    "'" + transactionContent.getEmail() + "')";
    
            // Führe die SQL-Anweisung aus
            stmt.executeUpdate(insertSQL);
            
            // Schließe das Statement und die Verbindung
        } catch (SQLException e) {
            Logger.getLogger("SetNewTransactionWriter").log(Level.SEVERE, "Fehler beim Hinzufügen der Tranaction.", e);
        } finally {
            try {
                // Schließen von Statement und Connection, um Ressourcen freizugeben
                if (stmt != null) stmt.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                // Fehler beim Schließen protokollieren
                Logger.getLogger("SetNewTransactionWriter").log(Level.SEVERE, "Error beim Schließen der Ressourcen. Error: {0}", e);
            }
        }
    }

    public List<Transaction> getAllTransactions(String email, String sortby){
        List<Transaction> transactions = new ArrayList<>();
        Statement stmt = null;
        Connection connection = null;

        try {
            connection = DriverManager.getConnection(dbUrl, username, password);
            stmt = connection.createStatement();

            String getTransactions = "SELECT * FROM group12transaction WHERE email = '" + email + "' ORDER BY " + sortby + " DESC";

            ResultSet rs = stmt.executeQuery(getTransactions);
            while (rs.next()) {
                transactions.add(
                        new Transaction(
                                Integer.parseInt(rs.getString("transactionid")),
                                Integer.parseInt(rs.getString("transactiontype")),
                                Double.parseDouble(rs.getString("stockamount")),
                                Timestamp.valueOf(rs.getString("date")),
                                Double.parseDouble(rs.getString("priceperstock")),
                                Double.parseDouble(rs.getString("totalprice")),
                                rs.getString("email"),
                                rs.getString("symbol")
                                )
                );
            }
            rs.close();
        } catch (Exception e) {
            Logger.getLogger("GetTransactionsLoger").log(Level.INFO, "Can't get all transactions. Error: {0}", e);
        } finally {
            try {
                // Schließen von Statement und Connection, um Ressourcen freizugeben
                if (stmt != null) stmt.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                // Fehler beim Schließen protokollieren
                Logger.getLogger("GetTransactionsLoger").log(Level.SEVERE, "Error beim Schließen der Ressourcen. Error: {0}", e);
            }
        }
        return transactions;
    }
}