package mosbach.dhbw.de.controller;

import mosbach.dhbw.de.model.User;
import mosbach.dhbw.de.implementation.*;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController

// @RequestMapping("/api")
public class MappingController {

    public MappingController() {
    }

    @GetMapping("/user")
    public User getUserProfile(@RequestParam(value = "userID", defaultValue = "0") int userID) {
        DatabaseUserImplementation db_user = new DatabaseUserImplementation();
        return db_user.getUserProfile(userID);   
    }

    @PostMapping(                                                                               
            path = "/user",
            consumes = {MediaType.APPLICATION_JSON_VALUE}
    )
    @ResponseStatus(HttpStatus.OK)
    public User createUser(@RequestBody User user) {
        DatabaseUserImplementation db_user = DatabaseUserImplementation.getDatabaseUser();
        User newUser = new User();
        newUser.setUserID(db_user.addUser(user));
        return newUser;
    }

    @PutMapping(                                                                                
            path = "/user",
            consumes = {MediaType.APPLICATION_JSON_VALUE}
    )
    @ResponseStatus(HttpStatus.OK)
    public StringAnswer editUser(@RequestBody User user) {
        DatabaseUserImplementation db_user = DatabaseUserImplementation.getDatabaseUser();
        boolean success = db_user.editUser(user);
        if (success) return new StringAnswer("success");
        else return new StringAnswer("failure");
    }

    @DeleteMapping(path = "/user")                                                              
    public StringAnswer deleteUser(@RequestParam (value = "userID", defaultValue = "0") int userID) {
        DatabaseUserImplementation db_user = DatabaseUserImplementation.getDatabaseUser();
        boolean success = db_user.deleteUser(userID);
        if (success) return new StringAnswer("success");
        else return new StringAnswer("failure");
    }
}