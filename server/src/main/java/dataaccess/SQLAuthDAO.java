package dataaccess;

import model.Auth;

import java.sql.*;

import static java.sql.Statement.RETURN_GENERATED_KEYS;

public class SQLAuthDAO implements AuthDAO{
    public SQLAuthDAO(){
        //MySQL Startup
        try{
            DatabaseManager database  = new DatabaseManager();
            database.initializeDatabase();
        }catch(DataAccessException dae){
            System.out.println("Error initializing database!");
        }
    }
    @Override
    public Auth createAuth(String authToken, String username) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement("insert into auths (token, username) values (?, ?)", RETURN_GENERATED_KEYS)){
                preparedStatement.setString(1, authToken);
                preparedStatement.setString(2, username);

                preparedStatement.executeUpdate();

                return new Auth(authToken, username);
            }
        }catch (SQLException se) {
            throw new DataAccessException(se.getMessage());
        }
    }

    @Override
    public Auth getAuth(String authToken) throws DataAccessException{
        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement("select token, username from auths where token=?", RETURN_GENERATED_KEYS)){
                preparedStatement.setString(1, authToken);
                try (var rs = preparedStatement.executeQuery()) {
                    rs.next();
                    var retToken = rs.getString("token");
                    var retUsername = rs.getString("username");

                    return new Auth(retToken, retUsername);
                }
            }
        }catch (SQLException se) {
            return null;
        }
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException{
        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement("delete from auths where token=?")) {
                preparedStatement.setString(1, authToken);
                preparedStatement.executeUpdate();
            }
        }catch (SQLException se) {
            throw new DataAccessException(se.getMessage());
        }
    }

    @Override
    public void clear() throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement("truncate auths")) {
                preparedStatement.executeUpdate();
            }
        }catch (SQLException se) {
            throw new DataAccessException(se.getMessage());
        }
    }
}
