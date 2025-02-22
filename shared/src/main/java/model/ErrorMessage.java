package model;

import java.util.Objects;

public class ErrorMessage {
    public String getMessage() {
        return message;
    }

    private final String message;

    public ErrorMessage(String message) {
        this.message = message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ErrorMessage that)) {
            return false;
        }
        return Objects.equals(getMessage(), that.getMessage());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getMessage());
    }
}
