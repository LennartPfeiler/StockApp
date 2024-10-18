package mosbach.dhbw.de.stockwizzard.controller;

import org.springframework.web.bind.annotation.*;

import java.sql.Connection;
import java.util.List;
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
import mosbach.dhbw.de.stockwizzard.model.TokenEmail;
import mosbach.dhbw.de.stockwizzard.model.TokenUser;
import mosbach.dhbw.de.stockwizzard.model.User;
import mosbach.dhbw.de.stockwizzard.model.Portfolio;
import mosbach.dhbw.de.stockwizzard.model.PortfolioStock;
import mosbach.dhbw.de.stockwizzard.model.PortfolioStockValue;
import mosbach.dhbw.de.stockwizzard.model.Stock;
import mosbach.dhbw.de.stockwizzard.model.Session;
import mosbach.dhbw.de.stockwizzard.model.AddStockRequest;
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

//////////////////////////////////////////////////////////////Auth Endpoints////////////////////////////////////////////////////////////////////

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
    
//////////////////////////////////////////////////////////////User Endpoints////////////////////////////////////////////////////////////////////

    @GetMapping("/user")
    public ResponseEntity<?> getUserProfile( 
        @RequestParam(value = "email", defaultValue = "") String email,
        @RequestParam(value = "token", defaultValue = "") String token) {
        
        try {
            Boolean isValid = sessionManager.validToken(token, email);
            if (isValid) {
                User user = userManager.getUserProfile(email);
                return ResponseEntity.ok(user);
            }
            else{
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new StringAnswer("Unauthorized for this transaction!"));
            } 
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new StringAnswer("An unexpected error occurred during getting user data."));
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

    // @PutMapping(
    //     path = "/user",
    //     consumes = {MediaType.APPLICATION_JSON_VALUE}
    // ) 
    // public ResponseEntity<?> editUser(@RequestBody EditRequest editRequest){
    //     String token = editRequest.getToken();
    //     User currentUser = userManager.getUserProfile(editRequest.getCurrentmail());
    //     User new_user_data = editRequest.getUser();

    //     if (token != null && currentUser.getEmail() != null) {
    //         Boolean isValid = sessionManager.validToken(token, currentUser.getEmail());
    //         if (isValid) {
    //             userManager.editUser(currentUser, new_user_data);
    //             return ResponseEntity.ok(userManager.getUserProfile(new_user_data.getEmail()));
    //         } else {
    //             return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null); // Ungültiger Token - gib Fehlerstatus zurück
    //         }
    //     } else {
    //         return ResponseEntity.badRequest().body(null); // Ungültige Anfrage, falls Token oder Email fehlen
    //     }
    // }

    @PutMapping(
        path = "/user",
        consumes = {MediaType.APPLICATION_JSON_VALUE}
    ) 
    public ResponseEntity<?> editProfile(@RequestBody EditRequest editRequest){
        try{
            String token = editRequest.getToken();
            User currentUser = userManager.getUserProfile(editRequest.getCurrentmail());
            User new_user_data = editRequest.getUser();

            Boolean isValid = sessionManager.validToken(token, currentUser.getEmail());
            if (isValid) {
                Boolean emailChanged = !new_user_data.getEmail().equals(currentUser.getEmail());
                Portfolio userPortfolio = portfolioManager.getUserPortfolio(currentUser.getEmail());
                    if(emailChanged == false){
                        // Update user
                        userManager.editProfile(currentUser, new_user_data);
                        portfolioManager.editAllPortfolioValues(currentUser.getEmail(), new_user_data.getEmail(), (userPortfolio.getStartValue() + new_user_data.getBudget()), (userPortfolio.getValue() + new_user_data.getBudget()));
                        return ResponseEntity.ok(userManager.getUserProfile(new_user_data.getEmail()));
                    } else {
                        Boolean isRegistered = userManager.isEmailAlreadyRegistered(new_user_data.getEmail());
                        if(isRegistered == false){
                            userManager.addUser(new User(new_user_data.getFirstName(), new_user_data.getLastName(), new_user_data.getEmail(), currentUser.getPassword(), (new_user_data.getBudget() + currentUser.getBudget())));
                            sessionManager.editSession(currentUser.getEmail(), new_user_data.getEmail());
                            transactionManager.editTransactionEmail(currentUser.getEmail(), new_user_data.getEmail());
                            portfolioManager.editAllPortfolioValues(currentUser.getEmail(), new_user_data.getEmail(), (userPortfolio.getStartValue() + new_user_data.getBudget()), (userPortfolio.getValue() + new_user_data.getBudget()));
                            userManager.deleteUser(currentUser.getEmail());
                            return ResponseEntity.ok(userManager.getUserProfile(new_user_data.getEmail()));
                        }
                        else{
                            return ResponseEntity.status(HttpStatus.CONFLICT).body(new StringAnswer("This email is already registered"));
                        }
                    }
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new StringAnswer("Unauthorized for this transaction")); // Ungültiger Token - gib Fehlerstatus zurück
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new StringAnswer("An unexpected error occurred during editing user."));
        } 
    }

    @PutMapping(
            path = "/user/reset",
            consumes = {MediaType.APPLICATION_JSON_VALUE}
    ) 
    public ResponseEntity<?> resetProfile(@RequestBody TokenEmail tokenEmail){
        try{
            String token = tokenEmail.getToken();
            String email = tokenEmail.getEmail();
            User newUser;
            boolean isValid = sessionManager.validToken(token, email);
            if (isValid) {
                    portfolioStockManager.deleteAllPortfolioStocks(email);
                    transactionManager.deleteAllTransactions(email);
                    portfolioManager.resetPortfolio(email);
                    userManager.resetProfile(email);
                return ResponseEntity.ok(userManager.getUserProfile(email));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new StringAnswer("Unauthorized for this transaction!")); 
            }
        } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new StringAnswer("An unexpected error occurred during resetting."));
            } 
    }

    @DeleteMapping(
            path = "/user",
            consumes = {MediaType.APPLICATION_JSON_VALUE}
    ) 
    public ResponseEntity<?> deleteProfile(@RequestBody TokenEmail tokenEmail){
        try{
            String token = tokenEmail.getToken();
            String email = tokenEmail.getEmail();
            boolean isValid = sessionManager.validToken(token, email);
            if (isValid) {
                userManager.deleteUser(email);
                return ResponseEntity.ok(new StringAnswer("Profile successfully deleted"));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new StringAnswer("Unauthorized for this transaction!")); // Ungültiger Token - gib Fehlerstatus zurück
            }
        } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new StringAnswer("An unexpected error occurred during resetting."));
            } 
    }
    
//////////////////////////////////////////////////////////////Stock Endpoints////////////////////////////////////////////////////////////////////
    @GetMapping("/stock")
    public ResponseEntity<?> getStock(
        @RequestParam(value = "email", defaultValue = "") String email,
        @RequestParam(value = "token", defaultValue = "") String token,
        @RequestParam(value = "symbol", defaultValue = "") String symbol) {
        
        try {
            Boolean isValid = sessionManager.validToken(token, email);
            if (isValid) {
                Stock stock = stockManager.getStock(symbol);
                if (stock != null) {
                    return ResponseEntity.ok(stock);
                } else {
                    // Aktie nicht gefunden
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new StringAnswer("Stock is not in the database."));
                }
            } else {
                // Ungültige Authentifizierung
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new StringAnswer("Unauthorized for this transaction!"));
            }
        } catch (Exception e) {
            // Unerwarteter Fehler
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new StringAnswer("An unexpected error occurred during getting stock data."));
        }
    }

    @PostMapping(
            path = "/stock",
            consumes = {MediaType.APPLICATION_JSON_VALUE}
    )
    public ResponseEntity<?> createStock(@RequestBody AddStockRequest addStockRequest) {
        try {
            Boolean isValid = sessionManager.validToken(addStockRequest.getTokenEmail().getToken(), addStockRequest.getTokenEmail().getEmail());
            if (isValid) {
                stockManager.addStock(addStockRequest.getStock());
                return ResponseEntity.ok(new StringAnswer("Stock got added to Database"));
            } else {
                // Ungültige Authentifizierung
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new StringAnswer("Unauthorized for this transaction!"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new StringAnswer("An unexpected error occurred during adding Stock to Database."));
        }
    }

//////////////////////////////////////////////////////////////PortfolioStock Endpoints////////////////////////////////////////////////////////////////////

    @GetMapping("/portfolioStocks")
    public ResponseEntity<?> getAllPortfolioStocks( 
        @RequestParam(value = "email", defaultValue = "") String email,
        @RequestParam(value = "token", defaultValue = "") String token,
        @RequestParam(value = "sortby", defaultValue = "") String sortby) {
        
        try {
            Boolean isValid = sessionManager.validToken(token, email);
            if (isValid) {
                List<PortfolioStock> portfolioStocks = portfolioStockManager.getAllPortfolioStocks(email, sortby);
                return ResponseEntity.ok(portfolioStocks);
            }
            else{
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new StringAnswer("Unauthorized for this transaction!"));
            } 
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new StringAnswer("An unexpected error occurred during getting portfolioStocks."));
        }  
    }

//////////////////////////////////////////////////////////////Transaction Endpoints////////////////////////////////////////////////////////////////////

    @GetMapping("/transactions")
    public ResponseEntity<?> getAllTransactions( 
        @RequestParam(value = "email", defaultValue = "") String email,
        @RequestParam(value = "token", defaultValue = "") String token,
        @RequestParam(value = "sortby", defaultValue = "") String sortby) {
        
        try {
            Boolean isValid = sessionManager.validToken(token, email);
            if (isValid) {
                List<Transaction> transactions = transactionManager.getAllTransactions(email, sortby);
                return ResponseEntity.ok(transactions);
            }
            else{
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new StringAnswer("Unauthorized for this transaction!"));
            } 
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new StringAnswer("An unexpected error occurred during getting transactions."));
        }  
    }

    // @GetMapping("/transaction")
    // public void getTransaction(@RequestParam(value = "transactionID", defaultValue = "") Integer transactionID) {
    //     // userManager.createUserTable();
    //     // sessionManager.createSessionTable();
    //     // portfolioManager.createPortfolioTable();
    //     // stockManager.createStockTable();
    //     // portfolioStockManager.createPortfolioStockTable();
    //     // portfolioManager.createPortfolioTable();
    //     transactionManager.createTransactionTable();
    //     // stockManager.createStockTable();
    //     //return transactionManager.getTransaction(transactionID);
    //     // return new Transaction(null, null, null, null, null, null, null, null, null);   
    // }

//////////////////////////////////////////////////////////////Portfolio Endpoints////////////////////////////////////////////////////////////////////

    @GetMapping("/portfolio")
    public ResponseEntity<?> getUserPortfolio( 
        @RequestParam(value = "email", defaultValue = "") String email,
        @RequestParam(value = "token", defaultValue = "") String token) {
        
        try {
            Boolean isValid = sessionManager.validToken(token, email);
            if (isValid) {
                Portfolio portfolio = portfolioManager.getUserPortfolio(email);
                return ResponseEntity.ok(portfolio);
            }
            else{
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new StringAnswer("Unauthorized for this transaction!"));
            } 
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new StringAnswer("An unexpected error occurred during getting user portfolio."));
        }  
    }

//////////////////////////////////////////////////////////////Order Endpoints////////////////////////////////////////////////////////////////////

    @PostMapping(
            path = "/order/buy",
            consumes = {MediaType.APPLICATION_JSON_VALUE}
    ) 
    public ResponseEntity<?> createBuyOrder(@RequestBody TokenTransactionContent tokenTransactionContent){
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
                portfolioStockManager.addPortfolioStock(userPortfolio.getPortfolioID(), transactionContent.getSymbol(), transactionContent.getStockAmount(), transactionContent.getTotalPrice());
                
                //Budget ändern
                userManager.editUserBudget(currentUser.getEmail(), currentUser.getBudget(), transactionContent.getTotalPrice(), transactionContent.getTransactionType());
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
    
    @PostMapping(
            path = "/order/sell",
            consumes = {MediaType.APPLICATION_JSON_VALUE}
    ) 
    public ResponseEntity<?> createSellOrder(@RequestBody TokenTransactionContent tokenTransactionContent){
        String token = tokenTransactionContent.getToken();
        TransactionContent transactionContent = tokenTransactionContent.getTransactionContent();
        Boolean isValid = sessionManager.validToken(token, transactionContent.getEmail());
        if (isValid) {
            User currentUser = userManager.getUserProfile(transactionContent.getEmail());
            PortfolioStockValue portfolioStockValues = portfolioStockManager.checkPortfolioStockValue(transactionContent.getTotalPrice(), currentUser.getEmail(), transactionContent.getSymbol());
            if(portfolioStockValues == null){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new StringAnswer("You don't own a position with the selected stock!"));
            }
            else{
                if(portfolioStockValues.getCurrentValue() == -1){
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new StringAnswer("Your stock position is not that high!"));
                }
                else{
                    transactionManager.addTransaction(transactionContent);
                    Portfolio userPortfolio = portfolioManager.getUserPortfolio(transactionContent.getEmail());
                    List<Transaction> transactionsInPortfolio = transactionManager.getAllTransactionsInPortfolioStock(transactionContent.getEmail());
                    Logger.getLogger("MappingMethode").log(Level.SEVERE, "{0}.", transactionsInPortfolio);
                    //PortfolioStock abändern
                    portfolioStockManager.deletePortfolioStock(userPortfolio.getPortfolioID(), transactionContent.getSymbol(), transactionContent.getStockAmount(), transactionContent.getTotalPrice(), portfolioStockValues, transactionsInPortfolio);
                    //Budget ändern works
                    userManager.editUserBudget(currentUser.getEmail(), currentUser.getBudget(), transactionContent.getTotalPrice(), transactionContent.getTransactionType());
                    //Portfoliowert ändern
                    //portfolioManager.editPortfolioValue(userPortfolio.getPortfolioID(), userPortfolio.getValue(), transactionContent.getTotalPrice());
                    return ResponseEntity.ok(new StringAnswer("Transaction was successfully completed"));
                }
                
            }
        }
        else{
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new StringAnswer("Unauthorized for this transaction!"));
        }
    }
        
}