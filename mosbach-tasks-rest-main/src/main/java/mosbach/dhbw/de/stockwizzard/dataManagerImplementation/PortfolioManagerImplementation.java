package mosbach.dhbw.de.stockwizzard.dataManagerImplementation;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.*;

import mosbach.dhbw.de.stockwizzard.dataManager.IPortfolioManager;
import mosbach.dhbw.de.stockwizzard.model.Portfolio;
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

    //Create Portfolio database table
    public void createPortfolioTable() {
        Statement stmt = null;
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(dbUrl, username, password);
            stmt = connection.createStatement();
            String dropPortfolioTableSQL = "DROP TABLE IF EXISTS group12portfolio";
            stmt.executeUpdate(dropPortfolioTableSQL);

            String createPortfolioTableSQL = "CREATE TABLE group12portfolio (" +
                     "portfolioid SERIAL PRIMARY KEY, " +
                     "value DOUBLE PRECISION NOT NULL, " +
                     "startValue DOUBLE PRECISION NOT NULL, " +
                     "email VARCHAR(100) NOT NULL, " +
                     "FOREIGN KEY (email) REFERENCES group12user(email) ON DELETE CASCADE)";

            stmt.executeUpdate(createPortfolioTableSQL);
        } catch (Exception e) {
            Logger.getLogger("CreatePortfolioTableLogger").log(Level.INFO, "Portfolio table cannot be created. Error: {0}", e);
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                Logger.getLogger("CreatePortfolioTableLogger").log(Level.SEVERE, "Error when closing the resource. Error: {0}", e);
            }
        }
    }
    
    //Create a portfolio for a new user
    public void addPortfolio(Portfolio portfolioData) {
        Statement stmt = null;
        Connection connection = null;
        Logger.getLogger("SetNewPortfolioWriter").log(Level.INFO, "Start addPortfolio-method");
        try {
            connection = DriverManager.getConnection(dbUrl, username, password);
            stmt = connection.createStatement();
    
            String insertPortfolioSQL = "INSERT INTO group12portfolio (value, startValue, email) VALUES (" +
                    portfolioData.getValue() + ", " +
                    portfolioData.getStartValue() + ", " +
                    "'" + portfolioData.getEmail() + "')";
    
            stmt.executeUpdate(insertPortfolioSQL);
            Logger.getLogger("SetNewPortfolioWriter").log(Level.INFO, "Portfolio successfully added");
        } catch (SQLException e) {
            Logger.getLogger("SetNewPortfolioWriter").log(Level.SEVERE, "Error while creating new portfolio.", e);
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                Logger.getLogger("SetNewPortfolioWriter").log(Level.SEVERE, "Error when closing the resource. Error: {0}", e);
            }
        }
    }

    //Get a user profile by an email
    public Portfolio getUserPortfolio(String email) {
        Statement stmt = null;
        Connection connection = null;
        Portfolio portfolio = null;
        try {
            connection = DriverManager.getConnection(dbUrl, username, password);
            stmt = connection.createStatement();

            String selectPortfolioSQL = "SELECT * FROM group12portfolio WHERE email = '" + email + "'";
            
            ResultSet rs = stmt.executeQuery(selectPortfolioSQL);
            if (rs.next()) {
                int portfolioID = rs.getInt("portfolioid");
                double value = rs.getDouble("value");
                double startValue = rs.getDouble("startvalue");
                String userEmail = rs.getString("email");
                portfolio = new Portfolio(portfolioID, value, startValue, userEmail);
                Logger.getLogger("GetPortfolioReader").log(Level.INFO, "Portfolio found");
            } else {
                Logger.getLogger("GetPortfolioReader").log(Level.INFO, "No portfolio found for the given email");
            }
            rs.close();
        } catch (SQLException e) {
            Logger.getLogger("GetPortfolioReader").log(Level.SEVERE, "Error when retrieving the portfolio from the database.", e);
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                Logger.getLogger("GetPortfolioReader").log(Level.SEVERE, "Error when closing the resource. Error: {0}", e);
            }
        }
        return portfolio;
    }

    //Change the value of a portfolio
    public void editPortfolioValue(Integer portfolioId, Double oldValue, Double addition){
        Statement stmt = null;
        Connection connection = null;
        Logger.getLogger("UpdatePortfolioValueLogger").log(Level.INFO, "Start updatePortfolioValue method");
        try {
            connection = DriverManager.getConnection(dbUrl, username, password);
            stmt = connection.createStatement();

            String updatePortfolioValueSQL = "UPDATE group12portfolio SET value = " + (oldValue + addition) +
                            " WHERE portfolioid = " + portfolioId;

            stmt.executeUpdate(updatePortfolioValueSQL);
        } catch (SQLException e) {
            Logger.getLogger("UpdatePortfolioValueLogger").log(Level.SEVERE, "Error when updating the portfolio value.", e);
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                Logger.getLogger("UpdatePortfolioValueLogger").log(Level.SEVERE, "Error when closing the resource.", e);
            }
        }
    }

    //Change all portfolio values an user can edit manually in the Frontend
    public void editAllPortfolioValues(String email, String newEmail, Double newStartValue, Double newValue){
        Statement stmt = null;
        Connection connection = null;
        Logger.getLogger("UpdateAllPortfolioValuesLogger").log(Level.INFO, "Start editAllPortfolioValues method");

        try {
            connection = DriverManager.getConnection(dbUrl, username, password);
            stmt = connection.createStatement();

            String updateAllPortfolioValuesSQL = "UPDATE group12portfolio SET email = '" + newEmail + "', startvalue = " + newStartValue + ", value = " + newValue + " WHERE email = '" + email + "'";

            stmt.executeUpdate(updateAllPortfolioValuesSQL);
        } catch (SQLException e) {
            Logger.getLogger("UpdateAllPortfolioValuesLogger").log(Level.SEVERE, "Error when updating all portfolio values.", e);
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                Logger.getLogger("UpdateAllPortfolioValuesLogger").log(Level.SEVERE, "Error when closing the resource.", e);
            }
        }
    }

    //Reset an user portfolio to registration status
    public void resetPortfolio(String email) {
        Statement stmt = null;
        Connection connection = null;
        Logger.getLogger("ResetPortfolioLogger").log(Level.INFO, "Start resetPortfolio method");
        try {
            connection = DriverManager.getConnection(dbUrl, username, password);
            stmt = connection.createStatement();
            String resetPortfolioSQL = "UPDATE group12portfolio SET value = (SELECT startvalue from group12portfolio WHERE email = '" + email + "') WHERE email = '" + email + "' ";
            stmt.executeUpdate(resetPortfolioSQL);
        } catch (SQLException e) {
            Logger.getLogger("ResetPortfolioLogger").log(Level.SEVERE, "Error when resetting the portfolio.", e);
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                Logger.getLogger("ResetPortfolioLogger").log(Level.SEVERE, "Error when closing the resource.", e);
            }
        }
    }
}