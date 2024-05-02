package webSocketMessages.userCommands;

import chess.ChessGame;
import chess.ChessMove;

import java.util.Objects;

/**
 * Represents a command a user can send the server over a websocket
 * 
 * Note: You can add to this class, but you should not alter the existing
 * methods.
 */
public class UserGameCommand {

    public UserGameCommand(String authToken) {
        this.authToken = authToken;
    }

    public enum CommandType {
        JOIN_PLAYER,
        JOIN_OBSERVER,
        MAKE_MOVE,
        LEAVE,
        RESIGN
    }

    protected CommandType commandType;

    private final String authToken;

    public void setCommandType(CommandType commandtype){
        this.commandType = commandtype;
    }

    public String getAuthString() {
        return authToken;
    }

    public CommandType getCommandType() {
        return this.commandType;
    }

    public int getGameID() {
        return gameID;
    }

    public void setGameID(int gameID) {
        this.gameID = gameID;
    }
    private ChessGame.TeamColor playerColor;
    private int gameID;

    public void setJoinColor(String color){
        if(Objects.equals(color, "WHITE")) {
            playerColor = ChessGame.TeamColor.WHITE;
        }else if(Objects.equals(color, "BLACK")){
            playerColor = ChessGame.TeamColor.BLACK;
        }else{
            playerColor = null;
        }
    }

    public String getPlayerColor() {
        if(playerColor == ChessGame.TeamColor.WHITE){
            return "WHITE";
        }else if(playerColor == ChessGame.TeamColor.BLACK){
            return "BLACK";
        }else{
            return "OBSERVER";
        }
    }

    public ChessMove getMove() {
        return move;
    }

    public void setMove(ChessMove move) {
        this.move = move;
    }

    private ChessMove move;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof UserGameCommand))
            return false;
        UserGameCommand that = (UserGameCommand) o;
        return getCommandType() == that.getCommandType() && Objects.equals(getAuthString(), that.getAuthString());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCommandType(), getAuthString());
    }
}
