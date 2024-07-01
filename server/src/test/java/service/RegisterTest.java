package service;

import model.Auth;
import dataaccess.DataAccessException;
import org.junit.jupiter.api.*;
import org.mindrot.jbcrypt.BCrypt;

@SuppressWarnings("unused")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RegisterTest extends BaseTest {
    @Test
    @Order(1)
    @DisplayName("Register User Test")
    public void registerUser() throws DataAccessException {
        //register test
        Auth auth = register.registerUser(newUser);
        actualUserDAO.createUser(hashedNewUser);
        //check if the user has been registered
        //user is in the /user database
        assertNewUser();
        //authToken is in the /auth database
        Assertions.assertEquals(auth, testAuthDAO.getAuth(auth.getAuthToken()), "authToken not registered in database.");
    }

    @Test
    @Order(2)
    @DisplayName("Register More than Once Test")
    public void registerTwice() throws DataAccessException {
        //register test
        Auth auth = register.registerUser(newUser);
        actualUserDAO.createUser(hashedNewUser);
        //check if the user has been registered
        //user is in the /user database
        assertNewUser();
        //authToken is in the /auth database
        Assertions.assertEquals(auth, testAuthDAO.getAuth(auth.getAuthToken()), "authToken not registered in database.");
        //attempt to register again
        Assertions.assertThrows(DataAccessException.class, () -> register.registerUser(newUser),"Register not throwing exception!");
    }
}
