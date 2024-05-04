package dataaccess;

import Model.Auth;

import java.util.HashMap;
import java.util.Objects;

public class MemoryAuthDAO implements AuthDAO{
    final private HashMap<String, Auth> auths = new HashMap<>();
    @Override
    public Auth createAuth(String authToken, String username) {
        Auth auth = new Auth(authToken,username);
        auths.put(authToken, auth);
        return auth;
    }

    @Override
    public Auth getAuth(String authToken) {
        return auths.get(authToken);
    }

    @Override
    public void deleteAuth(String authToken) {
        auths.remove(authToken);
    }
    @Override
    public void clear() {
        auths.clear();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MemoryAuthDAO that)) return false;
        return Objects.equals(auths, that.auths);
    }

    @Override
    public int hashCode() {
        return Objects.hash(auths);
    }
}
