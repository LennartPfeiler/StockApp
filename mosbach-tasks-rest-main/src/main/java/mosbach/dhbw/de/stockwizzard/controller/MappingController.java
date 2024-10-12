package mosbach.dhbw.de.stockwizzard.controller;

import org.springframework.web.bind.annotation.*;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.http.*;
import mosbach.dhbw.de.stockwizzard.dataManagerImplementation.AuthManagerImplementation;
import mosbach.dhbw.de.stockwizzard.dataManagerImplementation.PasswordManagerImplementation;
import mosbach.dhbw.de.stockwizzard.dataManagerImplementation.PortfolioManagerImplementation;
import mosbach.dhbw.de.stockwizzard.dataManagerImplementation.PortfolioStockManagerImplementation;
import mosbach.dhbw.de.stockwizzard.dataManagerImplementation.UserManagerImplementation;
import mosbach.dhbw.de.stockwizzard.dataManagerImplementation.SessionManagerImplementation;
import mosbach.dhbw.de.stockwizzard.dataManagerImplementation.StockManagerImplementation;
import mosbach.dhbw.de.stockwizzard.dataManagerImplementation.TransactionManagerImplementation;
import mosbach.dhbw.de.stockwizzard.model.LoginRequest;
import mosbach.dhbw.de.stockwizzard.model.LogoutRequest;
import mosbach.dhbw.de.stockwizzard.model.StringAnswer;
import mosbach.dhbw.de.stockwizzard.model.TokenUser;
import mosbach.dhbw.de.stockwizzard.model.User;
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
    StockManagerImplementation stockManager = StockManagerImplementation.getStockManager();
    PortfolioStockManagerImplementation portfolioStockManager = PortfolioStockManagerImplementation.getPortfolioStockManager();

    @PostMapping(
            path = "/auth",
            consumes = {MediaType.APPLICATION_JSON_VALUE}
    )
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest){
        String email = loginRequest.getEmail();
        String password = loginRequest.getPassword();
        System.err.println(email);
        //Überprüfen, ob der Benutzer in der Datenbank existiert
        User user = userManager.getUserProfile(email);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new StringAnswer("Please register first!"));
        }

        // Passwortüberprüfung
        if (!passwordManager.checkPassword(password, user.getPassword())) {
            // Falsches Passwort
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new StringAnswer("Incorrect email or password!"));
        }

        String token = authManager.generateToken();
        sessionManager.createSession(user.getEmail(), token);
        // Erstelle eine TokenTask-Instanz mit dem generierten Token und dem Benutzer
        TokenUser tokenUser = new TokenUser(token, user);

        // Erfolgreiche Anmeldung: Antwort mit TokenTask zurückgeben
        return ResponseEntity.ok(tokenUser);
    }

    @DeleteMapping(
        path = "/auth",
        consumes = {MediaType.APPLICATION_JSON_VALUE}
)
public ResponseEntity<?> logout(@RequestBody LogoutRequest logoutRequest){
    try{
        String email = logoutRequest.getEmail();
        String token = logoutRequest.getToken();
        Boolean isValid = sessionManager.validToken(token, email);
        if (isValid) {
            sessionManager.deleteSession(email, token);
            return ResponseEntity.ok(new StringAnswer("Logout successfully!"));
        }
        else{
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new StringAnswer("Unauthorized for this logout!"));
        }
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new StringAnswer("An unexpected error occurred during registration."));
    }
}
    
    @PostMapping(
            path = "/user",
            consumes = {MediaType.APPLICATION_JSON_VALUE}
    )
    public ResponseEntity<?> createUser(@RequestBody User user) {
        try {
            // Überprüfen, ob die E-Mail bereits registriert ist
            Boolean isRegistered = userManager.isEmailAlreadyRegistered(user.getEmail());
            if (isRegistered) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new StringAnswer("You are already registered!"));
            } else {
                // Neuen Benutzer hinzufügen
                userManager.addUser(new User(user.getFirstName(), user.getLastName(), user.getEmail(), user.getPassword(), user.getBudget()));
                // Neuen Portfolio hinzufügen
                portfolioManager.addPortfolio(new Portfolio(null, user.getBudget(), user.getBudget(), user.getEmail()));
                return ResponseEntity.ok(new StringAnswer("User successfully registered"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new StringAnswer("An unexpected error occurred during registration."));
        }
    }
    
    

    @GetMapping("/session")
    public Session getSession(@RequestParam(value = "email", defaultValue = "") String email) {
        return sessionManager.getSession(email);   
    }

    @GetMapping("/user")
    public User getUserProfile(@RequestParam(value = "email", defaultValue = "") String email) {
        return userManager.getUserProfile(email);   
    }

    @GetMapping("/portfolio")
    public Portfolio getUserPortfolio(@RequestParam(value = "email", defaultValue = "") String email) {
        return portfolioManager.getUserPortfolio(email);   
    }

    @GetMapping("/transaction")
    public Transaction getTransaction(@RequestParam(value = "transactionID", defaultValue = "") Integer transactionID) {
        userManager.createUserTable();
        sessionManager.createSessionTable();
        portfolioManager.createPortfolioTable();
        stockManager.createStockTable();
        portfolioStockManager.createPortfolioStockTable();
        portfolioManager.createPortfolioTable();
        transactionManager.createTransactionTable();
        stockManager.createStockTable();
        //return transactionManager.getTransaction(transactionID);
        return new Transaction(null, null, null, null, null, null, null, null);   
    }

    // @PutMapping(
    //         path = "/user",
    //         consumes = {MediaType.APPLICATION_JSON_VALUE}
    // ) 
    // public ResponseEntity<?> editUser(@RequestBody EditRequest editRequest){
    //     String token = editRequest.getToken();
    //     String currentEmail = editRequest.getCurrentmail();
    //     User user = editRequest.getUser();

    //      if (token != null && currentEmail != null) {
    //     // Führe Validierung oder eine weitere Aktion durch
    //     // Beispiel: Prüfen, ob der Token gültig ist
    //     boolean isValid = sessionManager.validToken(token, currentEmail);
    //     if (isValid) {
    //         userManager.editUser(currentEmail, user);
    //         return ResponseEntity.ok("Token gültig"); // Gültiger Token - gib TokenUser zurück
    //     } else {
    //         return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null); // Ungültiger Token - gib Fehlerstatus zurück
    //     }
    //     } else {
    //         return ResponseEntity.badRequest().body(null); // Ungültige Anfrage, falls Token oder Email fehlen
    //     }
    // }

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
                Portfolio userPortfolio = portfolioManager.getUserPortfolio(transactionContent.getEmail());
                //PortfolioStock hinzufügen
                portfolioStockManager.addPortfolioStock(userPortfolio.getPortfolioID(), transactionContent.getSymbol(), transactionContent.getStockAmount(), transactionContent.getPricePerStock());
                
                //Budget ändern
                userManager.editUserBudget(currentUser.getEmail(), currentUser.getBudget(), transactionContent.getTotalPrice());
                //Portfoliowert ändern
                //portfolioManager.editPortfolioValue(userPortfolio.getPortfolioID(), userPortfolio.getValue(), transactionContent.getTotalPrice());
                return ResponseEntity.ok(new StringAnswer("Transaction was successfully completed"));
            }
            else{
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new StringAnswer("Not enough budget for this transaction!"));
            }
        }
        else{
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new StringAnswer("Unauthorized for this transaction!"));
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