package mosbach.dhbw.de.stockwizzard.dataManagerImplementation;

import java.io.*;
import mosbach.dhbw.de.stockwizzard.dataManager.IUserManager;
import mosbach.dhbw.de.stockwizzard.dataManagerImplementation.PasswordManagerImplementation;
import mosbach.dhbw.de.stockwizzard.model.User;
import mosbach.dhbw.de.stockwizzard.model.EmailCheckResponse;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserManagerImplementation implements IUserManager{

    private String fileName = "users.properties";
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

    public boolean editUser(User user) {
        boolean edited = true;
        // change the data of user, found by id
        // String editUser_database_query = "UPDATE Users" +
        //         "       SET FirstName = '" + user.getFirstName() + "', LastName = '" + user.getLastName() +
        //                 "', EMail = '" + user.getEmail() + "', Password = '" + user.getPassword() + "'" +
        //         "       WHERE UserID = " + user.getUserID() + ";";
        //Write into database
        return edited;
    }

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