package mosbach.dhbw.de.stockwizzard.dataManagerImplementation;

import java.io.*;
import mosbach.dhbw.de.stockwizzard.dataManager.ITransactionManager;
import mosbach.dhbw.de.stockwizzard.dataManager.IUserManager;
import mosbach.dhbw.de.stockwizzard.model.Transaction;
import mosbach.dhbw.de.stockwizzard.model.TransactionContent;
import mosbach.dhbw.de.stockwizzard.model.User;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TransactionManagerImplementation implements ITransactionManager{

    private String fileName = "transactions.properties";

    static TransactionManagerImplementation databaseUser = null;

    private TransactionManagerImplementation(){
        
    }

    static public TransactionManagerImplementation getTransactionManager() {
        if (databaseUser == null)
            databaseUser = new TransactionManagerImplementation();
        return databaseUser;
    }

    //CHANGE DATE DATATYPE TO DATE INSTEAD OF STRING
    public void addTransaction(TransactionContent transactionContent){
        Properties properties = new Properties();
        
        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            try (InputStream resourceStream = loader.getResourceAsStream(fileName)) {
                if (resourceStream == null) {
                    Logger.getLogger("AddTransaction").log(Level.WARNING, "Die properties-Datei {0} wurde nicht gefunden.", fileName);
                    //return -1; // Datei nicht gefunden
                }
                properties.load(resourceStream);
            }
        } catch (IOException e) {
            Logger.getLogger("AddTransaction").log(Level.SEVERE, "Fehler beim Laden der properties-Datei.", e);
        }    
        Integer nextTransactionId = getNextTransactionId(properties);

        // Erstelle die Schlüssel-Wert-Paare für den neuen Benutzer
        properties.setProperty("Transaction." + nextTransactionId + ".TransactionID", nextTransactionId.toString());
        properties.setProperty("Transaction." + nextTransactionId + ".TransactionType", transactionContent.getTransactionType().toString());
        properties.setProperty("Transaction." + nextTransactionId + ".StockAmount", transactionContent.getStockAmount().toString());
        properties.setProperty("Transaction." + nextTransactionId + ".Date", transactionContent.getDate().toString());
        properties.setProperty("Transaction." + nextTransactionId + ".PricePerStock", transactionContent.getPricePerStock().toString());
        properties.setProperty("Transaction." + nextTransactionId + ".TotalPrice", transactionContent.getTotalPrice().toString());
        properties.setProperty("Transaction." + nextTransactionId + ".Email", transactionContent.getEmail());
        properties.setProperty("Transaction." + nextTransactionId + ".Symbol", transactionContent.getSymbol());

        try {
            properties.store(new FileOutputStream(fileName), null);
        } catch (IOException e) {
            Logger.getLogger("AddTransaction").log(Level.INFO, "File can not be written to disk");
        }
    }
    
    private int getNextTransactionId(Properties properties) {
        int maxId = 0;
        for (String key : properties.stringPropertyNames()) {
            if (key.startsWith("Transaction.") && key.endsWith(".TransactionID")) {
                try {
                    int id = Integer.parseInt(key.split("\\.")[1]);
                    maxId = Math.max(maxId, id);
                } catch (NumberFormatException e) {
                    Logger.getLogger("TransactionManager").log(Level.WARNING, "Ungültige ID in Properties-Datei: " + key, e);
                }
            }
        }
        return maxId + 1;
    }

    public Transaction getTransaction(Integer transactionID){
        Properties properties = new Properties();
        Transaction transaction = null;
        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            try (InputStream resourceStream = loader.getResourceAsStream(fileName)) {
                if (resourceStream == null) {
                    Logger.getLogger("GetTransactionReader").log(Level.WARNING, "Die properties-Datei {0} wurde nicht gefunden.", fileName);
                    return null; // Datei nicht gefunden
                }
                properties.load(resourceStream);
            }
            int i = 1;
            while (true) {
                String transactionIdKey = "Transaction." + i + ".TransactionID";
                String currentTransactionId = properties.getProperty(transactionIdKey);

                // Überprüfen, ob die aktuelle E-Mail der gesuchten E-Mail entspricht
                if (currentTransactionId.equalsIgnoreCase(transactionID.toString())) {
                    Integer transactionType = Integer.valueOf(properties.getProperty("Transaction." + i + ".TransactionType"));
                    Double stockAmount = Double.valueOf(properties.getProperty("Transaction." + i + ".StockAmount"));
                    String date = properties.getProperty("Transaction." + i + ".Date");
                    Double pricePerStock = Double.valueOf(properties.getProperty("Transaction." + i + ".PricePerStock"));
                    Double totalPrice = Double.valueOf(properties.getProperty("Transaction." + i + ".TotalPrice"));
                    String email = properties.getProperty("Transaction." + i + ".Email");
                    String symbol = properties.getProperty("Transaction." + i + ".Symbol");

                    transaction = new Transaction(transactionID, transactionType, stockAmount, date, pricePerStock, totalPrice, email, symbol);
                    Logger.getLogger("GetTransactionReader").log(Level.INFO, "Benutzer gefunden: {0}", transaction);
                    break; // Benutzer gefunden, Schleife verlassen
                }
                i++; // Nächsten Benutzer prüfen
            }
        } catch (IOException e) {
            Logger.getLogger("GetTransactionReader").log(Level.SEVERE, "Fehler beim Laden der properties-Datei.", e);
        }
        return transaction;
    }
}