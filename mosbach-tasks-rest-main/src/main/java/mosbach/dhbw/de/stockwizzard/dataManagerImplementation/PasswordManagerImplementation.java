package mosbach.dhbw.de.stockwizzard.dataManagerImplementation;
import org.mindrot.jbcrypt.BCrypt;

public class PasswordManagerImplementation {
    
    static PasswordManagerImplementation passwordManager = null;

    private PasswordManagerImplementation(){
        
    }

    static public PasswordManagerImplementation getPasswordManager() {
        if (passwordManager == null)
        passwordManager = new PasswordManagerImplementation();
        return passwordManager;
    }

    public String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    // Überprüfe ein Passwort
    public boolean checkPassword(String password, String hashed) {
        return BCrypt.checkpw(password, hashed);
    }
}
