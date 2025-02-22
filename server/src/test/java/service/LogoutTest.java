package service;

import model.Auth;
import dataaccess.DataAccessException;
import org.junit.jupiter.api.*;

@SuppressWarnings("unused")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LogoutTest extends BaseTest {
    @Test
    @Order(1)
    @DisplayName("Logout Existing User Test!")
    public void logoutUser() throws DataAccessException {
        //login test
        Auth auth = login.login(existingUser);
        //check if the user has been logged-in
        //user is in the /user database
        assertExistingUser();
        //authToken is in the /auth database
        Assertions.assertEquals(auth, testAuthDAO.getAuth(auth.getAuthToken()), "authToken not registered in database.");
        login.logout(auth.getAuthToken());
        //check if the user has logged-out
        Assertions.assertNotEquals(auth, testAuthDAO.getAuth(auth.getAuthToken()), "User didn't logout.");
    }
    @Test
    @Order(1)
    @DisplayName("Unauthorized Logout Test!")
    public void invalidLogout() {
        //attempt to log out with a random auth token
        String invalidToken = "WHAT90235A329WONDERFUL328592386";
        Assertions.assertThrows(DataAccessException.class, () -> login.logout(invalidToken),"Logout not throwing exception!");
    }
}
