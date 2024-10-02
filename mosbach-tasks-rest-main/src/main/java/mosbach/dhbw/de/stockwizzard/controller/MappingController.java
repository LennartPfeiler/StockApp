package mosbach.dhbw.de.stockwizzard.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;

import mosbach.dhbw.de.stockwizzard.dataManagerImplementation.AuthManagerImplementation;
import mosbach.dhbw.de.stockwizzard.dataManagerImplementation.PasswordManagerImplementation;
import mosbach.dhbw.de.stockwizzard.dataManagerImplementation.PortfolioManagerImplementation;
import mosbach.dhbw.de.stockwizzard.dataManagerImplementation.UserManagerImplementation;
import mosbach.dhbw.de.stockwizzard.model.LoginRequest;
import mosbach.dhbw.de.stockwizzard.model.RegisterRequest;
import mosbach.dhbw.de.stockwizzard.model.StringAnswer;
import mosbach.dhbw.de.stockwizzard.model.TokenUser;
import mosbach.dhbw.de.stockwizzard.model.User;
import mosbach.dhbw.de.stockwizzard.model.EmailCheckResponse;
import mosbach.dhbw.de.stockwizzard.model.Portfolio;


@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/api")
public class MappingController {

    UserManagerImplementation userManager = UserManagerImplementation.getUserManager();
    AuthManagerImplementation authManager = AuthManagerImplementation.getAuthManager();
    PortfolioManagerImplementation portfolioManager = PortfolioManagerImplementation.getPortfolioManager();
    PasswordManagerImplementation passwordManager = PasswordManagerImplementation.getPasswordManager();
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

        // Token generieren (Beispiel: JWT oder einfacher String-Token)
        String token = authManager.generateToken();

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
    public ResponseEntity<StringAnswer> createUser(@RequestBody RegisterRequest registerRequest){
        EmailCheckResponse mailResponse = userManager.isEmailAlreadyRegistered(registerRequest.getEmail());
        if(mailResponse.isRegistered() == true){
            //return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Der Benutzer ist bereits registriert.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        else{
            if(mailResponse.isRegistered() == false && mailResponse.getMessage().equals("Email ist noch nicht registriert.")){
                userManager.addUser(new User(registerRequest.getFirstname(), registerRequest.getLastname(), registerRequest.getEmail(), registerRequest.getPassword()));
                portfolioManager.addPortfolio(new Portfolio(null, registerRequest.getBudget(), registerRequest.getEmail()));
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


    @GetMapping("/user")
    public User getUserProfile(@RequestParam(value = "email", defaultValue = "") String email) {
        return userManager.getUserProfile(email);   
    }

    @GetMapping("/portfolio")
    public Portfolio getUserPortfolio(@RequestParam(value = "email", defaultValue = "") String email) {
        return portfolioManager.getUserPortfolio(email);   
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