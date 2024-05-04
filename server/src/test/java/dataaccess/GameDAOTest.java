package dataaccess;


import Model.Game;

import chess.ChessGame;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GameDAOTest {
    protected static GameDAO testGameDAO;
    protected static Game existingGame;
    protected static Game newGame;
    protected static List<Game> expectedGames = new ArrayList<>();
    protected static List<Game> resultedGames = new ArrayList<>();
    @BeforeAll
    public static void init() {
        testGameDAO = new SQLGameDAO();
    }

    @BeforeEach
    public void setUp() throws DataAccessException {
        existingGame = new Game(1,null,null,"aGameName",new ChessGame());
        newGame = new Game(2,null,null,"WOW!",new ChessGame());
        expectedGames.clear();
        resultedGames.clear();
        testGameDAO.clear();
        testGameDAO.createGame(existingGame);
    }

    @Test
    @Order(1)
    @DisplayName("Create Game Test")
    public void create() throws DataAccessException {
        //create and put a game into the database.
        testGameDAO.createGame(newGame);
        //assert the game is inserted.
        existingGame = testGameDAO.getGame(newGame.getGameID());
        Assertions.assertEquals(newGame,testGameDAO.getGame(newGame.getGameID()),"Game not inserted properly!");
    }

    @Test
    @Order(2)
    @DisplayName("List Games Test")
    public void list() throws DataAccessException {
        //create and put a game into the database.
        testGameDAO.createGame(newGame);
        //getting list of games to compare
        expectedGames.add(existingGame);
        expectedGames.add(newGame);
        //check if games were created
        Assertions.assertEquals(expectedGames,testGameDAO.listGames(),"The game was not created correctly!");
    }

    @Test
    @Order(3)
    @DisplayName("Create, Join, and List a Game Test!")
    public void createJoinListGame() throws DataAccessException {
        //create and put a game into the database.
        testGameDAO.createGame(newGame);
        //update the game in the database with new usernames
        newGame = new Game(newGame.getGameID(), "aWholeNewName","Phoenix_Gamer",newGame.getGameName(), newGame.getGame());
        testGameDAO.updateGame(newGame.getWhiteUsername(), newGame.getGameID(), "WHITE",newGame);
        testGameDAO.updateGame(newGame.getBlackUsername(), newGame.getGameID(), "BLACK",newGame);
        //getting list of games to compare
        expectedGames.add(existingGame);
        expectedGames.add(newGame);
        //check if games were created and updated
        Assertions.assertEquals(expectedGames,testGameDAO.listGames(),"The game was not updated correctly!");
    }

    @Test
    @Order(4)
    @DisplayName("Join as Watcher Test!")
    public void joinWatch() throws DataAccessException {
        //create and put a game into the database.
        testGameDAO.createGame(newGame);
        //update the game in the database with new usernames
        testGameDAO.updateGame("Phoenix_Gamer", newGame.getGameID(), "",newGame);
        testGameDAO.updateGame("Maillman", newGame.getGameID(), "",newGame);
        //getting list of games to compare
        expectedGames.add(existingGame);
        expectedGames.add(newGame);
        //check if games were created and updated
        Assertions.assertEquals(expectedGames,testGameDAO.listGames(),"The game was not updated correctly!");
    }

    @Test
    @Order(5)
    @DisplayName("Access Out of Bounds Test")
    public void outOfBounds(){
        //attempt to update a game that isn't in the database
        Assertions.assertThrows(DataAccessException.class, () -> testGameDAO.updateGame("SomeUser",3,"WHITE",newGame),"Update not throwing exception!");
    }

    @Test
    @Order(6)
    @DisplayName("Listing Empty List Test")
    public void emptyList() throws DataAccessException{
        //clear the database
        testGameDAO.clear();
        //check if games were created
        Assertions.assertEquals(expectedGames,testGameDAO.listGames(),"The list wasn't emptied correctly!");
    }

    @Test
    @Order(7)
    @DisplayName("Clear Test")
    public void clear() throws DataAccessException {
        //clear the database
        testGameDAO.clear();
        //check if users database cleared by running a negative test.
        Assertions.assertNull(testGameDAO.getGame(existingGame.getGameID()), "Games not null");
    }
}
