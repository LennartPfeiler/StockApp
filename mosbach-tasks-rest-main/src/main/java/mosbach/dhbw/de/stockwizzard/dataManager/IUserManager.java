package mosbach.dhbw.de.stockwizzard.dataManager;

import mosbach.dhbw.de.stockwizzard.model.User;

public interface IUserManager{

    public void createUserTable();

    public Boolean CheckIfEnoughBudgetLeft(Double needed, User currentUser);

    public Boolean isEmailAlreadyRegistered(String email);

    public User getUserProfile(String email);

    public void addUser(User user);

    public void editUserBudget(String email, Double oldValue, Double bougthValue, Integer transactionType);

    public void editProfile(User currentUser, User new_user_data);

    public void resetProfile(String email);

    public void deleteUser(String email);

}