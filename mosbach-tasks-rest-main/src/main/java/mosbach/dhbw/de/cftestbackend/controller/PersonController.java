package mosbach.dhbw.de.cftestbackend.controller;

import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/persons")
public class PersonController {

    // private final PersonRepository personRepository;

    public PersonController(
    ) {}

    @GetMapping
    public String getAllPersons() {
        return "I am alive.";
    }
}