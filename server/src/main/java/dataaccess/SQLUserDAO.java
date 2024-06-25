package dataaccess;

import model.User;

import java.sql.*;

import static java.sql.Statement.RETURN_GENERATED_KEYS;

public class SQLUserDAO implements UserDAO{

    public SQLUserDAO(){
        //MySQL Startup
        try{
            DatabaseManager database  = new DatabaseManager();
            database.initializeDatabase();
        }catch(DataAccessException dae){
            System.out.println("Error initializing database!");
        }
    }
    @Override
    public User getUser(String username) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement("select username, password, email from users where username=?", RETURN_GENERATED_KEYS)){
                preparedStatement.setString(1, username);
                try (var rs = preparedStatement.executeQuery()) {
                    rs.next();
                    var retUsername = rs.getString("username");
                    var retPassword = rs.getString("password");
                    var retEmail = rs.getString("email");

                    return new User(retUsername,retPassword,retEmail);
                }
            }
        }catch (SQLException se) {
            return null;
        }
    }

    @Override
    public void createUser(User user) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement("insert into users (username, password, email) values (?, ?, ?)", RETURN_GENERATED_KEYS)){
                preparedStatement.setString(1, user.getUsername());
                preparedStatement.setString(2, user.getPassword());
                preparedStatement.setString(3, user.getEmail());

                preparedStatement.executeUpdate();
            }
        }catch (SQLException se) {
            throw new DataAccessException(se.getMessage());
        }
    }

    @Override
    public void clear() throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement("truncate users")) {
                preparedStatement.executeUpdate();
            }
        }catch (SQLException se) {
            throw new DataAccessException(se.getMessage());
        }
    }
}
