package server.WebSocket;

import Model.Auth;
import Model.Game;
import chess.ChessMove;
import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import service.GameService;
import service.UserService;
import websocket.messages.ServerMessage;
import websocket.commands.UserGameCommand;

import java.io.IOException;
import java.util.Objects;

@WebSocket
public class WebSocketHandler {
    private final ConnectionManager connections = new ConnectionManager();
    private final UserService userService;
    private final GameService gameService;

    public WebSocketHandler(UserService userService, GameService gameService){
        this.userService = userService;
        this.gameService = gameService;
    }

    public ConnectionManager getConnections(){
        return connections;
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException {
        Auth auth = null;
        try {
            UserGameCommand userGameCommand = new Gson().fromJson(message, UserGameCommand.class);
            auth = userService.verifyUser(userGameCommand.getAuthString());
            if (auth == null) {
                throw new Exception("Unauthorized!");
            }
            switch (userGameCommand.getCommandType()) {
                case CONNECT -> {
                    String joinColor = userGameCommand.getPlayerColor();
                    int gameID = userGameCommand.getGameID();
                    Game game = gameService.getGame(gameID);
                    if(Objects.equals(auth.getUsername(), game.getWhiteUsername())){
                        if(Objects.equals(joinColor, "BLACK")){
                            throw new Exception("Bad Request");
                        }
                        joinColor = "WHITE";
                    }else if(Objects.equals(auth.getUsername(), game.getBlackUsername())){
                        if(Objects.equals(joinColor, "WHITE")){
                            throw new Exception("Bad Request");
                        }
                        joinColor = "BLACK";
                    }else{
                        joinColor = "OBSERVER";
                    }
                    join(auth, session, joinColor, game);
                }
                case LEAVE -> {
                    int gameID = userGameCommand.getGameID();
                    Game game = gameService.getGame(gameID);
                    if(Objects.equals(game.getWhiteUsername(), auth.getUsername())){
                        gameService.updateGame(auth.getAuthToken(), gameID, new Game(gameID,null,game.getBlackUsername(),game.getGameName(),game.getGame()));
                    }else if(Objects.equals(game.getBlackUsername(), auth.getUsername())){
                        gameService.updateGame(auth.getAuthToken(), gameID, new Game(gameID,game.getWhiteUsername(),null,game.getGameName(),game.getGame()));
                    }
                    leave(auth, gameID);
                }
                case MAKE_MOVE -> {
                    int gameID = userGameCommand.getGameID();
                    if(!connections.gamesOver.contains(gameID)) {
                        ChessMove chessMove = userGameCommand.getMove();
                        Game foundGame = gameService.getGame(gameID);
                        String chessPiece = foundGame.getGame().getBoard().getPiece(chessMove.getStartPosition()).toString();
                        foundGame.getGame().makeMove(chessMove);
                        Game updatedGame = gameService.updateGame(auth.getAuthToken(), gameID, foundGame);
                        move(auth, session, gameID, chessPiece, chessMove, updatedGame);
                    }else{
                        error(session,"The game is over!");
                    }
                }
                case RESIGN -> {
                    int gameID = userGameCommand.getGameID();
                    Game game = gameService.getGame(gameID);
                    if(connections.gamesOver.contains(gameID)){
                        throw new Exception("The game is over!");
                    }
                    if(Objects.equals(auth.getUsername(), game.getWhiteUsername()) || Objects.equals(auth.getUsername(), game.getBlackUsername())) {
                        connections.gamesOver.add(gameID);
                        resign(session, auth, gameID);
                    }else{
                        throw new Exception("Unauthorized!");
                    }
                }
            }
        } catch(Exception e){
            error(session,e.getMessage());
        }
    }
    private void resign(Session session, Auth auth, Integer gameID) throws IOException {
        String username = auth.getUsername();
        String authToken = auth.getAuthToken();
        var message = String.format("%s has resigned from the game.",username);
        var serverMessage = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
        serverMessage.addMessage(message);
        connections.broadcastNotification(authToken, gameID, serverMessage);
        connections.displayRoot(session, serverMessage);
    }

    private void move(Auth auth, Session session, Integer gameID, String chessPiece, ChessMove chessMove, Game updatedGame) throws IOException {
        String username = auth.getUsername();
        String authToken = auth.getAuthToken();
        var message = String.format("%s has moved a %s from %s to %s.",username, chessPiece, chessMove.getStartPosition(),chessMove.getEndPosition());
        var serverMessage = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
        var displayMessage = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME);
        displayMessage.addGame(updatedGame);
        connections.displayRoot(session, displayMessage);
        connections.broadcastNotification(authToken, gameID, displayMessage);
        serverMessage.addMessage(message);
        connections.broadcastNotification(authToken, gameID, serverMessage);
        checkGame(gameID, updatedGame, username);
    }

    private void checkGame(Integer gameID, Game updatedGame, String username) throws IOException {
        String check = null, otherPlayer;
        if(Objects.equals(username, updatedGame.getWhiteUsername())){
            otherPlayer = updatedGame.getBlackUsername();
        }else{
            otherPlayer = updatedGame.getWhiteUsername();
        }
        if (updatedGame.getGame().isInCheckmate(updatedGame.getGame().getTeamTurn())) {
            check = String.format("%s is in checkmate.", otherPlayer);
            connections.gamesOver.add(gameID);
        } else if(updatedGame.getGame().isInCheck(updatedGame.getGame().getTeamTurn())){
            check = String.format("%s is in check.", otherPlayer);
        } else if (updatedGame.getGame().isInStalemate(updatedGame.getGame().getTeamTurn())) {
            check = String.format("%s is in stalemate.", otherPlayer);
            connections.gamesOver.add(gameID);
        }
        if(check!=null){
            var checkMessage = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
            checkMessage.addMessage(check);
            connections.broadcastAll(gameID, checkMessage);
        }
    }

    private void leave(Auth auth, Integer gameID) throws IOException {
        String username = auth.getUsername();
        String authToken = auth.getAuthToken();
        connections.remove(authToken, gameID);
        var message = String.format("%s has left the game.",username);
        var serverMessage = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
        serverMessage.addMessage(message);
        connections.broadcastNotification(authToken, gameID, serverMessage);
    }

    private void join(Auth auth, Session session, String joinColor, Game game) throws IOException {
        String username = auth.getUsername();
        String authToken = auth.getAuthToken();
        connections.add(authToken, game.getGameID(), session);
        var message = String.format("%s has joined the game as %s.",username, joinColor);
        var serverMessage = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
        var displayMessage = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME);
        serverMessage.addMessage(message);
        connections.broadcastNotification(authToken, game.getGameID(), serverMessage);
        displayMessage.addGame(game);
        connections.displayRoot(session, displayMessage);
    }
    private void error(Session session, String exceptionMessage) throws IOException {
        var message = String.format("Error: %s",exceptionMessage);
        var displayMessage = new ServerMessage(ServerMessage.ServerMessageType.ERROR);
        displayMessage.addErrorMessage(message);
        connections.displayRoot(session, displayMessage);
    }
}
