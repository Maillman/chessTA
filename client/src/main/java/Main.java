import chess.*;

public class Main {

    public static void main(String[] args) {
        String port;
        if(args.length>0) {
            port = args[0];
        }else{
            port = "http://localhost:8080";
        }
        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        System.out.println("♕ 240 Chess Client: " + piece);
        Client chessClient = new Client(port);
        chessClient.run();
    }
}