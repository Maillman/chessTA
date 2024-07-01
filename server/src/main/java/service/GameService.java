package service;

import model.Auth;
import model.Join;
import model.Game;
import chess.ChessGame;
import dataaccess.*;

import java.util.List;
import java.util.Objects;

public class GameService {
    private final AuthDAO authDAO;
    private final GameDAO gameDAO;
    public GameService(AuthDAO authDAO, GameDAO gameDAO) {
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
    }

    public Game createGame(String authToken, Game game) throws DataAccessException {
        checkAuthToken(authToken);
        //We don't want a game that doesn't exist where we can't get a board.
        if(game.getGame()==null){
            game = new Game(game.getGameID(),game.getWhiteUsername(),game.getBlackUsername(),game.getGameName(), new ChessGame());
        }
        return gameDAO.createGame(game);
    }

    public Game joinGame(String authToken, Join join) throws DataAccessException {
        Auth auth = authDAO.getAuth(authToken);
        if(auth!=null){
            if(join.getGameID()==null){
                throw new DataAccessException("Bad Request!");
            }
            Game foundGame = gameDAO.getGame(join.getGameID());
            if(foundGame!=null&&join.getPlayerColor()!=null){
                return joiningGame(join, foundGame, auth);
            }else{
                throw new DataAccessException("Bad Request!");
            }
        }else{
            throw new DataAccessException("Unauthorized!");
        }
    }

    private Game joiningGame(Join join, Game foundGame, Auth auth) throws DataAccessException {
        switch(join.getPlayerColor()){
            case "WHITE" -> {
                if(!(whiteFree(foundGame) || whiteReserved(foundGame, auth))){
                    throw new DataAccessException("Already Taken!");
                }
            }
            case "BLACK" -> {
                if(!(blackFree(foundGame) || blackReserved(foundGame, auth))){
                    throw new DataAccessException("Already Taken!");
                }
            }
        }
        return gameDAO.updateGame(auth.getUsername(), foundGame.getGameID(), join.getPlayerColor(), foundGame);
    }

    private static boolean blackFree(Game foundGame) {
        return (Objects.equals(foundGame.getBlackUsername(), "")) || (foundGame.getBlackUsername() == null);
    }

    private static boolean whiteFree(Game foundGame) {
        return (Objects.equals(foundGame.getWhiteUsername(), "")) || (foundGame.getWhiteUsername() == null);
    }

    public List<Game> listGames(String authToken) throws DataAccessException {
        checkAuthToken(authToken);
        return gameDAO.listGames();
    }

    private void checkAuthToken(String authToken) throws DataAccessException {
        if(authDAO.getAuth(authToken)==null){
            throw new DataAccessException("Unauthorized!");
        }
    }

    public Game getGame(int gameID) throws DataAccessException{
        return gameDAO.getGame(gameID);
    }

    public Game updateGame(String authToken, int gameID, Game updatedGame) throws DataAccessException{
        Auth auth = authDAO.getAuth(authToken);
        if(auth!=null){
            Game foundGame = gameDAO.getGame(gameID);
            String theName;
            if(unchangedBoard(updatedGame, foundGame)){
                theName = null;
            }else{
                theName = auth.getUsername();
            }
            if(whiteReserved(foundGame, auth) &&(switchTurns(updatedGame,ChessGame.TeamColor.BLACK)||unchangedBoard(updatedGame, foundGame))){
                return gameDAO.updateGame(theName,foundGame.getGameID(), "WHITE",updatedGame);
            }else if(blackReserved(foundGame, auth) &&(switchTurns(updatedGame,ChessGame.TeamColor.WHITE)||unchangedBoard(updatedGame, foundGame))){
                return gameDAO.updateGame(theName,foundGame.getGameID(), "BLACK",updatedGame);
            }else{
                throw new DataAccessException("Unauthorized!");
            }
        }else{
            throw new DataAccessException("Unauthorized!");
        }
    }

    private static boolean unchangedBoard(Game updatedGame, Game foundGame) {
        return Objects.equals(foundGame.getGame().getBoard(), updatedGame.getGame().getBoard());
    }

    private static boolean switchTurns(Game updatedGame, ChessGame.TeamColor switchColor) {
        return updatedGame.getGame().getTeamTurn() == switchColor;
    }

    private static boolean blackReserved(Game foundGame, Auth auth) {
        return Objects.equals(foundGame.getBlackUsername(), auth.getUsername());
    }

    private static boolean whiteReserved(Game foundGame, Auth auth) {
        return Objects.equals(foundGame.getWhiteUsername(), auth.getUsername());
    }
}
