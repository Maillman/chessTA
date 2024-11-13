import model.*;

import chess.ChessBoard;
import chess.ChessGame;
import clienttoserver.ResponseException;
import org.junit.jupiter.api.*;
import clienttoserver.ServerFacade;
import server.Server;

import java.util.ArrayList;


public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;
    private static User user;
    private static Game game;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade("http://localhost:" + port);
        user = new User("player1", "password", "p1@email.com");
        game = new Game(1,null,null,"aGame",new ChessGame());
    }

    @AfterAll
    static void stopServer() throws ResponseException {
        facade.clear();
        server.stop();
    }

    @Test
    @BeforeEach
    void clear() throws Exception {
        facade.clear();
    }

    @Test
    void sendCrap() throws Exception {
        Game randomGame = new Game(10,"White","Hello There","",new ChessGame(ChessGame.TeamColor.WHITE,new ChessBoard()));
        Assertions.assertThrows(ResponseException.class, () -> facade.create(randomGame,"authToken"));
        Assertions.assertThrows(ResponseException.class, () -> facade.register(new User(null,null,null)));
        Assertions.assertThrows(ResponseException.class, () -> facade.login(new User("Hackerman","Malware","StealData")));
        Assertions.assertThrows(ResponseException.class, () -> facade.register(new User("Trying","CoolPassword",null)));
        Assertions.assertThrows(ResponseException.class, () -> facade.logout("yuyfghnhgtygfewufewi"));
        Assertions.assertThrows(ResponseException.class, () -> facade.join(new Join("WHITE",1000),"Familiar"));
    }
    @Test
    void register() throws Exception {
        var authData = facade.register(new User("player1", "password", "p1@email.com"));
        Assertions.assertTrue(authData.getAuthToken().length() > 10);
    }

    @Test
    void registerTwice() throws Exception {
        facade.register(user);
        Assertions.assertThrows(ResponseException.class, () -> facade.register(user));
    }

    @Test
    void logInvalid() throws Exception {
        Assertions.assertThrows(ResponseException.class, () -> facade.login(new User("NotWorthy","drowssap","fake@email.com")));
    }

    @Test
    void logoutInvalid() throws Exception {
        Assertions.assertThrows(ResponseException.class, () -> facade.logout("authToken"));
    }

    @Test
    void logoutLogin() throws Exception {
        var authData = facade.register(user);
        String registerToken = authData.getAuthToken();
        Assertions.assertTrue(authData.getAuthToken().length() > 10);
        facade.logout(authData.getAuthToken());
        authData = facade.login(user);
        Assertions.assertNotEquals(registerToken, authData.getAuthToken());
    }

    @Test
    void listWithoutAuthentication() throws Exception {
        Assertions.assertThrows(ResponseException.class, () -> facade.list("MyAuthenticationIsFALSE!"));
    }

    @Test
    void createList() throws Exception {
        var authData = facade.register(user);
        Assertions.assertTrue(authData.getAuthToken().length() > 10);
        Game anotherGame = new Game(2,null,null,"anotherGame",new ChessGame());
        ArrayList<Game> listOfGames = new ArrayList<>();
        listOfGames.add(game);
        listOfGames.add(anotherGame);
        Games theGames = new Games(listOfGames);
        facade.create(game,authData.getAuthToken());
        facade.create(anotherGame,authData.getAuthToken());
        Assertions.assertEquals(theGames,facade.list(authData.getAuthToken()),"The list of games is different than expected");
    }

    @Test
    void createWithoutAuthentication() throws Exception {
        Assertions.assertThrows(ResponseException.class, () -> facade.create(game,"FakeToken"));
    }

    @Test
    void createJoinList() throws Exception {
        var authData = facade.register(user);
        Assertions.assertTrue(authData.getAuthToken().length() > 10);
        Game aGame = new Game(1,null,null,"anotherGame",new ChessGame());
        facade.create(aGame,authData.getAuthToken());
        Join join = new Join("WHITE",1);
        aGame = facade.join(join,authData.getAuthToken());
        ArrayList<Game> listOfGames = new ArrayList<>();
        listOfGames.add(aGame);
        Games theGames = new Games(listOfGames);
        Assertions.assertEquals(user.getUsername(),aGame.getWhiteUsername(),"Player didn't join the game properly.");
        Assertions.assertEquals(theGames,facade.list(authData.getAuthToken()),"The list of games is different than expected");
    }

    @Test
    void joinWithoutAuthentication() throws Exception {
        var authData = facade.register(user);
        Assertions.assertTrue(authData.getAuthToken().length() > 10);
        Game aGame = new Game(1,null,null,"anotherGame",new ChessGame());
        facade.create(aGame,authData.getAuthToken());
        Join join = new Join("WHITE",1);
        facade.logout(authData.getAuthToken());
        Assertions.assertThrows(ResponseException.class, () -> facade.join(join, authData.getAuthToken()));
    }

    @Test
    void joinOutOfRange() throws Exception {
        var authData = facade.register(user);
        Assertions.assertTrue(authData.getAuthToken().length() > 10);
        Game aGame = new Game(1,null,null,"anotherGame",new ChessGame());
        facade.create(aGame,authData.getAuthToken());
        Join join = new Join("WHITE",16796893);
        Assertions.assertThrows(ResponseException.class, () -> facade.join(join,authData.getAuthToken()));
    }
    @Test
    void joinWhiteTwice() throws Exception {
        var authData = facade.register(user);
        Assertions.assertTrue(authData.getAuthToken().length() > 10);
        Game aGame = new Game(1,null,null,"anotherGame",new ChessGame());
        facade.create(aGame,authData.getAuthToken());
        Join join = new Join("WHITE",1);
        aGame = facade.join(join,authData.getAuthToken());
        ArrayList<Game> listOfGames = new ArrayList<>();
        listOfGames.add(aGame);
        Games theGames = new Games(listOfGames);
        Assertions.assertEquals(user.getUsername(),aGame.getWhiteUsername(),"Player didn't join the game properly.");
        Assertions.assertEquals(theGames,facade.list(authData.getAuthToken()),"The list of games is different than expected");
        Assertions.assertEquals(aGame,facade.join(join,authData.getAuthToken()),"White did not join the same game.");
    }
}
