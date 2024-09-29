package mosbach.dhbw.de.stockwizzard.controller;

import mosbach.dhbw.de.stockwizzard.model.User;
import mosbach.dhbw.de.stockwizzard.dataManagerImplementation.UserManagerImplementation;
import mosbach.dhbw.de.stockwizzard.dataManagerImplementation.*;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController

// @RequestMapping("/api")
public class MappingController {

    UserManagerImplementation userManager = UserManagerImplementation.getUserManager();
    public MappingController() {
    }

    @GetMapping("/user")
    public User getUserProfile(@RequestParam(value = "email", defaultValue = "") String email) {
        return userManager.getUserProfile(email);   
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