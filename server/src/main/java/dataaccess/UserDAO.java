package dataaccess;

import model.User;

public interface UserDAO {
    User getUser(String username) throws DataAccessException;

    void createUser(User user) throws DataAccessException;

    void clear() throws DataAccessException;
}
