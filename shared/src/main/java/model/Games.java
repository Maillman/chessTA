package model;

import java.util.List;
import java.util.Objects;

public class Games {
    private final List<Game> games;

    public Games(List<Game> games) {
        this.games = games;
    }

    public List<Game> getGames() {
        return games;
    }

    @Override
    public String toString() {
        return "Games{" +
                "games=" + games +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Games games1)) return false;
        return Objects.equals(getGames(), games1.getGames());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getGames());
    }
}
