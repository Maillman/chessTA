import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

import model.*;
import chess.*;
import clienttoserver.ResponseException;
import clienttoserver.ServerFacade;
import clienttoserver.ServerMessageObserver;
import ui.*;
import websocket.messages.ServerMessage;

import static ui.ChessBoardUI.Perspective.*;

public class Client implements ServerMessageObserver {
    private ServerFacade server;
    private String authToken = null;
    private Game curGame = null;

    private ChessBoardUI.Perspective persp;

    public Client(String serverURL) {
        try {
            server = new ServerFacade(serverURL, this);
        }catch(ResponseException re) {
            System.out.println("An error with WebSocket has occurred:");
            System.out.println(re.getMessage());
            server = new ServerFacade(serverURL);
        }
    }

    public void run(){
        Scanner scanner = new Scanner(System.in);
        var result = "";
        System.out.println();
        System.out.println("Welcome to the chess client! ♘");
        preLoginUI();
        while(!result.equals("quit")){
            printPrompt();
            String line = scanner.nextLine();
            try {
                String[] splitResult = line.split(" ");
                splitResult[0] = splitResult[0].toLowerCase();
                evaluate(splitResult);
                result = splitResult[0];
            } catch (Throwable e){
                var msg = e.toString();
                System.out.println(msg);
            }
        }
    }

    private void evaluate(String[] result) {
        switch (result[0]) {
            case "quit" -> {
                quitLogic();
            }
            case "help" -> {
                helpLogic();
            }
            default -> {
                defaultLogic(result);
            }
        }
    }

    private void defaultLogic(String[] result) {
        boolean validCMD;
        if(authToken==null){
            validCMD = evalPreLogin(result);
        }else{
            if(curGame==null) {
                validCMD = evalPostLogin(result);
            }else{
                validCMD = evalGameplay(result);
            }
        }
        if(!validCMD){
            System.out.println("That is not a valid command!");
            evaluate(new String[]{"help"});
        }
    }

    private void helpLogic() {
        if(authToken==null) {
            preLoginUI();
        }else{
            if(curGame==null){
                postLoginUI();
            }else{
                gameplayUI();
            }
        }
    }

    private void quitLogic() {
        if(authToken!=null){
            try {
                if(curGame!=null) {
                    leaveLogic();
                }
                server.logout(authToken);
                authToken = null;
                System.out.println("You have successfully logged out!");
                System.out.println();
            }catch(ResponseException re){
                System.out.println(re.getMessage());
                evaluate(new String[]{"help"});
            }
        }
        System.out.println("You have exited the program!");
    }

    private boolean evalPreLogin(String[] result) {
        try {
            boolean isSuccessful = true;
            switch (result[0]) {
                case "register" -> {
                    registerLogic(result);
                }
                case "login" -> {
                    loginLogic(result);
                }
                default -> {
                    isSuccessful = false;
                }
            }
            return isSuccessful;
        }catch(ResponseException re){
            System.out.println(re.getMessage());
            evaluate(new String[]{"help"});
            return true;
        }
    }

    private void loginLogic(String[] result) throws ResponseException {
        if (result.length >= 3) {
            authToken = server.login(new User(result[1], result[2], null)).getAuthToken();
            System.out.println("You have successfully logged in!");
            postLoginUI();
        } else {
            System.out.println("Not enough arguments where expected (Expected 3, Got " + result.length + ").");
            System.out.println("Login <USERNAME> <PASSWORD>");
        }
    }

    private void registerLogic(String[] result) throws ResponseException {
        if (result.length >= 4) {
            authToken = server.register(new User(result[1], result[2], result[3])).getAuthToken();
            System.out.println("You have successfully registered and logged in!");
            postLoginUI();
        } else {
            System.out.println("Not enough arguments where expected (Expected 4, Got " + result.length + ").");
            System.out.println("Register <USERNAME> <PASSWORD> <EMAIL>");
        }
    }

    private boolean evalPostLogin(String[] result){
        try {
            boolean isSuccessful = true;
            switch (result[0]) {
                case "logout" -> {
                    logoutLogic();
                }
                case "list" -> {
                    listLogic();
                }
                case "create" -> {
                    createLogic(result);
                }
                case "join" -> {
                    joinLogic(result);
                }
                case "observe" -> {
                    observeLogic(result);
                }
                default -> {
                    isSuccessful = false;
                }
            }
            return isSuccessful;
        }catch(ResponseException re){
            System.out.println(re.getMessage());
            evaluate(new String[]{"help"});
            return true;
        }
    }

    private void observeLogic(String[] result) throws ResponseException {
        if(result.length >= 2) {
            joinObserve(result);
        }else{
            System.out.println("Not enough arguments where expected (Expected 2, Got " + result.length + ").");
            System.out.println("Observe <ID>");
        }
    }

    private void joinLogic(String[] result) throws ResponseException {
        if (result.length >= 3) {
            joinObserve(result);
        }else if(result.length == 2){
            result[0] = "observe";
            joinObserve(result);
        }else{
            System.out.println("Not enough arguments where expected (Expected 3 (or 2 if observing), Got " + result.length + ").");
            System.out.println("Join <ID> [WHITE|BLACK|<empty>]");
        }
    }

    private void createLogic(String[] result) throws ResponseException {
        if (result.length >= 2) {
            Game game = server.create(new Game(null,null,null, result[1],new ChessGame()),authToken);
            System.out.println("The Game " + result[1] + " was created! It's ID is " + game.getGameID() + ".");
        } else {
            System.out.println("Not enough arguments where expected (Expected 2, Got " + result.length + ").");
            System.out.println("Create <GAMENAME>");
        }
    }

    private void listLogic() throws ResponseException {
        List<Game> games = server.list(authToken).getGames();
        listGames(games);
    }

    private void logoutLogic() throws ResponseException {
        server.logout(authToken);
        authToken = null;
        System.out.println("You have successfully logged out!");
        preLoginUI();
    }

    private boolean evalGameplay(String[] result) {
        try {
            boolean isSuccessful = true;
            switch (result[0]) {
                case "redraw" -> {
                    clientDrawChessBoard(curGame);
                }
                case "highlight" -> {
                    highlightLogic(result);
                }
                case "leave" -> {
                    leaveLogic();
                }
                case "move" -> {
                    moveLogic(result);
                }
                case "resign" -> {
                    server.leaveResign(curGame.getGameID(), true, authToken);
                }
                default -> {
                    isSuccessful = false;
                }
            }
            return isSuccessful;
        }
        catch(ResponseException re){
            System.out.println(re.getMessage());
            evaluate(new String[]{"help"});
            return true;
        }
    }

    private void moveLogic(String[] result) throws ResponseException {
        if(result.length >= 2){
            char[] move = result[1].toCharArray();
            try {
                curGame.getGame().makeMove(evalMove(move));
                server.move(curGame.getGameID(), evalMove(move), authToken);
            }catch(InvalidMoveException ime){
                System.out.println("\n" + EscapeSequences.SET_TEXT_COLOR_RED + ime.getMessage());
            }
        }else{
            System.out.println("Not enough arguments where expected (Expected 2, Got " + result.length + ").");
            System.out.println("Move <CHESSMOVE>");
        }
    }

    private void leaveLogic() throws ResponseException {
        server.leaveResign(curGame.getGameID(), false, authToken);
        curGame = null;
        System.out.println("You have successfully left the game.");
    }

    private void highlightLogic(String[] result) {
        if(result.length >= 2){
            char[] pos = result[1].toCharArray();
            var out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
            ChessBoardUI.drawChessBoard(out,curGame.getGame(), persp,evalPos(pos));
        }
    }

    char[] possibleColMoves = new char[]{'a','b','c','d','e','f','g','h'};
    char[] possibleRowMoves = new char[]{'1','2','3','4','5','6','7','8'};
    char[] possiblePromotionPieces = new char[]{'Q','R','B','N'};
    private ChessPosition evalPos(char[] moveInChars){
        int row = 0, col = 0;
        for (int i = 0; i < possibleColMoves.length; i++) {
            if (possibleColMoves[i] == moveInChars[0]) {
                col = i+1;
            }
        }
        for (int i = 0; i < possibleRowMoves.length; i++) {
            if (possibleRowMoves[i] == moveInChars[1]) {
                row = i+1;
            }
        }
        return new ChessPosition(row,col);
    }

    private ChessMove evalMove(char[] moveInChars){
        ChessPosition startPos = evalPos(Arrays.copyOfRange(moveInChars,0,2));
        ChessPosition endPos = evalPos(Arrays.copyOfRange(moveInChars,2,4));
        if(moveInChars.length>=5){
            ChessPiece.PieceType[] possiblePieceTypes = new ChessPiece.PieceType[]{
                    ChessPiece.PieceType.QUEEN,
                    ChessPiece.PieceType.ROOK,
                    ChessPiece.PieceType.BISHOP,
                    ChessPiece.PieceType.KNIGHT
            };
            for(int i = 0; i < possiblePromotionPieces.length; i++) {
                if (possiblePromotionPieces[i] == moveInChars[4]) {
                    ChessPiece.PieceType piecePromotion = possiblePieceTypes[i];
                    return new ChessMove(startPos,endPos,piecePromotion);
                }
            }
        }
        return new ChessMove(startPos,endPos);
    }

    private void listGames(List<Game> games){
        System.out.println("Here are the List of Games!");
        for (Game selGame : games) {
            System.out.print(EscapeSequences.SET_TEXT_COLOR_YELLOW);
            System.out.println("Game ID: " + selGame.getGameID());
            System.out.print(EscapeSequences.SET_TEXT_COLOR_BLUE);
            System.out.println("Game Name: " + selGame.getGameName());
            System.out.println("White Player: " + selGame.getWhiteUsername() + ", Black Player: " + selGame.getBlackUsername());
            ChessBoardUI.Perspective holdPersp = persp;
            persp = WHITE;
            clientDrawChessBoard(selGame);
            persp = holdPersp;
            System.out.print(EscapeSequences.SET_TEXT_COLOR_WHITE);
            System.out.println();
        }
    }

    private void joinObserve(String[] result) throws ResponseException {
        Join join;
        Game game;
        if(Objects.equals(result[0], "join")){
            join = new Join(result[2],Integer.parseInt(result[1]));
            game = server.join(join,authToken);
        }else{
            join = new Join(null,Integer.parseInt(result[1]));
            curGame = server.join(join,authToken);
            System.out.println("You are observing the game");
        }
        if(Objects.equals(join.getPlayerColor(), "WHITE")){
            persp = WHITE;
        }else if(Objects.equals(join.getPlayerColor(), "BLACK")){
            persp = BLACK;
        }else{
            persp = WATCH;
        }
        //clientDrawChessBoard(game);
    }

    private void clientDrawChessBoard(Game game) {
        var out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        ChessBoardUI.drawChessBoard(out,game.getGame(), persp, null);
    }
    private void gameplayUI() {
        uiPreHelper();
        helpGameplay();
        uiPostHelper();
    }

    private void postLoginUI() {
        uiPreHelper();
        helpPostLogin();
        uiPostHelper();
    }

    private void preLoginUI() {
        uiPreHelper();
        helpPreLogin();
        uiPostHelper();
    }
    private void uiPreHelper(){
        System.out.println("Execute any of the commands below!");
        System.out.print(EscapeSequences.SET_TEXT_COLOR_BLUE);
        System.out.println();
    }
    private void uiPostHelper(){
        System.out.print(EscapeSequences.SET_TEXT_COLOR_WHITE);
    }
    private void helpPreLogin(){
        String[] preLoginMenu = new String[]{
                "Help: List possible commands.",
                "Register <USERNAME> <PASSWORD> <EMAIL>: Creates an account to interact with the server.",
                "Login <USERNAME> <PASSWORD>: Allows the ability to create, join, and list Chess Games in the server.",
                "Quit: Exit the program."
        };
        for (String preLoginCommand : preLoginMenu) {
            System.out.println(preLoginCommand);
        }
    }
    private void helpPostLogin(){
        String[] postLoginMenu = new String[]{
                "Help: List possible commands.",
                "Logout: Log out of the server.",
                "List: Lists all of the games that currently exist in the server.",
                "Create <GAMENAME>: Creates a game of chess to join and play.",
                "Join <ID> [WHITE|BLACK|<empty>]: Allows the ability to play (or observe if no color is specified) the specified game of chess.",
                "Observe <ID>: Allows the ability to observe the specified game of chess.",
                "Quit: Exit the program."
        };
        for (String postLoginCommand : postLoginMenu) {
            System.out.println(postLoginCommand);
        }
    }
    private void helpGameplay(){
        String[] gameplayMenu = new String[]{
                "Help: List possible commands.",
                "Redraw: Redraws the chess board.",
                "Leave: Leave the current game.",
                "Move <CHESSMOVE>: Make a move in the current game, moving a piece from starting position to ending position (E.G.: 'Move e4e6')",
                "Resign: Forfeit the game (The game will immediately end!).",
                "Highlight <CHESSPOSITION>: Highlights all the legal moves of the chess piece at the chosen chess position.",
                "Quit: Exit the program."
        };
        for (String gameplayCommand : gameplayMenu) {
            System.out.println(gameplayCommand);
        }
    }
    private void printPrompt() {
        System.out.print("\n" + EscapeSequences.SET_TEXT_COLOR_GREEN + " >>> " + EscapeSequences.SET_TEXT_COLOR_WHITE);
    }

    @Override
    public void notify(ServerMessage message) {
        if(message.getServerMessageType() == ServerMessage.ServerMessageType.NOTIFICATION) {
            System.out.println("\n" + EscapeSequences.SET_TEXT_COLOR_RED + message.getMessage());
        }else if(message.getServerMessageType() == ServerMessage.ServerMessageType.LOAD_GAME){
            if(curGame==null){
                System.out.println("You have successfully joined the game!");
            }
            curGame = message.getGame();
            clientDrawChessBoard(curGame);
            gameplayUI();
        }else if(message.getServerMessageType() == ServerMessage.ServerMessageType.ERROR){
            System.out.println("\n" + EscapeSequences.SET_TEXT_COLOR_RED + message.getErrorMessage());
        }
        printPrompt();
    }
}
