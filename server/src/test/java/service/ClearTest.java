package service;

import model.User;
import dataaccess.DataAccessException;
import org.junit.jupiter.api.*;

@SuppressWarnings("unused")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ClearTest extends BaseTest {
    @BeforeEach
    public void setup() throws DataAccessException {
        actualUserDAO.clear();
        actualAuthDAO.clear();
        actualGameDAO.clear();
    }
    @Test
    @Order(1)
    @DisplayName("Clear Service Test")
    public void clearData() throws DataAccessException {
        //create filler users
        newUser = new User("newUser","newPassword","new@Email.com");
        testUserDAO.createUser(newUser);
        newUser = new User("Maillman","MyBelovedPearl","WhitakerFamily@TheWorld.com");
        testUserDAO.createUser(newUser);
        newUser = new User("PhoenixFke","MyBelovedPearl","WhitakerFamily@TheWorld.com");
        testUserDAO.createUser(newUser);
        newUser = new User("Phoenix_Gamer","...21FDS-=_game","Leverage@TheWorld.com");
        testUserDAO.createUser(newUser);
        //check if the database is filled
        Assertions.assertNotEquals(actualUserDAO, testUserDAO, "The user database did not get filled");
        //clear the database
        clear.clear();
        //check if the database cleared
        Assertions.assertEquals(actualUserDAO.getUser(existingUser.getUsername()),testUserDAO.getUser(existingUser.getUsername()),"The user database did not clear!");
        Assertions.assertEquals(actualUserDAO.getUser(newUser.getUsername()),testUserDAO.getUser(newUser.getUsername()),"The user database did not clear!");
    }

    @Test
    @Order(2)
    @DisplayName("Multiple Clears")
    public void clearManyData() throws DataAccessException {
        //clear an empty database
        clear.clear();
        //create filler users
        newUser = new User("newUser","newPassword","new@Email.com");
        testUserDAO.createUser(newUser);
        newUser = new User("Maillman","MyBelovedPearl","WhitakerFamily@TheWorld.com");
        testUserDAO.createUser(newUser);
        //clear the database
        clear.clear();
        Assertions.assertEquals(actualUserDAO.getUser(existingUser.getUsername()),testUserDAO.getUser(existingUser.getUsername()),"The user database did not clear!");
        Assertions.assertEquals(actualUserDAO.getUser(newUser.getUsername()),testUserDAO.getUser(newUser.getUsername()),"The user database did not clear!");
        //create more filler users
        newUser = new User("Phoenix_Gamer","...21FDS-=_game","Leverage@TheWorld.com");
        testUserDAO.createUser(newUser);
        actualUserDAO.createUser(newUser);
        //check if the database is filled correctly post-clear
        Assertions.assertEquals(actualUserDAO.getUser(existingUser.getUsername()),testUserDAO.getUser(existingUser.getUsername()),"The user database did not clear!");
        Assertions.assertEquals(actualUserDAO.getUser(newUser.getUsername()),testUserDAO.getUser(newUser.getUsername()),"The user database did not clear!");
    }
}
