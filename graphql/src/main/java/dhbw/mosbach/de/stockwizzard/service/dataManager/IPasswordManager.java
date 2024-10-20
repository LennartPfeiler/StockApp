package dhbw.mosbach.de.stockwizzard.service.dataManager;

public interface IPasswordManager {

    public String hashPassword(String password);

    public Boolean checkPassword(String password, String hashed);
}
