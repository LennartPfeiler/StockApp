package mosbach.dhbw.de.stockwizzard.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.http.*;
import mosbach.dhbw.de.stockwizzard.dataManagerImplementation.AuthManagerImplementation;
import mosbach.dhbw.de.stockwizzard.dataManagerImplementation.PasswordManagerImplementation;
import mosbach.dhbw.de.stockwizzard.dataManagerImplementation.PortfolioManagerImplementation;
import mosbach.dhbw.de.stockwizzard.dataManagerImplementation.UserManagerImplementation;
import mosbach.dhbw.de.stockwizzard.dataManagerImplementation.SessionManagerImplementation;
import mosbach.dhbw.de.stockwizzard.dataManagerImplementation.TransactionManagerImplementation;
import mosbach.dhbw.de.stockwizzard.model.LoginRequest;
import mosbach.dhbw.de.stockwizzard.model.StringAnswer;
import mosbach.dhbw.de.stockwizzard.model.TokenUser;
import mosbach.dhbw.de.stockwizzard.model.User;
import mosbach.dhbw.de.stockwizzard.model.EmailCheckResponse;
import mosbach.dhbw.de.stockwizzard.model.Portfolio;
import mosbach.dhbw.de.stockwizzard.model.Session;
import mosbach.dhbw.de.stockwizzard.model.EditRequest;
import mosbach.dhbw.de.stockwizzard.model.TokenTransactionContent;
import mosbach.dhbw.de.stockwizzard.model.Transaction;
import mosbach.dhbw.de.stockwizzard.model.TransactionContent;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/api")
public class MappingController {

    TransactionManagerImplementation transactionManager = TransactionManagerImplementation.getTransactionManager();
    UserManagerImplementation userManager = UserManagerImplementation.getUserManager();
    AuthManagerImplementation authManager = AuthManagerImplementation.getAuthManager();
    PortfolioManagerImplementation portfolioManager = PortfolioManagerImplementation.getPortfolioManager();
    PasswordManagerImplementation passwordManager = PasswordManagerImplementation.getPasswordManager();
    SessionManagerImplementation sessionManager = SessionManagerImplementation.getSessionManager();
    @PostMapping(
            path = "/auth",
            consumes = {MediaType.APPLICATION_JSON_VALUE}
    )
    public ResponseEntity<TokenUser> login(@RequestBody LoginRequest loginRequest){
        String email = loginRequest.getEmail();
        String password = loginRequest.getPassword();
        System.err.println(email);
        //Überprüfen, ob der Benutzer in der Datenbank existiert
        User user = userManager.getUserProfile(email);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        // Passwortüberprüfung
        if (!passwordManager.checkPassword(password, user.getPassword())) {
            // Falsches Passwort
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = authManager.generateToken();
        sessionManager.createSession(user.getEmail(), token);
        //authManager.generateSession();
        // Erstelle eine TokenTask-Instanz mit dem generierten Token und dem Benutzer
        TokenUser tokenUser = new TokenUser(token, user);

        // Erfolgreiche Anmeldung: Antwort mit TokenTask zurückgeben
        return ResponseEntity.ok(tokenUser);
    }
    
    @PostMapping(
        path = "/user",
        consumes = {MediaType.APPLICATION_JSON_VALUE}
)
    public ResponseEntity<StringAnswer> createUser(@RequestBody User user){
        EmailCheckResponse mailResponse = userManager.isEmailAlreadyRegistered(user.getEmail());
        if(mailResponse.isRegistered() == true){
            //return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Der Benutzer ist bereits registriert.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        else{
            if(mailResponse.isRegistered() == false && mailResponse.getMessage().equals("Email ist noch nicht registriert.")){
                userManager.addUser(new User(user.getFirstName(), user.getLastName(), user.getEmail(), user.getPassword(), user.getBudget()));
                portfolioManager.addPortfolio(new Portfolio(null, user.getBudget(), user.getEmail()));
                StringAnswer sA = new StringAnswer();
                sA.setAnswer("User successfully registered");
                return ResponseEntity.ok(sA);
            }
            else{
                //return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Fehler beim registrieren.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        }
    }

    @GetMapping("/session")
    public Session getSession(@RequestParam(value = "email", defaultValue = "") String email) {
        return sessionManager.getSession(email);   
    }

    @GetMapping("/user")
    public User getUserProfile(@RequestParam(value = "email", defaultValue = "") String email) {
        usermanager.createTable();
        return userManager.getUserProfile(email);   
    }

    @GetMapping("/portfolio")
    public Portfolio getUserPortfolio(@RequestParam(value = "email", defaultValue = "") String email) {
        return portfolioManager.getUserPortfolio(email);   
    }

    @GetMapping("/transaction")
    public Transaction getTransaction(@RequestParam(value = "transactionID", defaultValue = "") Integer transactionID) {
        return transactionManager.getTransaction(transactionID);  
    }

    @PutMapping(
            path = "/user",
            consumes = {MediaType.APPLICATION_JSON_VALUE}
    ) 
    public ResponseEntity<?> editUser(@RequestBody EditRequest editRequest){
        String token = editRequest.getToken();
        String currentEmail = editRequest.getCurrentmail();
        User user = editRequest.getUser();

         if (token != null && currentEmail != null) {
        // Führe Validierung oder eine weitere Aktion durch
        // Beispiel: Prüfen, ob der Token gültig ist
        boolean isValid = sessionManager.validToken(token, currentEmail);
        if (isValid) {
            userManager.editUser(currentEmail, user);
            return ResponseEntity.ok("Token gültig"); // Gültiger Token - gib TokenUser zurück
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null); // Ungültiger Token - gib Fehlerstatus zurück
        }
        } else {
            return ResponseEntity.badRequest().body(null); // Ungültige Anfrage, falls Token oder Email fehlen
        }
    }

    @PostMapping(
            path = "/order/buy",
            consumes = {MediaType.APPLICATION_JSON_VALUE}
    ) 
    public ResponseEntity<?> createOrder(@RequestBody TokenTransactionContent tokenTransactionContent){
        String token = tokenTransactionContent.getToken();
        TransactionContent transactionContent = tokenTransactionContent.getTransactionContent();

        Boolean isValid = sessionManager.validToken(token, transactionContent.getEmail());
        if (isValid) {
            User currentUser = userManager.getUserProfile(transactionContent.getEmail());
            Boolean enoughBudget = userManager.CheckIfEnoughBudgetLeft(transactionContent.getTotalPrice(), currentUser);
            if(enoughBudget == true){
                transactionManager.addTransaction(transactionContent);
                return ResponseEntity.ok("Token gültig");
            }
            else{
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        }
        else{
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
        
        
        




//     @PostMapping(                                                                               
//             path = "/user",
//             consumes = {MediaType.APPLICATION_JSON_VALUE}
//     )
//     @ResponseStatus(HttpStatus.OK)
//     public User createUser(@RequestBody User user) {
//         DatabaseUserImplementation db_user = DatabaseUserImplementation.getDatabaseUser();
//         User newUser = new User();
//         newUser.setUserID(db_user.addUser(user));
//         return newUser;
//     }

//     @PutMapping(                                                                                
//             path = "/user",
//             consumes = {MediaType.APPLICATION_JSON_VALUE}
//     )
//     @ResponseStatus(HttpStatus.OK)
//     public StringAnswer editUser(@RequestBody User user) {
//         DatabaseUserImplementation db_user = DatabaseUserImplementation.getDatabaseUser();
//         boolean success = db_user.editUser(user);
//         if (success) return new StringAnswer("success");
//         else return new StringAnswer("failure");
//     }

//     @DeleteMapping(path = "/user")                                                              
//     public StringAnswer deleteUser(@RequestParam (value = "userID", defaultValue = "0") int userID) {
//         DatabaseUserImplementation db_user = DatabaseUserImplementation.getDatabaseUser();
//         boolean success = db_user.deleteUser(userID);
//         if (success) return new StringAnswer("success");
//         else return new StringAnswer("failure");
//     }
}