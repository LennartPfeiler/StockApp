package mosbach.dhbw.de.stockwizzard.dataManagerImplementation;

import java.io.*;

import mosbach.dhbw.de.stockwizzard.dataManager.IPortfolioManager;
import mosbach.dhbw.de.stockwizzard.model.Portfolio;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PortfolioManagerImplementation implements IPortfolioManager{

    private String fileName = "portfolios.properties";

    static PortfolioManagerImplementation databaseUser = null;

    private PortfolioManagerImplementation(){
        
    }

    static public PortfolioManagerImplementation getPortfolioManager() {
        if (databaseUser == null)
            databaseUser = new PortfolioManagerImplementation();
        return databaseUser;
    }

    public void createPortfolio(Portfolio portfolioData) {
        Properties properties = new Properties();
    
        // Erstelle die Schlüssel-Wert-Paare für das Portfolio
        properties.setProperty("Portfolio." + portfolioData.getPortfolioID().toString() + ".PortfolioID", String.valueOf(portfolioData.getPortfolioID()));
        properties.setProperty("Portfolio." + portfolioData.getPortfolioID().toString() + ".Value", String.valueOf(portfolioData.getValue()));
        properties.setProperty("Portfolio." + portfolioData.getPortfolioID().toString() + ".Email", portfolioData.getEmail());
        
        try {
            properties.store(new FileOutputStream(fileName), null);
        } catch (IOException e) {
            Logger.getLogger("SetNewUserWriter").log(Level.INFO, "File can not be written to disk");
        }
    }
    

}