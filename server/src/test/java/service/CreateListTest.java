package service;

import Model.Auth;
import Model.Game;
import chess.ChessGame;
import dataAccess.DataAccessException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Order;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CreateListTest extends BaseTest{
    protected static List<Game> expectedGames = new ArrayList<>();
    protected static List<Game> resultedGames = new ArrayList<>();
    protected static Auth auth;
    protected static Game newGame;
    @BeforeEach
    public void SetupGame() throws DataAccessException {
        expectedGames.clear();
        resultedGames.clear();
        //The game
        newGame = new Game(1,"","","newGame",new ChessGame());
        //create game with authorized user
        auth = register.login(existingUser);
        create.createGame(auth.getAuthToken(),newGame);
        //getting list of games to compare
        expectedGames.add(newGame);
        //check if game is created
        Assertions.assertEquals(expectedGames,create.listGames(auth.getAuthToken()),"The game was not created correctly!");
    }
    @Test
    @Order(1)
    @DisplayName("Create and List Several Games Test!")
    public void create_listGames() throws DataAccessException {
        //The games
        Game anotherGame = new Game(2,"","","ssehC",new ChessGame());
        Game aNewGame = new Game(3,"","","newGame",new ChessGame());
        //create games with authorized user
        auth = register.login(existingUser);
        create.createGame(auth.getAuthToken(),anotherGame);
        create.createGame(auth.getAuthToken(),aNewGame);
        //getting list of games to compare
        expectedGames.add(anotherGame);
        expectedGames.add(aNewGame);
        //check if games were created
        Assertions.assertEquals(expectedGames,create.listGames(auth.getAuthToken()),"The game was not created correctly!");
    }
    @Test
    @Order(2)
    @DisplayName("Unauthorized Create Game!")
    public void unCreateGame() {
        //The game
        Game newGame = new Game(1,"","","newGame",new ChessGame());
        //create game with unauthorized user
        Assertions.assertThrows(DataAccessException.class, () -> create.createGame("UNLAWFUL CRIMINAL",newGame),"CreateGame not throwing exception!");
    }

    @Test
    @Order(3)
    @DisplayName("Unauthorized List Game!")
    public void unListGame() throws DataAccessException {
        //The games
        Game anotherGame = new Game(2,"","","ssehC",new ChessGame());
        Game aNewGame = new Game(3,"","","newGame",new ChessGame());
        //create games with authorized user
        auth = register.login(existingUser);
        create.createGame(auth.getAuthToken(),anotherGame);
        create.createGame(auth.getAuthToken(),aNewGame);
        //getting list of games to compare
        expectedGames.add(anotherGame);
        expectedGames.add(aNewGame);
        //attempt to list game with unauthorized token
        Assertions.assertThrows(DataAccessException.class, () -> create.listGames("UNLAWFUL CRIMINAL"),"ListGames not throwing exception!");
    }
}
