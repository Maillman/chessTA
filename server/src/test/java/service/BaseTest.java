package service;

import Model.User;
import dataaccess.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.mindrot.jbcrypt.BCrypt;

public abstract class BaseTest {
    protected static User existingUser;
    protected static User hashedExistingUser;

    protected static User newUser;
    protected static User hashedNewUser;
    protected static UserDAO testUserDAO;
    protected static AuthDAO testAuthDAO;
    protected static GameDAO testGameDAO;
    protected static UserDAO actualUserDAO;
    protected static AuthDAO actualAuthDAO;
    protected static GameDAO actualGameDAO;
    protected static UserService register;
    protected static UserService login;
    protected static GameService create;
    protected static ClearService clear;
    @BeforeAll
    public static void init() {
        //SQLDatabase -> MemoryDatabase for now....
        testUserDAO = new MemoryUserDAO();
        testAuthDAO = new MemoryAuthDAO();
        testGameDAO = new MemoryGameDAO();
        //MemoryDatabase
        actualUserDAO = new MemoryUserDAO();
        actualAuthDAO = new MemoryAuthDAO();
        actualGameDAO = new MemoryGameDAO();
        String hashedPassword = BCrypt.hashpw("existingUserPassword",BCrypt.gensalt());
        existingUser = new User("ExistingUser","existingUserPassword","eu@mail.com");
        hashedExistingUser = new User("ExistingUser",hashedPassword,"eu@mail.com");
        hashedPassword = BCrypt.hashpw("newPassword",BCrypt.gensalt());
        newUser = new User("newUser","newPassword","new@Email.com");
        hashedNewUser = new User("newUser",hashedPassword,"new@Email.com");
    }
    @BeforeEach
    public void setUp() throws DataAccessException {
        testUserDAO.clear();
        testAuthDAO.clear();
        testGameDAO.clear();
        actualUserDAO.clear();
        actualAuthDAO.clear();
        actualGameDAO.clear();
        actualUserDAO.createUser(hashedExistingUser);
        testUserDAO.createUser(hashedExistingUser);
        register = new UserService(testUserDAO,testAuthDAO);
        login = new UserService(testUserDAO, testAuthDAO);
        create = new GameService(testAuthDAO, testGameDAO);
        clear = new ClearService(testUserDAO, testAuthDAO, testGameDAO);
    }
    @AfterAll
    public static void shutDown() throws DataAccessException {
        testUserDAO.clear();
        testAuthDAO.clear();
        testGameDAO.clear();
        actualUserDAO.clear();
        actualAuthDAO.clear();
        actualGameDAO.clear();
    }
}
