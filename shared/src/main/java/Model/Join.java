package Model;

import java.util.Objects;

public class Join {
    private final String playerColor;
    private final Integer gameID;

    public Join(String playerColor, Integer gameID) {
        this.playerColor = playerColor;
        this.gameID = gameID;
    }

    public String getPlayerColor() {
        return playerColor;
    }

    public Integer getGameID() {
        return gameID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Join join)) return false;
        return Objects.equals(getPlayerColor(), join.getPlayerColor()) && Objects.equals(getGameID(), join.getGameID());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPlayerColor(), getGameID());
    }
}
