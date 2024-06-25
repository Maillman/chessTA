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
                if(authToken!=null){
                    try {
                        if(curGame!=null) {
                            server.leaveResign(curGame.getGameID(), false, authToken);
                            curGame = null;
                            System.out.println("You have successfully left the game.");
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
            case "help" -> {
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
            default -> {
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
        }
    }
    private boolean evalPreLogin(String[] result) {
        try {
            boolean isSuccessful = true;
            switch (result[0]) {
                case "register" -> {
                    if (result.length >= 4) {
                        authToken = server.register(new User(result[1], result[2], result[3])).getAuthToken();
                        System.out.println("You have successfully registered and logged in!");
                        postLoginUI();
                    } else {
                        System.out.println("Not enough arguments where expected (Expected 4, Got " + result.length + ").");
                        System.out.println("Register <USERNAME> <PASSWORD> <EMAIL>");
                    }
                }
                case "login" -> {
                    if (result.length >= 3) {
                        authToken = server.login(new User(result[1], result[2], null)).getAuthToken();
                        System.out.println("You have successfully logged in!");
                        postLoginUI();
                    } else {
                        System.out.println("Not enough arguments where expected (Expected 3, Got " + result.length + ").");
                        System.out.println("Login <USERNAME> <PASSWORD>");
                    }
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
    private boolean evalPostLogin(String[] result){
        try {
            boolean isSuccessful = true;
            switch (result[0]) {
                case "logout" -> {
                    server.logout(authToken);
                    authToken = null;
                    System.out.println("You have successfully logged out!");
                    preLoginUI();
                }
                case "list" -> {
                    List<Game> games = server.list(authToken).getGames();
                    listGames(games);
                }
                case "create" -> {
                    if (result.length >= 2) {
                        Game game = server.create(new Game(null,null,null,result[1],new ChessGame()),authToken);
                        System.out.println("The Game " + result[1] + " was created! It's ID is " + game.getGameID() + ".");
                    } else {
                        System.out.println("Not enough arguments where expected (Expected 2, Got " + result.length + ").");
                        System.out.println("Create <GAMENAME>");
                    }
                }
                case "join" -> {
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
                case "observe" -> {
                    if(result.length >= 2) {
                        joinObserve(result);
                    }else{
                        System.out.println("Not enough arguments where expected (Expected 2, Got " + result.length + ").");
                        System.out.println("Observe <ID>");
                    }
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
    private boolean evalGameplay(String[] result) {
        try {
            boolean isSuccessful = true;
            switch (result[0]) {
                case "redraw" -> {
                    clientDrawChessBoard(curGame);
                }
                case "highlight" -> {
                    if(result.length >= 2){
                        char[] pos = result[1].toCharArray();
                        var out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
                        ChessBoardUI.drawChessBoard(out,curGame.getGame(), persp,evalPos(pos));
                    }
                }
                case "leave" -> {
                    server.leaveResign(curGame.getGameID(), false, authToken);
                    curGame = null;
                    System.out.println("You have successfully left the game.");
                }
                case "move" -> {
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
            ChessPiece.PieceType[] possiblePieceTypes = new ChessPiece.PieceType[]{ChessPiece.PieceType.QUEEN, ChessPiece.PieceType.ROOK, ChessPiece.PieceType.BISHOP, ChessPiece.PieceType.KNIGHT};
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
        System.out.println("Help: List possible commands.");
        System.out.println("Register <USERNAME> <PASSWORD> <EMAIL>: Creates an account to interact with the server.");
        System.out.println("Login <USERNAME> <PASSWORD>: Allows the ability to create, join, and list Chess Games in the server.");
        System.out.println("Quit: Exit the program.");
    }
    private void helpPostLogin(){
        System.out.println("Help: List possible commands.");
        System.out.println("Logout: Log out of the server.");
        System.out.println("List: Lists all of the games that currently exist in the server.");
        System.out.println("Create <GAMENAME>: Creates a game of chess to join and play.");
        System.out.println("Join <ID> [WHITE|BLACK|<empty>]: Allows the ability to play (or observe if no color is specified) the specified game of chess.");
        System.out.println("Observe <ID>: Allows the ability to observe the specified game of chess.");
        System.out.println("Quit: Exit the program.");
    }
    private void helpGameplay(){
        System.out.println("Help: List possible commands.");
        System.out.println("Redraw: Redraws the chess board.");
        System.out.println("Leave: Leave the current game.");
        System.out.println("Move <CHESSMOVE>: Make a move in the current game, moving a piece from starting position to ending position (E.G.: 'Move e4e6'");
        System.out.println("Resign: Forfeit the game (The game will immediately end!).");
        System.out.println("Highlight <CHESSPOSITION>: Highlights all the legal moves of the chess piece at the chosen chess position.");
        System.out.println("Quit: Exit the program.");
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
