package dataAccess;

import Model.Game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class MemoryGameDAO implements GameDAO{
    private int gameID = 0;
    final private HashMap<Integer, Game> games = new HashMap<>();

    @Override
    public List<Game> listGames(){
        List<Game> listGames = new ArrayList<>();
        for(int i = 1; i <= gameID; i++){
            listGames.add(games.get(i));
        }
        return listGames;
    }
    @Override
    public Game getGame(int gameID){
        return games.get(gameID);
    }

    @Override
    public Game createGame(Game game){
        gameID++;
        Game createGame = new Game(gameID,game.getWhiteUsername(), game.getBlackUsername(), game.getGameName(), game.getGame());
        games.put(gameID,createGame);
        return createGame;
    }
    @Override
    public Game updateGame(String username, Integer gameID, String playerColor, Game upGame) {
        Game newGame = games.get(gameID);
        if(playerColor != null){
            switch (playerColor) {
                case "BLACK" -> newGame = new Game(gameID, newGame.getWhiteUsername(), username, newGame.getGameName(), upGame.getGame());
                case "WHITE" -> newGame = new Game(gameID, username, newGame.getBlackUsername(), newGame.getGameName(), upGame.getGame());
            }
        }else {
            newGame = new Game(gameID,newGame.getWhiteUsername(),newGame.getBlackUsername(),newGame.getGameName(),upGame.getGame());
        }
        games.remove(gameID);
        games.put(gameID,newGame);
        return newGame;
    }
    @Override
    public void clear() {
        gameID = 0;
        games.clear();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MemoryGameDAO that)) return false;
        return gameID == that.gameID && Objects.equals(games, that.games);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gameID, games);
    }
}
