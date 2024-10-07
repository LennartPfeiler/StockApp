package mosbach.dhbw.de.stockwizzard.dataManagerImplementation;

import java.io.*;
import mosbach.dhbw.de.stockwizzard.dataManager.IUserManager;
import mosbach.dhbw.de.stockwizzard.dataManagerImplementation.PasswordManagerImplementation;
import mosbach.dhbw.de.stockwizzard.model.User;
import mosbach.dhbw.de.stockwizzard.model.EmailCheckResponse;
import mosbach.dhbw.de.stockwizzard.model.EditRequest;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserManagerImplementation implements IUserManager{

    private String fileName = "users.properties";
    private String transactionFile = "transactions.properties";
    private String sessionsFile = "sessions.properties";
    private String portoliosFile = "portfolios.properties";
    
    PasswordManagerImplementation passwordManager = PasswordManagerImplementation.getPasswordManager();

    static UserManagerImplementation databaseUser = null;

    private UserManagerImplementation(){
        
    }

    static public UserManagerImplementation getUserManager() {
        if (databaseUser == null)
            databaseUser = new UserManagerImplementation();
        return databaseUser;
    }

    public EmailCheckResponse isEmailAlreadyRegistered(String email) {
        Properties properties = new Properties();
        String message;
    
        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            try (InputStream resourceStream = loader.getResourceAsStream(fileName)) {
                if (resourceStream == null) {
                    Logger.getLogger("CheckalreadyRegisteredReader").log(Level.WARNING, "Die properties-Datei {0} wurde nicht gefunden.", fileName);
                    message = "Fehler beim Laden der properties-Datei.";
                    return new EmailCheckResponse(false, message);
                }
                properties.load(resourceStream);
            }
    
            int i = 1;
            while (true) {
                String userEmailKey = "User." + i + ".Email";
                String currentUserEmail = properties.getProperty(userEmailKey);
    
                // Überprüfen, ob die aktuelle E-Mail der gesuchten E-Mail entspricht
                if (currentUserEmail == null) {
                    break; // Breche die Schleife, wenn keine weiteren Benutzer vorhanden sind
                }
    
                if (currentUserEmail.equalsIgnoreCase(email)) {
                    message = "Email ist bereits registriert";
                    return new EmailCheckResponse(true, message);
                }
                i++; // Nächsten Benutzer prüfen
            }
    
            message = "Email ist noch nicht registriert.";
            return new EmailCheckResponse(false, message);
    
        } catch (IOException e) {
            Logger.getLogger("CheckalreadyRegisteredReader").log(Level.SEVERE, "Fehler beim Laden der properties-Datei.", e);
            message = "Fehler beim Laden der properties-Datei.";
            return new EmailCheckResponse(false, message);
        }
    }    

    public User getUserProfile(String email) {
        Properties properties = new Properties();
        User user = null;
        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            try (InputStream resourceStream = loader.getResourceAsStream(fileName)) {
                if (resourceStream == null) {
                    Logger.getLogger("GetUserReader").log(Level.WARNING, "Die properties-Datei {0} wurde nicht gefunden.", fileName);
                    return null; // Datei nicht gefunden
                }
                properties.load(resourceStream);
            }
            int i = 1;
            while (true) {
                String userEmailKey = "User." + i + ".Email";
                String currentUserEmail = properties.getProperty(userEmailKey);

                // Überprüfen, ob die aktuelle E-Mail der gesuchten E-Mail entspricht
                if (currentUserEmail.equalsIgnoreCase(email)) {
                    String firstName = properties.getProperty("User." + i + ".Firstname");
                    String lastName = properties.getProperty("User." + i + ".Lastname");
                    String password = properties.getProperty("User." + i + ".Password");
                    Double budget = Double.valueOf(properties.getProperty("User." + i + ".Budget"));

                    user = new User(firstName, lastName, currentUserEmail, password, budget);
                    Logger.getLogger("GetUserReader").log(Level.INFO, "Benutzer gefunden: {0}", user);
                    break; // Benutzer gefunden, Schleife verlassen
                }
                i++; // Nächsten Benutzer prüfen
            }
        } catch (IOException e) {
            Logger.getLogger("GetUserReader").log(Level.SEVERE, "Fehler beim Laden der properties-Datei.", e);
        }
        return user;
    }


    public void addUser(User user) {
        Properties properties = new Properties();
        
        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            try (InputStream resourceStream = loader.getResourceAsStream(fileName)) {
                if (resourceStream == null) {
                    Logger.getLogger("GetUserReader").log(Level.WARNING, "Die properties-Datei {0} wurde nicht gefunden.", fileName);
                    //return -1; // Datei nicht gefunden
                }
                properties.load(resourceStream);
            }
        } catch (IOException e) {
            Logger.getLogger("GetUserReader").log(Level.SEVERE, "Fehler beim Laden der properties-Datei.", e);
        }    
        int nextUserId = getNextUserId(properties);

        // Erstelle die Schlüssel-Wert-Paare für den neuen Benutzer
        properties.setProperty("User." + nextUserId + ".Firstname", user.getFirstName());
        properties.setProperty("User." + nextUserId + ".Lastname", user.getLastName());
        properties.setProperty("User." + nextUserId + ".Email", user.getEmail());
        properties.setProperty("User." + nextUserId + ".Password", passwordManager.hashPassword(user.getPassword()));
        properties.setProperty("User." + nextUserId + ".Budget", user.getBudget().toString());

        try {
            properties.store(new FileOutputStream(fileName), null);
        } catch (IOException e) {
            Logger.getLogger("SetNewUserWriter").log(Level.INFO, "File can not be written to disk");
        }
    }

    // Methode zum Ermitteln der nächsten User-ID
    private int getNextUserId(Properties properties) {
        int maxId = 0;
        for (String key : properties.stringPropertyNames()) {
            if (key.startsWith("User.") && key.endsWith(".Firstname")) {
                // Extrahiere die ID zwischen "User." und ".Firstname"
                try {
                    int id = Integer.parseInt(key.split("\\.")[1]);
                    maxId = Math.max(maxId, id);
                } catch (NumberFormatException e) {
                    Logger.getLogger("UserManager").log(Level.WARNING, "Ungültige ID in Properties-Datei: " + key, e);
                }
            }
        }
        return maxId + 1;
    }

    public boolean editUser(String currentEmail, User user) {
         Properties propertiesU = new Properties();
    
        try {
            ClassLoader loaderU = Thread.currentThread().getContextClassLoader();
            try (InputStream resourceStream = loaderU.getResourceAsStream(fileName)) {
                if (resourceStream == null) {
                    Logger.getLogger("EditUserUsers").log(Level.WARNING, "Die properties-Datei {0} wurde nicht gefunden.", fileName);
                    return false;
                }
                propertiesU.load(resourceStream);
            }

            Properties propertiesT = new Properties();

            ClassLoader loaderT = Thread.currentThread().getContextClassLoader();
            try (InputStream resourceStream = loaderT.getResourceAsStream(transactionFile)) {
                if (resourceStream == null) {
                    Logger.getLogger("EditUserTransactions").log(Level.WARNING, "Die properties-Datei {0} wurde nicht gefunden.", fileName);
                    return false;
                }
                propertiesT.load(resourceStream);
            }

            Properties propertiesS = new Properties();

            ClassLoader loaderS = Thread.currentThread().getContextClassLoader();
            try (InputStream resourceStream = loaderS.getResourceAsStream(sessionsFile)) {
                if (resourceStream == null) {
                    Logger.getLogger("EditUserSessions").log(Level.WARNING, "Die properties-Datei {0} wurde nicht gefunden.", fileName);
                    return false;
                }
                propertiesS.load(resourceStream);
            }

            Properties propertiesP = new Properties();

            ClassLoader loaderP = Thread.currentThread().getContextClassLoader();
            try (InputStream resourceStream = loaderP.getResourceAsStream(portoliosFile)) {
                if (resourceStream == null) {
                    Logger.getLogger("EditUserPortfolios").log(Level.WARNING, "Die properties-Datei {0} wurde nicht gefunden.", fileName);
                    return false;
                }
                propertiesP.load(resourceStream);
            }

        int i = 1;
        while (true){
            String emailKey = "User." + i + ".Email";
            String currentUserEmailUsers = propertiesU.getProperty(emailKey);

            if(currentUserEmailUsers == null){
                return false;
            }

            if(currentUserEmailUsers.equals(currentEmail)){
                String newEmail = user.getEmail();
                String newFirstname = user.getFirstName();
                String newLastname = user.getLastName();
                String newPassword = user.getPassword();
                String newBudget = user.getBudget().toString();
                String firstnameKey = "User." + i + ".Firstname";
                String lastnameKey = "User." + i + ".Lastname";
                String passwordKey = "User." + i + ".Password";
                String budgetKey = "User." + i + ".Budget";
                
                propertiesU.setProperty(firstnameKey, newFirstname);
                propertiesU.setProperty(lastnameKey, newLastname);
                propertiesU.setProperty(passwordKey, newPassword);
                propertiesU.setProperty(budgetKey, newBudget);

                if(!currentEmail.equalsIgnoreCase(newEmail)){
                   int t = 1;
                   while (true){
                    String transEmailKey = "Transaction." + t + ".Email";
                    String currentUserEmailTrans = propertiesT.getProperty(transEmailKey);

                    if(currentUserEmailTrans == null){
                        break;
                    }

                    if(currentUserEmailTrans.equals(currentEmail)){
                        propertiesT.setProperty(transEmailKey, newEmail);

                    }
                    t++;

                   }
                    int s = 1;
                   while (true){
                    String sessionEmailKey = "Session." + s + ".Email";
                    String currentUserEmailSession = propertiesS.getProperty(sessionEmailKey);

                    if(currentUserEmailSession == null){
                        break;
                    }

                    if(currentUserEmailSession.equals(currentEmail)){
                        propertiesS.setProperty(sessionEmailKey, newEmail);

                    }
                    s++;

                   }
                    int p = 1;
                   while (true){
                    String portfolioEmailKey = "Portfolio." + p + ".Email";
                    String currentUserEmailPortfolio = propertiesP.getProperty(portfolioEmailKey);

                    if(currentUserEmailPortfolio == null){
                        break;
                    }

                    if(currentUserEmailPortfolio.equals(currentEmail)){
                        propertiesP.setProperty(portfolioEmailKey, newEmail);

                    }
                    p++;

                   }
                   propertiesU.setProperty(emailKey, newEmail);
                }
            }
            i++;

            return true;
        }
        

     } catch (IOException e) {
            Logger.getLogger("EditUser").log(Level.SEVERE, "Fehler beim Laden der properties-Datei.", e);
        }
        return false;
    }
        // change the data of user, found by id
        // String editUser_database_query = "UPDATE Users" +
        //         "       SET FirstName = '" + user.getFirstName() + "', LastName = '" + user.getLastName() +
        //                 "', EMail = '" + user.getEmail() + "', Password = '" + user.getPassword() + "'" +
        //         "       WHERE UserID = " + user.getUserID() + ";";
        //Write into database
    

    @Override
    public boolean deleteUser(int userID) {
        boolean deleted = true;
        // sql strings to delete the user and the connections, if he uses carpools (foreign key)
        // String deleteUser_database_query = "DELETE FROM Users" +
        //         "       WHERE UserID = " + userID + ";";
        //Delete from database
        return deleted;
    }

}