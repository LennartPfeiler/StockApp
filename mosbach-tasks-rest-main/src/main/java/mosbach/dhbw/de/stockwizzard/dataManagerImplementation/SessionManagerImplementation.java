package mosbach.dhbw.de.stockwizzard.dataManagerImplementation;

import java.io.*;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.UUID;
import mosbach.dhbw.de.stockwizzard.dataManager.ISessionManager;
import mosbach.dhbw.de.stockwizzard.model.Portfolio;
import mosbach.dhbw.de.stockwizzard.model.Session;

public class SessionManagerImplementation implements ISessionManager{

    private String fileName = "sessions.properties";

    static SessionManagerImplementation databaseUser = null;

    private SessionManagerImplementation(){
        
    }

    static public SessionManagerImplementation getSessionManager() {
        if (databaseUser == null)
            databaseUser = new SessionManagerImplementation();
        return databaseUser;
    }


    public void createSession(String email, String token){
        Properties properties = new Properties();
        
        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            try (InputStream resourceStream = loader.getResourceAsStream(fileName)) {
                if (resourceStream == null) {
                    Logger.getLogger("AddSessionWriter").log(Level.WARNING, "Die properties-Datei {0} wurde nicht gefunden.", fileName);
                    //return -1; // Datei nicht gefunden
                }
                properties.load(resourceStream);
            }
        } catch (IOException e) {
            Logger.getLogger("AddSessionWriter").log(Level.SEVERE, "Fehler beim Laden der properties-Datei.", e);
        }    

        int nextSessionId = getNextSessionId(properties);
        // Erstelle die Schlüssel-Wert-Paare für den neuen Benutzer
        properties.setProperty("Session." + nextSessionId + ".Email", email);
        properties.setProperty("Session." + nextSessionId + ".Token", token);
        Logger.getLogger("AddSessionWriter").log(Level.INFO, "{0} und {1}", new Object[]{email , token});
        Logger.getLogger("AddSessionWriter").log(Level.INFO, "token nr: {0}", nextSessionId);
        try {
            properties.store(new FileOutputStream(fileName), null);
            Logger.getLogger("AddSessionWriter").log(Level.INFO, "worked");
        } catch (IOException e) {
            Logger.getLogger("AddSessionWriter").log(Level.INFO, "File can not be written to disk");
        }
    }
    
    public Session getSession(String email) {
        Properties properties = new Properties();
        Session session = null;
        try {
            // Lade die properties-Datei
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            try (InputStream resourceStream = loader.getResourceAsStream(fileName)) {
                if (resourceStream == null) {
                    Logger.getLogger("GetSessionReader").log(Level.WARNING, "Die properties-Datei {0} wurde nicht gefunden.", fileName);
                    return null; // Datei nicht gefunden
                }
                properties.load(resourceStream);
            }
    
            int i = 1;
            while (true) {
                // E-Mail-Adresse des aktuellen Portfolios abrufen
                String userEmailKey = "Session." + i + ".Email";
                String currentUserEmail = properties.getProperty(userEmailKey);
    
                // Überprüfen, ob weitere Einträge vorhanden sind
                if (currentUserEmail == null) {
                    Logger.getLogger("GetSessionReader").log(Level.INFO, "Keine weiteren Portfolios gefunden. Abbruch bei Index: {0}", i);
                    break; // Kein weiterer Eintrag vorhanden, Schleife verlassen
                }
    
                // Überprüfen, ob die aktuelle E-Mail der gesuchten E-Mail entspricht
                if (currentUserEmail.equalsIgnoreCase(email)) {
                    try {
                        String token = properties.getProperty("Session." + i + ".Token");
                        session = new Session(email, token);
                        Logger.getLogger("GetSessionReader").log(Level.INFO, "Session gefunden: {0}", session);
                        break; // Benutzer gefunden, Schleife verlassen
                    } catch (NumberFormatException e) {
                        Logger.getLogger("GetSessionReader").log(Level.WARNING, "Fehler bei der Konvertierung der Portfolio-Daten.", e);
                        return null; // Abbruch, da die Daten fehlerhaft sind
                    }
                }
                i++; // Nächsten Benutzer prüfen
            }
        } catch (IOException e) {
            Logger.getLogger("GetSessionReader").log(Level.SEVERE, "Fehler beim Laden der properties-Datei.", e);
        }
    
        return session;
    }

    public void deleteSession(String email, String token){

    }

    private int getNextSessionId(Properties properties) {
        int maxId = 0;
        for (String key : properties.stringPropertyNames()) {
            if (key.startsWith("Session.") && key.endsWith(".Email")) {
                // Extrahiere die ID zwischen "User." und ".Firstname"
                try {
                    int id = Integer.parseInt(key.split("\\.")[1]);
                    maxId = Math.max(maxId, id);
                } catch (NumberFormatException e) {
                    Logger.getLogger("SessionManager").log(Level.WARNING, "Ungültige Session in Properties-Datei: " + key, e);
                }
            }
        }
        return maxId + 1;
    }
}