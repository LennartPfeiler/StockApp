package dhbw.mosbach.de.stockwizzard.service.dataManager;

import dhbw.mosbach.de.stockwizzard.model.User;


public interface IUserManager {

    public void createUserTable();

    public Boolean checkIfEnoughBudgetLeft(Double needed, User currentUser);

    public Boolean isEmailAlreadyRegistered(String email);

    public User getUserProfile(String email);

    public void addUser(User user);

    public void editUserBudget(String email, Double oldValue, Double bougthValue, Integer transactionType);

    public void editProfile(User currentUser, User newUserData);

    public void resetProfile(String email);

    public void deleteUser(String email);

}