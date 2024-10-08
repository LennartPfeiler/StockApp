package mosbach.dhbw.de.stockwizzard.dataManager;

import mosbach.dhbw.de.stockwizzard.model.User;
import mosbach.dhbw.de.stockwizzard.model.EmailCheckResponse;

public interface IUserManager{

    /**
     * Get a existing user
     * @param userID: UserID of the user
     * @return User object 
     */
    public User getUserProfile(String email);

    /**
     * Create a new user
     * @param user: User object with data without ID
     * @return Returns generated ID of the new user
     */
    public void addUser(User user);

    /**
     * Edit an existing user
     * @param user: The user which is currently logged in
     * @return Returns true if successful, returns false if not successful
     */
    //public boolean editUser(String currentEmail, User user);

    /**
     * Delete an existing user
     * @param userID: UserID of the user
     * @return Returns true if successful, returns false if not successful
     */
    //public boolean deleteUser(int userID);

    public EmailCheckResponse isEmailAlreadyRegistered(String email);

    public Boolean CheckIfEnoughBudgetLeft(Double needed, User currentUser);
}