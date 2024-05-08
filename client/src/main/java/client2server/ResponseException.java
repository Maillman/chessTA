package client2server;

public class ResponseException extends Exception {
    public int getStatusCode() {
        return statusCode;
    }

    final private int statusCode;

    public ResponseException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }
}