package dhbw.mosbach.de.stockwizzard.service.dataManager;

import dhbw.mosbach.de.stockwizzard.model.Session;

public interface ISessionManager {

    public void createSessionTable();

    public void createSession(String email, String token);

    public Session getSession(String email);

    public void deleteSession(String email, String token);

    public Boolean validToken(String token, String email);

    public void editSession(String email, String newEmail);
}