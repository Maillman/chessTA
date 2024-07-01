package service;

import model.Auth;
import model.Join;
import model.Game;
import chess.ChessGame;
import dataaccess.DataAccessException;
import org.junit.jupiter.api.*;
import org.mindrot.jbcrypt.BCrypt;

@SuppressWarnings("unused")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JoinTest extends CreateListTest {
    @Test
    @Order(1)
    @DisplayName("Create, Join, and List a Game Test!")
    public void createJoinListGame() throws DataAccessException {
        //authorized user joins as White
        create.joinGame(auth.getAuthToken(),new Join("WHITE",1));
        //getting list of games to compare
        expectedGames.clear();
        newGame = new Game(1, existingUser.getUsername(), "","newGame",new ChessGame());
        expectedGames.add(newGame);
        //check if game updated
        Assertions.assertEquals(expectedGames,create.listGames(auth.getAuthToken()),"The game was not updated correctly!");
    }
    @Test
    @Order(1)
    @DisplayName("Two Joins as White Test!")
    public void doubleWhiteGame() throws DataAccessException {
        //authorized user joins as White
        create.joinGame(auth.getAuthToken(),new Join("WHITE",1));
        //register test
        Auth newAuthToken = register.registerUser(newUser);
        actualUserDAO.createUser(hashedNewUser);
        //check if the user has been registered
        //user is in the /user database
        assertNewUser();
        //authToken is in the /auth database
        Assertions.assertEquals(newAuthToken, testAuthDAO.getAuth(newAuthToken.getAuthToken()), "authToken not registered in database.");
        //new authorized user also attempts to join as White;
        Assertions.assertThrows(
                DataAccessException.class,
                () -> create.joinGame(newAuthToken.getAuthToken(),new Join("WHITE",1)),
                "JoinGame not throwing exception (Attempted to join as white when already taken)!");
    }
}
