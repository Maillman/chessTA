package service;

import model.Auth;
import model.User;
import dataaccess.*;
import org.mindrot.jbcrypt.BCrypt;

import java.util.UUID;


public class UserService {
    private final UserDAO userDAO;
    private final AuthDAO authDAO;

    public UserService(UserDAO userDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    public Auth registerUser(User user) throws DataAccessException {
        if(user.getUsername()==null||user.getPassword()==null||user.getEmail()==null){
            throw new DataAccessException("Bad Request!");
        }
        String username = user.getUsername();
        if(userDAO.getUser(username)==null){
            String hashedPassword = BCrypt.hashpw(user.getPassword(),BCrypt.gensalt());
            User hashedUser = new User(user.getUsername(), hashedPassword, user.getEmail());
            userDAO.createUser(hashedUser);
            return createNewAuth(username);
        }else {
            throw new DataAccessException("Already Taken!");
        }
    }

    public Auth login(User user) throws DataAccessException {
        String username = user.getUsername();
        User getUser = userDAO.getUser(username);
        if(getUser!=null) {
            var hashedPassword = getUser.getPassword();
            if(BCrypt.checkpw(user.getPassword(), hashedPassword)){
                return createNewAuth(username);
            }
        }
        throw new DataAccessException("Unauthorized!");
    }

    private Auth createNewAuth(String username) throws DataAccessException {
        String authToken = UUID.randomUUID().toString();
        return authDAO.createAuth(authToken, username);
    }

    public void logout(String authToken) throws DataAccessException {
        if(authDAO.getAuth(authToken)!=null){
            authDAO.deleteAuth(authToken);
        }else{
            throw new DataAccessException("Unauthorized!");
        }
    }

    public Auth verifyUser(String authToken) throws DataAccessException{
        return authDAO.getAuth(authToken);
    }
}
