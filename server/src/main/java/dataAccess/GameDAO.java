package dataAccess;

import Model.Game;

import java.util.List;

public interface GameDAO {
    List<Game> listGames() throws DataAccessException;

    Game getGame(int gameID) throws DataAccessException;

    Game createGame(Game game) throws DataAccessException;

    Game updateGame(String username, Integer gameID, String playerColor, Game upGame) throws  DataAccessException;

    void clear() throws DataAccessException;
}
