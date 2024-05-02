package chess;

import java.util.Arrays;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChessBoard that)) return false;
        return Arrays.deepEquals(squares, that.squares);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(squares);
    }

    @Override
    public String toString() {
        return "ChessBoard{" +
                "squares=" + Arrays.deepToString(squares) +
                '}';
    }

    private ChessPiece[][] squares = new ChessPiece[8][8];

    public boolean isCastleQueenSideW() {
        return castleQueenSideW;
    }

    public void setCastleQueenSideW(boolean castleQueenSideW) {
        this.castleQueenSideW = castleQueenSideW;
    }

    public boolean isCastleKingSideW() {
        return castleKingSideW;
    }

    public void setCastleKingSideW(boolean castleKingSideW) {
        this.castleKingSideW = castleKingSideW;
    }

    public boolean isCastleQueenSideB() {
        return castleQueenSideB;
    }

    public void setCastleQueenSideB(boolean castleQueenSideB) {
        this.castleQueenSideB = castleQueenSideB;
    }

    public boolean isCastleKingSideB() {
        return castleKingSideB;
    }

    public void setCastleKingSideB(boolean castleKingSideB) {
        this.castleKingSideB = castleKingSideB;
    }

    boolean castleQueenSideW = true, castleKingSideW = true;
    boolean castleQueenSideB = true, castleKingSideB = true;
    public ChessBoard() {
        
    }

    public ChessBoard(ChessBoard board){
        for(int i = 0; i < 8; i++) {
            for(int j = 0; j < 8; j++) {
                squares[i][j] = board.squares[i][j];
            }
        }
    }
    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        squares[position.getRow()-1][position.getColumn()-1] = piece;
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        return squares[position.getRow()-1][position.getColumn()-1];
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        ChessGame.TeamColor color = ChessGame.TeamColor.WHITE;
        int rank = 0;
        //White-Side
        squares[rank][0] = new ChessPiece(color, ChessPiece.PieceType.ROOK);
        squares[rank][7] = new ChessPiece(color, ChessPiece.PieceType.ROOK);
        squares[rank][1] = new ChessPiece(color, ChessPiece.PieceType.KNIGHT);
        squares[rank][6] = new ChessPiece(color, ChessPiece.PieceType.KNIGHT);
        squares[rank][2] = new ChessPiece(color, ChessPiece.PieceType.BISHOP);
        squares[rank][5] = new ChessPiece(color, ChessPiece.PieceType.BISHOP);
        squares[rank][3] = new ChessPiece(color, ChessPiece.PieceType.QUEEN);
        squares[rank][4] = new ChessPiece(color, ChessPiece.PieceType.KING);
        rank++;
        for (int i = 0; i < 8; i++) {
            squares[rank][i] = new ChessPiece(color, ChessPiece.PieceType.PAWN);
        }
        //Black-Side
        color = ChessGame.TeamColor.BLACK;
        rank = 7;
        squares[rank][0] = new ChessPiece(color, ChessPiece.PieceType.ROOK);
        squares[rank][7] = new ChessPiece(color, ChessPiece.PieceType.ROOK);
        squares[rank][1] = new ChessPiece(color, ChessPiece.PieceType.KNIGHT);
        squares[rank][6] = new ChessPiece(color, ChessPiece.PieceType.KNIGHT);
        squares[rank][2] = new ChessPiece(color, ChessPiece.PieceType.BISHOP);
        squares[rank][5] = new ChessPiece(color, ChessPiece.PieceType.BISHOP);
        squares[rank][3] = new ChessPiece(color, ChessPiece.PieceType.QUEEN);
        squares[rank][4] = new ChessPiece(color, ChessPiece.PieceType.KING);
        rank--;
        for (int i = 0; i < 8; i++) {
            squares[rank][i] = new ChessPiece(color, ChessPiece.PieceType.PAWN);
        }
        castleKingSideW = true;
        castleQueenSideW = true;
        castleKingSideB = true;
        castleQueenSideB = true;

    }

}
