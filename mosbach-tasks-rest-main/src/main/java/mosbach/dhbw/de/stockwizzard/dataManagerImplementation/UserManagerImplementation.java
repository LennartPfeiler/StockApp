package mosbach.dhbw.de.stockwizzard.dataManagerImplementation;

import java.io.*;
import mosbach.dhbw.de.stockwizzard.dataManager.IUserManager;
import mosbach.dhbw.de.stockwizzard.model.User;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserManagerImplementation implements IUserManager{

    private String fileName = "users.properties";

    static UserManagerImplementation databaseUser = null;

    private UserManagerImplementation(){
        
    }

    static public UserManagerImplementation getUserManager() {
        if (databaseUser == null)
            databaseUser = new UserManagerImplementation();
        return databaseUser;
    }

    public User getUserProfile(int userID) {
        Properties properties = new Properties();
        User user = null;
        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            try (InputStream resourceStream = loader.getResourceAsStream(fileName)) {
                if (resourceStream == null) {
                    logger.log(Level.WARNING, "Die properties-Datei {0} wurde nicht gefunden.", fileName);
                    return null; // Datei nicht gefunden
                }
                properties.load(resourceStream);
            }

            int i = 1; // Start mit User.1
            while (true) {
                String userIdKey = "User." + i + ".UserID";
                String currentUserId = properties.getProperty(userIdKey);
                if (currentUserId == null) {
                    logger.log(Level.INFO, "Kein weiterer Benutzer gefunden (User.{0}.UserID).", i);
                    break; // Breche die Schleife ab, wenn kein Benutzer mehr gefunden wird
                }

                if (Integer.parseInt(currentUserId) == userID) {
                    String firstName = properties.getProperty("User." + i + ".Firstname");
                    String lastName = properties.getProperty("User." + i + ".LastName");
                    String email = properties.getProperty("User." + i + ".Email");
                    String password = properties.getProperty("User." + i + ".Password");

                    user = new User(userID, firstName, lastName, email, password);
                    logger.log(Level.INFO, "Benutzer gefunden: {0}", user);
                    break; // Benutzer gefunden, Schleife verlassen
                }
                i++; // Nächsten Benutzer prüfen
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Fehler beim Laden der properties-Datei.", e);
        }
        return user;
    }


    public int addUser(User user) {
        int userID = 0;
        // write the data into database
        String addUser_database_query = "Insert into User (FirstName, LastName, EMail, Password) Values ('" + user.getFirstName() + "', '" + user.getLastName() + "', '" + user.getEmail()  +"', '" + user.getPassword() + "');";
        //Write into database
        return userID;
    }

    public boolean editUser(User user) {
        boolean edited = true;
        // change the data of user, found by id
        String editUser_database_query = "UPDATE Users" +
                "       SET FirstName = '" + user.getFirstName() + "', LastName = '" + user.getLastName() +
                        "', EMail = '" + user.getEmail() + "', Password = '" + user.getPassword() + "'" +
                "       WHERE UserID = " + user.getUserID() + ";";
        //Write into database
        return edited;
    }

    @Override
    public boolean deleteUser(int userID) {
        boolean deleted = true;
        // sql strings to delete the user and the connections, if he uses carpools (foreign key)
        String deleteUser_database_query = "DELETE FROM Users" +
                "       WHERE UserID = " + userID + ";";
        //Delete from database
        return deleted;
    }

}