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

    // public void createPortfolio(Portfolio portfolioData) {
    //     Properties properties = new Properties();
    
    //     // Erstelle die Schlüssel-Wert-Paare für das Portfolio
    //     properties.setProperty("Portfolio." + portfolioData.getPortfolioID().toString() + ".PortfolioID", String.valueOf(portfolioData.getPortfolioID()));
    //     properties.setProperty("Portfolio." + portfolioData.getPortfolioID().toString() + ".Value", String.valueOf(portfolioData.getValue()));
    //     properties.setProperty("Portfolio." + portfolioData.getPortfolioID().toString() + ".Email", portfolioData.getEmail());
    //     Logger.getLogger("CreatePortfolioLogger").log(Level.INFO, "Portfolio gespeichert: ID = {0}, Value = {1}, Email = {2}",
    // new Object[]{portfolioData.getPortfolioID(), portfolioData.getValue(), portfolioData.getEmail()});

    //     try {
    //         properties.store(new FileOutputStream(fileName), null);
    //     } catch (IOException e) {
    //         Logger.getLogger("SetNewUserWriter").log(Level.INFO, "File can not be written to disk");
    //     }
    // }
    
    public void addPortfolio(Portfolio portfolioData) {
        Properties properties = new Properties();
        
        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            try (InputStream resourceStream = loader.getResourceAsStream(fileName)) {
                if (resourceStream == null) {
                    Logger.getLogger("AddPortfolioWriter").log(Level.WARNING, "Die properties-Datei {0} wurde nicht gefunden.", fileName);
                    //return -1; // Datei nicht gefunden
                }
                properties.load(resourceStream);
            }
        } catch (IOException e) {
            Logger.getLogger("AddPortfolioWriter").log(Level.SEVERE, "Fehler beim Laden der properties-Datei.", e);
        }    

        int nextPortfolioId = getNextPortfolioId(properties);
        // Erstelle die Schlüssel-Wert-Paare für den neuen Benutzer
        properties.setProperty("Portfolio." + nextPortfolioId + ".PortfolioID", String.valueOf(nextPortfolioId));
        properties.setProperty("Portfolio." + nextPortfolioId + ".Value", String.valueOf(portfolioData.getValue()));
        properties.setProperty("Portfolio." + nextPortfolioId + ".Email", portfolioData.getEmail());
        Logger.getLogger("CreatePortfolioLogger").log(Level.INFO, "Portfolio gespeichert: ID = {0}, Value = {1}, Email = {2}",
    new Object[]{portfolioData.getPortfolioID(), portfolioData.getValue(), portfolioData.getEmail()});

        try {
            properties.store(new FileOutputStream(fileName), null);
        } catch (IOException e) {
            Logger.getLogger("AddPortfolioWriter").log(Level.INFO, "File can not be written to disk");
        }
    }

    public Portfolio getUserPortfolio(String email) {
        Properties properties = new Properties();
        Portfolio portfolio = null;
        try {
            // Lade die properties-Datei
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            try (InputStream resourceStream = loader.getResourceAsStream(fileName)) {
                if (resourceStream == null) {
                    Logger.getLogger("GetPortfolioReader").log(Level.WARNING, "Die properties-Datei {0} wurde nicht gefunden.", fileName);
                    return null; // Datei nicht gefunden
                }
                properties.load(resourceStream);
            }
    
            int i = 1;
            while (true) {
                // E-Mail-Adresse des aktuellen Portfolios abrufen
                String userEmailKey = "Portfolio." + i + ".Email";
                String currentUserEmail = properties.getProperty(userEmailKey);
    
                // Überprüfen, ob weitere Einträge vorhanden sind
                if (currentUserEmail == null) {
                    Logger.getLogger("GetPortfolioReader").log(Level.INFO, "Keine weiteren Portfolios gefunden. Abbruch bei Index: {0}", i);
                    break; // Kein weiterer Eintrag vorhanden, Schleife verlassen
                }
    
                // Überprüfen, ob die aktuelle E-Mail der gesuchten E-Mail entspricht
                if (currentUserEmail.equalsIgnoreCase(email)) {
                    try {
                        Integer portfolioID = Integer.valueOf(properties.getProperty("Portfolio." + i + ".PortfolioID"));
                        Double value = Double.valueOf(properties.getProperty("Portfolio." + i + ".Value"));
                        
                        portfolio = new Portfolio(portfolioID, value, currentUserEmail);
                        Logger.getLogger("GetPortfolioReader").log(Level.INFO, "Portfolio gefunden: {0}", portfolio);
                        break; // Benutzer gefunden, Schleife verlassen
                    } catch (NumberFormatException e) {
                        Logger.getLogger("GetPortfolioReader").log(Level.WARNING, "Fehler bei der Konvertierung der Portfolio-Daten.", e);
                        return null; // Abbruch, da die Daten fehlerhaft sind
                    }
                }
                i++; // Nächsten Benutzer prüfen
            }
        } catch (IOException e) {
            Logger.getLogger("GetPortfolioReader").log(Level.SEVERE, "Fehler beim Laden der properties-Datei.", e);
        }
    
        return portfolio;
    }
    
    private int getNextPortfolioId(Properties properties) {
        int maxId = 0;
        for (String key : properties.stringPropertyNames()) {
            if (key.startsWith("Portfolio.") && key.endsWith(".PortfolioID")) {
                // Extrahiere die ID zwischen "User." und ".Firstname"
                try {
                    int id = Integer.parseInt(key.split("\\.")[1]);
                    maxId = Math.max(maxId, id);
                } catch (NumberFormatException e) {
                    Logger.getLogger("PortfolioManager").log(Level.WARNING, "Ungültige ID in Properties-Datei: " + key, e);
                }
            }
        }
        return maxId + 1;
    }
}