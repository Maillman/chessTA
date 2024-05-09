package service;

import Model.Auth;
import Model.Join;
import Model.Game;
import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPosition;
import dataaccess.*;
import org.junit.jupiter.api.Assertions;

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
                if(     ((Objects.equals(join.getPlayerColor(), "WHITE") && ((Objects.equals(foundGame.getWhiteUsername(), ""))||(foundGame.getWhiteUsername()==null)|| Objects.equals(foundGame.getWhiteUsername(), auth.getUsername())))) ||
                        ((Objects.equals(join.getPlayerColor(), "BLACK") && ((Objects.equals(foundGame.getBlackUsername(), ""))||(foundGame.getBlackUsername()==null)|| Objects.equals(foundGame.getWhiteUsername(), auth.getUsername())))) ||
                        ((!Objects.equals(join.getPlayerColor(),"WHITE") && !Objects.equals(join.getPlayerColor(),"BLACK")))){
                    return gameDAO.updateGame(auth.getUsername(),foundGame.getGameID(),join.getPlayerColor(),foundGame);
                }else{
                    throw new DataAccessException("Already Taken!");
                }
            }else{
                throw new DataAccessException("Bad Request!");
            }
        }else{
            throw new DataAccessException("Unauthorized!");
        }
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
            if(Objects.equals(foundGame.getGame().getBoard(), updatedGame.getGame().getBoard())){
                theName = null;
            }else{
                theName = auth.getUsername();
            }
            if(Objects.equals(foundGame.getWhiteUsername(), auth.getUsername())&&(updatedGame.getGame().getTeamTurn() == ChessGame.TeamColor.BLACK||Objects.equals(foundGame.getGame().getBoard(), updatedGame.getGame().getBoard()))){
                return gameDAO.updateGame(theName,foundGame.getGameID(), "WHITE",updatedGame);
            }else if(Objects.equals(foundGame.getBlackUsername(), auth.getUsername())&&(updatedGame.getGame().getTeamTurn() == ChessGame.TeamColor.WHITE||Objects.equals(foundGame.getGame().getBoard(), updatedGame.getGame().getBoard()))){
                return gameDAO.updateGame(theName,foundGame.getGameID(), "BLACK",updatedGame);
            }else{
                throw new DataAccessException("Unauthorized!");
            }
        }else{
            throw new DataAccessException("Unauthorized!");
        }
    }
}
