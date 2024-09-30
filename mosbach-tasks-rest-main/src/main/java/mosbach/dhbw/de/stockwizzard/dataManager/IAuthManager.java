package mosbach.dhbw.de.stockwizzard.dataManager;

public interface IAuthManager{

    public String generateToken();

    public void generateSession();
}