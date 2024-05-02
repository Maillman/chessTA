package Model;

import chess.ChessGame;

import java.util.Objects;

public class Game{
    private final Integer gameID;
    private final String whiteUsername;
    private final String blackUsername;
    private final String gameName;
    private final ChessGame game;

    public Game(Integer gameID, String whiteUsername, String blackUsername, String gameName, ChessGame game){
        this.gameID = gameID;
        this.whiteUsername = whiteUsername;
        this.blackUsername = blackUsername;
        this.gameName = gameName;
        this.game = game;
    }

    public Integer getGameID() {
        return gameID;
    }

    public String getWhiteUsername() {
        return whiteUsername;
    }

    public String getBlackUsername() {
        return blackUsername;
    }

    public String getGameName() {
        return gameName;
    }

    public ChessGame getGame() {
        return game;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Game game1)) return false;
        return Objects.equals(getGameID(), game1.getGameID()) && Objects.equals(getWhiteUsername(), game1.getWhiteUsername()) && Objects.equals(getBlackUsername(), game1.getBlackUsername()) && Objects.equals(getGameName(), game1.getGameName()) && Objects.equals(getGame(), game1.getGame());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getGameID(), getWhiteUsername(), getBlackUsername(), getGameName(), getGame());
    }

    @Override
    public String toString() {
        return "Game{" +
                "gameID=" + gameID +
                ", whiteUsername='" + whiteUsername + '\'' +
                ", blackUsername='" + blackUsername + '\'' +
                ", gameName='" + gameName + '\'' +
                ", game=" + game +
                '}';
    }
}
