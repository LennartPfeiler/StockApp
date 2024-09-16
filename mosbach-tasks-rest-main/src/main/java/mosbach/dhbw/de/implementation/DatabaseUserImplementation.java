package mosbach.dhbw.de.implementation;

import mosbach.dhbw.de.model.User;

public class DatabaseUserImplementation{

    static DatabaseUserImplementation databaseUser = null;

    private DatabaseUserImplementation(){
        
    }

    static public DatabaseUserImplementation getDatabaseUser() {
        if (databaseUser == null)
            databaseUser = new DatabaseUserImplementation();
        return databaseUser;
    }

    public User getUserProfile(int userID){
        User user = null;
        string getUser_database_query= "SELECT * FROM User WHERE UserID = " + userID + ";";
        //get from database
        return user;
    }

    public int addUser(User user) {
        int userID = 0;
        // write the data into database
        String addUser_database_query = "Insert into User (FirstName, LastName, EMail, Password) Values ('" + user.getFirstName() + "', '" + user.getLastName() + "', '" + user.getEmail() + "', '" + user.getPhone() +"', '" + user.getPassword() + "');";
        //Write into database
        return userID;
    }

    public boolean editUser(User user) {
        boolean edited;
        // change the data of user, found by id
        String editUser_database_query = "UPDATE Users" +
                "       SET FirstName = '" + user.getFirstName() + "', LastName = '" + user.getLastName() +
                        "', EMail = '" + user.getEmail() + "', Password = '" + user.getPassword() + "'" +
                "       WHERE UserID = " + user.getUserID() + ";";
        //Write into database
        return edited;
    }

    @Override
    public boolean deleteUser(int userID) {
        boolean deleted;
        // sql strings to delete the user and the connections, if he uses carpools (foreign key)
        String deleteUser_database_query = "DELETE FROM Users" +
                "       WHERE UserID = " + userID + ";";
        //Delete from database
        return deleted;
    }

}