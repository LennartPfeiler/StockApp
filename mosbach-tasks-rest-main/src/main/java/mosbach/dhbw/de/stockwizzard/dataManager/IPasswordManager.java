package mosbach.dhbw.de.stockwizzard.dataManager;

public interface IPasswordManager {
    
    public String hashPassword(String password);

    // Überprüfe ein Passwort
    public boolean checkPassword(String password, String hashed);
}
