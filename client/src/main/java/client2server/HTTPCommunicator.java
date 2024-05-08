package client2server;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

public class HTTPCommunicator {
    private final String serverURL;
    private static String requestHeader;
    public HTTPCommunicator(String url) {
        serverURL = url;
    }
    public void addRequestHeader(String reqHeader){
        requestHeader = reqHeader;
    }
    public <T> T makeRequest(String method, String path, Object request, Class<T> responseClass) throws ResponseException {
        try{
            URL uri = (new URI(serverURL + path)).toURL();
            HttpURLConnection http = (HttpURLConnection)  uri.openConnection();
            http.setRequestMethod(method);
            http.setDoOutput(true);
            writeHeader(http);
            writeBody(request, http);
            http.connect();
            throwIfNotSuccessful(http);
            return readBody(http, responseClass);
        }catch(Exception e){
            throw new ResponseException(500, e.getMessage());
        }
    }
    private static void writeHeader(HttpURLConnection http){
        if(requestHeader!=null){
            http.addRequestProperty("authorization", requestHeader);
            requestHeader = null;
        }
    }
    private static void writeBody(Object request, HttpURLConnection http) throws IOException {
        if (request != null) {
            http.addRequestProperty("Content-Type", "application/json");
            String reqData = new Gson().toJson(request);
            try (OutputStream reqBody = http.getOutputStream()) {
                reqBody.write(reqData.getBytes());
            }
        }
    }
    private static <T> T readBody(HttpURLConnection http, Class<T> responseClass) throws IOException{
        T response = null;
        if (http.getContentLength() < 0){
            try(InputStream respBody = http.getInputStream()){
                InputStreamReader reader = new InputStreamReader(respBody);
                if(responseClass != null){
                    response = new Gson().fromJson(reader, responseClass);
                }
            }
        }
        return response;
    }
    private void throwIfNotSuccessful(HttpURLConnection http) throws IOException, ResponseException {
        var status = http.getResponseCode();
        var message = http.getResponseMessage();
        if(!isSuccessful(status)) {
            throw new ResponseException(status, "Error: Something went wrong with the server. " + status + " " + message);
        }
    }
    private boolean isSuccessful(int statusCode) {
        return (statusCode/100==2);
    }
}
