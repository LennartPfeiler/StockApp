package mosbach.dhbw.de.stockwizzard.dataManager;

public interface ISessionManager{

    public void createSessionTable();

    public void createSession(String email, String token);

    public void deleteSession(String email, String token);

    public boolean validToken(String token, String email);
}