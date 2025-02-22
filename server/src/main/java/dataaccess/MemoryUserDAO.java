package dataaccess;

import model.User;

import java.util.HashMap;
import java.util.Objects;

public class MemoryUserDAO implements UserDAO {
    final private HashMap<String, User> users = new HashMap<>();

    @Override
    public User getUser(String username) throws DataAccessException {
        return users.get(username);
    }

    @Override
    public void createUser(User user) throws DataAccessException{
        users.put(user.getUsername(), user);
    }
    @Override
    public void clear() throws DataAccessException {
        users.clear();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MemoryUserDAO that)) {
            return false;
        }
        return Objects.equals(users, that.users);
    }

    @Override
    public int hashCode() {
        return Objects.hash(users);
    }
}
