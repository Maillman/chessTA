package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
//Maybe generate test-comparison here?

public class ChessPiece {

    private final ChessGame.TeamColor pieceColor;
    private final PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    @Override
    public String toString() {
        return "ChessPiece{" +
                "pieceColor=" + pieceColor +
                ", type=" + type +
                '}';
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ChessPiece that)) {
            return false;
        }
        return pieceColor == that.pieceColor && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        ArrayList<ChessMove> pieceMoves = new ArrayList<>();
        switch (board.getPiece(myPosition).getPieceType()) {
            case BISHOP -> {
                directional("UpLeft",board,myPosition,pieceMoves);
                directional("UpRight",board,myPosition,pieceMoves);
                directional("DownLeft",board,myPosition,pieceMoves);
                directional("DownRight",board,myPosition,pieceMoves);
            }
            case ROOK -> {
                //Checked Improved Code
                directional("Up",board,myPosition,pieceMoves);
                directional("Down",board,myPosition,pieceMoves);
                directional("Left",board,myPosition,pieceMoves);
                directional("Right",board,myPosition,pieceMoves);

            }
            case QUEEN -> {
                directional("UpLeft",board,myPosition,pieceMoves);
                directional("UpRight",board,myPosition,pieceMoves);
                directional("DownLeft",board,myPosition,pieceMoves);
                directional("DownRight",board,myPosition,pieceMoves);
                directional("Up",board,myPosition,pieceMoves);
                directional("Down",board,myPosition,pieceMoves);
                directional("Left",board,myPosition,pieceMoves);
                directional("Right",board,myPosition,pieceMoves);
            }
            case KNIGHT -> {
                //System.out.println("This is a Knight!");
                //Start with the top L's
                knightL("Up",board,myPosition,pieceMoves);
                //Now with the bottom L's
                //knightL("Down",board,myPosition,pieceMoves);
                //Now with the left L's
                //knightL("Left",board,myPosition,pieceMoves);
                //Now with the right L's
                //knightL("Right",board,myPosition,pieceMoves);
            }
            case PAWN -> pawn(board,myPosition,pieceMoves);
            case KING -> king(board,myPosition,pieceMoves);
            default -> System.out.println("What is this piece?");
        }
        return pieceMoves;
    }
    private void knightL(String direction, ChessBoard board, ChessPosition myPosition, Collection<ChessMove> pieceMoves) {
        final int[][] moves = {
                {2, 1}, {2, -1}, {-2, 1}, {-2, -1},
                {1, 2}, {1, -2}, {-1, 2}, {-1, -2}
        };

        for (int[] move : moves) {
            int newRow = myPosition.getRow() + move[0];
            int newColumn = myPosition.getColumn() + move[1];

            if (isValidPosition(newRow, newColumn)) {
                ChessPosition newPos = new ChessPosition(newRow, newColumn);
                ChessPiece newPiece = board.getPiece(newPos);

                if (newPiece == null || newPiece.getTeamColor() != board.getPiece(myPosition).getTeamColor()) {
                    pieceMoves.add(new ChessMove(myPosition, newPos));
                }
            }
        }
    }

    private boolean isValidPosition(int row, int column) {
        return row >= 1 && row <= 8 && column >= 1 && column <= 8;
    }
    private void pawn(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> pieceMoves) {
        ChessPiece piece = board.getPiece(myPosition);
        int[] startPos = {myPosition.getRow(), myPosition.getColumn()};
        int[] curPos = startPos.clone();

        int forwardDirection = (piece.getTeamColor() == ChessGame.TeamColor.WHITE) ? 1 : -1;
        int startRow = (piece.getTeamColor() == ChessGame.TeamColor.WHITE) ? 2 : 7;
        int endRow = (piece.getTeamColor() == ChessGame.TeamColor.WHITE) ? 8 : 1;
        ChessPosition newPos;
        if(myPosition.getRow()==endRow){
            return;
        }
        pawnMoveForward(board,curPos,myPosition,pieceMoves,forwardDirection,endRow);
        //Start pawn moves forward twice
        if(myPosition.getRow()==startRow&&board.getPiece(new ChessPosition(curPos[0],curPos[1]))==null){
            pawnMoveForward(board,curPos,myPosition,pieceMoves,forwardDirection,endRow);
            curPos[0] -= forwardDirection;
        }

        // Check left capture
        curPos[1]--;
        newPos = new ChessPosition(curPos[0], curPos[1]);
        if (isValidPawnCapture(board, myPosition, newPos)) {
            addPawnMove(board, myPosition, newPos, pieceMoves, endRow);
        }

        // Check right capture
        curPos[1] += 2;
        newPos = new ChessPosition(curPos[0], curPos[1]);
        if (isValidPawnCapture(board, myPosition, newPos)) {
            addPawnMove(board, myPosition, newPos, pieceMoves, endRow);
        }
    }

    private void pawnMoveForward(ChessBoard board, int[] curPos, ChessPosition myPos, Collection<ChessMove> pieceMoves,int forward, int endRow){
        curPos[0] += forward;
        ChessPosition newPos = new ChessPosition(curPos[0], curPos[1]);
        addPawnMove(board, myPos, newPos, pieceMoves, endRow);
    }

    private void addPawnMove(ChessBoard board, ChessPosition myPosition, ChessPosition newPos, Collection<ChessMove> pieceMoves, int endRow) {
        ChessPiece newPiece = board.getPiece(newPos);
        if (newPiece == null||newPos.getColumn()!=myPosition.getColumn()) {
            if (newPos.getRow() == endRow) {
                pieceMoves.add(new ChessMove(myPosition, newPos, ChessPiece.PieceType.QUEEN));
                pieceMoves.add(new ChessMove(myPosition, newPos, ChessPiece.PieceType.ROOK));
                pieceMoves.add(new ChessMove(myPosition, newPos, ChessPiece.PieceType.BISHOP));
                pieceMoves.add(new ChessMove(myPosition, newPos, ChessPiece.PieceType.KNIGHT));
            } else {
                pieceMoves.add(new ChessMove(myPosition, newPos));
            }
        }
    }

    private boolean isValidPawnCapture(ChessBoard board, ChessPosition myPosition, ChessPosition newPos) {
        return isValidPosition(newPos.getRow(), newPos.getColumn()) &&
                board.getPiece(newPos) != null &&
                board.getPiece(newPos).getTeamColor() != board.getPiece(myPosition).getTeamColor();
    }
    private void king(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> pieceMoves) {
        int[][] directions = {
                {1, 0}, {1, 1}, {0, 1}, {-1, 1},
                {-1, 0}, {-1, -1}, {0, -1}, {1, -1}
        };

        for (int[] dir : directions) {
            int newRow = myPosition.getRow() + dir[0];
            int newCol = myPosition.getColumn() + dir[1];

            if (isValidPosition(newRow, newCol)) {
                ChessPosition newPos = new ChessPosition(newRow, newCol);
                ChessPiece pieceAtNewPos = board.getPiece(newPos);

                if (pieceAtNewPos == null || pieceAtNewPos.getTeamColor() != board.getPiece(myPosition).getTeamColor()) {
                    pieceMoves.add(new ChessMove(myPosition, newPos));
                }
            }
        }
    }

    private void directional(String direction, ChessBoard board, ChessPosition myPosition, Collection<ChessMove> pieceMoves){
        final int[] startPos = {myPosition.getRow(),myPosition.getColumn()};
        int[] curPos = startPos.clone();
        ChessPosition newPos;
        do{
            switch (direction) {
                case "Up" -> curPos[1]++;
                case "Down" -> curPos[1]--;
                case "Left" -> curPos[0]--;
                case "Right" -> curPos[0]++;
                case "UpLeft" -> {
                    curPos[1]++;
                    curPos[0]--;
                }
                case "UpRight" -> {
                    curPos[1]++;
                    curPos[0]++;
                }
                case "DownLeft" -> {
                    curPos[1]--;
                    curPos[0]--;
                }
                case "DownRight" -> {
                    curPos[1]--;
                    curPos[0]++;
                }
            }
            if((curPos[0]==0)||(curPos[0]==9)||(curPos[1]==0)||(curPos[1]==9)){
                break;
            }
            newPos = new ChessPosition(curPos[0],curPos[1]);
            if(((board.getPiece(newPos)!=null)&&(board.getPiece(newPos).getTeamColor()==board.getPiece(myPosition).getTeamColor()))){
                break;
            }
            pieceMoves.add(new ChessMove(myPosition,newPos));
        }while(!((board.getPiece(newPos)!=null)&&(board.getPiece(newPos).getTeamColor()!=board.getPiece(myPosition).getTeamColor())));
    }
}
