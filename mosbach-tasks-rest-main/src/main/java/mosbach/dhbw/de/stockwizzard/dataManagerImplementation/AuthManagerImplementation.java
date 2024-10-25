package mosbach.dhbw.de.stockwizzard.dataManagerImplementation;

import java.util.UUID;
import mosbach.dhbw.de.stockwizzard.dataManager.IAuthManager;

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