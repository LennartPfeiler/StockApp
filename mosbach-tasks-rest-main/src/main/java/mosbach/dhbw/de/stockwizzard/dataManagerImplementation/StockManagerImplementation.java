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
}