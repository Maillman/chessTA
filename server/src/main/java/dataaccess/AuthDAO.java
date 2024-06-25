package dataaccess;

import model.Auth;

public interface AuthDAO {
    Auth createAuth(String authToken, String username) throws DataAccessException;

    Auth getAuth(String authToken) throws DataAccessException;

    void deleteAuth(String authToken) throws DataAccessException;

    void clear() throws DataAccessException;
}
