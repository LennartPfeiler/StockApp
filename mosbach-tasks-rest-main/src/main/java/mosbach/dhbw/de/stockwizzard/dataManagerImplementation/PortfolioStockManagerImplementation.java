package mosbach.dhbw.de.stockwizzard.dataManagerImplementation;

import java.io.*;

import mosbach.dhbw.de.stockwizzard.dataManager.IPortfolioStockManager;
import mosbach.dhbw.de.stockwizzard.model.Portfolio;
import mosbach.dhbw.de.stockwizzard.model.PortfolioStock;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PortfolioStockManagerImplementation implements IPortfolioStockManager{

    private String fileName = "portfolios.properties";

    static PortfolioStockManagerImplementation databaseUser = null;

    private PortfolioStockManagerImplementation(){
        
    }

    static public PortfolioStockManagerImplementation getPortfolioStockManager() {
        if (databaseUser == null)
            databaseUser = new PortfolioStockManagerImplementation();
        return databaseUser;
    }

    public PortfolioStock getPortfolioStock(Integer portfolioId, String symbol){
        return new PortfolioStock(null, null, null, null, null);
    }

    public void addPortfolioStock(Integer portfolioId, String symbol){

    }

    public void editPortfolioStock(Integer portfolioId, String symbol, Integer transactionType){

    }

    public void deletePortfolioStock(Integer portfolioId, String symbol){

    }
}