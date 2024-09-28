package mosbach.dhbw.de.dataManager;

import mosbach.dhbw.de.model.User;

public interface IUserManager{

    /**
     * Get a existing user
     * @param userID: UserID of the user
     * @return User object 
     */
    public User getUserProfile(int userID);

    /**
     * Create a new user
     * @param user: User object with data without ID
     * @return Returns generated ID of the new user
     */
    public int addUser(User user);

    /**
     * Edit an existing user
     * @param user: The user which is currently logged in
     * @return Returns true if successful, returns false if not successful
     */
    public boolean editUser(User user);

    /**
     * Delete an existing user
     * @param userID: UserID of the user
     * @return Returns true if successful, returns false if not successful
     */
    public boolean deleteUser(int userID);
}