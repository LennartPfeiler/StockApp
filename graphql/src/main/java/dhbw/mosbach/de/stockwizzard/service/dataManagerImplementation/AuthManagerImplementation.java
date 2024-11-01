package dhbw.mosbach.de.stockwizzard.service.dataManagerImplementation;

import java.io.*;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.UUID;
import dhbw.mosbach.de.stockwizzard.service.dataManager.IAuthManager;

public class AuthManagerImplementation implements IAuthManager{

    private static AuthManagerImplementation databaseUser = null;

    private AuthManagerImplementation(){
        
    }

    static public AuthManagerImplementation getAuthManager() {
        if (databaseUser == null)
            databaseUser = new AuthManagerImplementation();
        return databaseUser;
    }


    public String generateToken(){
        UUID uuid = UUID.randomUUID();
        String uuidAsString = uuid.toString();
        return uuidAsString;
    }

}