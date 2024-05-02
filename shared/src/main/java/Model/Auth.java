package Model;

import java.util.Objects;

public class Auth {
    private final String authToken;
    private final String username;

    public Auth(String authToken, String username){
        this.authToken = authToken;
        this.username = username;
    }

    public String getAuthToken() {
        return authToken;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Auth auth)) return false;
        return Objects.equals(getAuthToken(), auth.getAuthToken()) && Objects.equals(getUsername(), auth.getUsername());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAuthToken(), getUsername());
    }
}
