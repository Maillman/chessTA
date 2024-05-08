package client2server;

import Model.*;
import chess.ChessMove;
import com.google.gson.Gson;
import webSocketMessages.userCommands.UserGameCommand;

import java.io.IOException;
import java.util.Objects;

public class ServerFacade {
    private static HTTPCommunicator conn;
    private final WebSocketCommunicator ws;

    public ServerFacade(String url){
        conn = new HTTPCommunicator(url);
        ws = null;
    }

    public ServerFacade(String url, ServerMessageObserver smo) throws ResponseException {
        conn = new HTTPCommunicator(url);
        ws = new WebSocketCommunicator(url,smo);
    }

    public Auth register(User user) throws ResponseException {
        var path = "/user";
        return conn.makeRequest("POST", path, user, Auth.class);
    }

    public Auth login(User user) throws ResponseException {
        var path = "/session";
        return conn.makeRequest("POST", path, user, Auth.class);
    }

    public void logout(String authToken) throws ResponseException {
        var path = "/session";
        conn.addRequestHeader(authToken);
        conn.makeRequest("DELETE", path, null, null);
    }

    public Games list(String authToken) throws ResponseException {
        var path = "/game";
        conn.addRequestHeader(authToken);
        return conn.makeRequest("GET", path, null, Games.class);
    }

    public Game create(Game game, String authToken) throws ResponseException {
        var path = "/game";
        conn.addRequestHeader(authToken);
        return conn.makeRequest("POST", path, game, Game.class);
    }

    public Game join(Join join, String authToken) throws ResponseException {
        var path = "/game";
        conn.addRequestHeader(authToken);
        Game game = null;
        try {
            game = conn.makeRequest("PUT", path, join, Game.class);
        }catch(ResponseException re){
            if(!Objects.equals(re.getMessage(), "Error: Something went wrong with the server. 403 Forbidden")){
                throw new ResponseException(re.getStatusCode(), re.getMessage());
            }
        }finally{
            if (ws != null) {
                //wsJoin(join, authToken);
            }
        }
        return game;
    }

    public void clear() throws ResponseException {
        var path = "/db";
        conn.makeRequest("DELETE", path, null, null);
    }

    /*
    public void leaveResign(Integer gameID, Boolean isResigning, String authToken) throws ResponseException {
        try{
            if(ws!=null) {
                var userGameCommand = new UserGameCommand(authToken);
                if(isResigning){
                    userGameCommand.setCommandType(UserGameCommand.CommandType.RESIGN);
                }else {
                    userGameCommand.setCommandType(UserGameCommand.CommandType.LEAVE);
                }
                userGameCommand.setGameID(gameID);
                ws.session.getBasicRemote().sendText(new Gson().toJson(userGameCommand));
            }
        }catch(IOException ioe){
            throw new ResponseException(500, ioe.getMessage());
        }
    }
     */
    /*
    public void move(Integer gameID, ChessMove chessMove, String authToken) throws ResponseException{
        try {
            if(ws!=null) {
                var userGameCommand = new UserGameCommand(authToken);
                userGameCommand.setCommandType(UserGameCommand.CommandType.MAKE_MOVE);
                userGameCommand.setGameID(gameID);
                userGameCommand.setMove(chessMove);
                ws.session.getBasicRemote().sendText(new Gson().toJson(userGameCommand));
            }
        }catch(IOException ioe){
            throw new ResponseException(500, ioe.getMessage());
        }
    }
     */
    /*
    private void wsJoin(Join join, String authToken) throws ResponseException {
        try {
            var userGameCommand = new UserGameCommand(authToken);
            if (join.getPlayerColor() == null) {
                userGameCommand.setCommandType(UserGameCommand.CommandType.JOIN_OBSERVER);
            } else {
                userGameCommand.setCommandType(UserGameCommand.CommandType.JOIN_PLAYER);
            }
            userGameCommand.setJoinColor(join.getPlayerColor());
            userGameCommand.setGameID(join.getGameID());
            assert ws != null;
            ws.session.getBasicRemote().sendText(new Gson().toJson(userGameCommand));
        }catch(IOException ioe){
            throw new ResponseException(500, ioe.getMessage());
        }
    }
     */
}
