@Controller
public class UserController {
    @QueryMapping
    public User UserByEmail(@Argument String email) {
        return UserService.getUserByEmail(email);
    }

    @SchemaMapping
    public 
