package chess;

import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    TeamColor turn;
    ChessBoard theBoard;
    ChessGame preGame;

    boolean enPassantL = false, enPassantR = false;
    public ChessGame() {
        theBoard = new ChessBoard();
        theBoard.resetBoard();
        turn = TeamColor.WHITE;
    }

    public ChessGame(TeamColor team, ChessBoard board){
        turn = team;
        theBoard = new ChessBoard(board);
    }
    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return turn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        turn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = theBoard.getPiece(startPosition);
        if (piece == null) {
            return null;
        }

        ArrayList<ChessMove> moves = new ArrayList<>(piece.pieceMoves(theBoard, startPosition));
        HashSet<ChessMove> validMoves = new HashSet<>();

        if (piece.getPieceType() == ChessPiece.PieceType.KING && startPosition.getColumn() == 5) {
            handleCastling(startPosition, validMoves);
        }

        if (piece.getPieceType() == ChessPiece.PieceType.PAWN) {
            handleEnPassant(startPosition, piece.getTeamColor(), moves);
        }

        for (ChessMove pieceMove : moves) {
            if (isValidMove(startPosition, pieceMove)) {
                validMoves.add(pieceMove);
            }
        }
        return validMoves;
    }

    private void handleCastling(ChessPosition startPosition, Collection<ChessMove> validMoves) {
        ChessPosition kingSide = new ChessPosition(startPosition.getRow(), startPosition.getColumn() + 1);
        ChessPosition queenSide = new ChessPosition(startPosition.getRow(), startPosition.getColumn() - 1);
        ChessPosition kingCastle = new ChessPosition(startPosition.getRow(), startPosition.getColumn() + 2);
        ChessPosition queenCastle = new ChessPosition(startPosition.getRow(), startPosition.getColumn() - 2);
        ChessPosition rookPos;
        ChessMove castleKMove = new ChessMove(startPosition,kingSide);
        ChessMove castleQMove = new ChessMove(startPosition,queenSide);
        ChessMove kingCastling = new ChessMove(startPosition,kingCastle);
        ChessMove queenCastling = new ChessMove(startPosition,queenCastle);
        switch (theBoard.getPiece(startPosition).getTeamColor()) {
            case WHITE -> {
                rookPos = new ChessPosition(startPosition.getRow(), 8);
                handleCastlingForColor(startPosition, rookPos, castleKMove, kingCastling, validMoves, theBoard.isCastleKingSideW());
                rookPos = new ChessPosition(startPosition.getRow(), 1);
                handleCastlingForColor(startPosition, rookPos, castleQMove, queenCastling, validMoves, theBoard.isCastleQueenSideW());
            }
            case BLACK -> {
                rookPos = new ChessPosition(startPosition.getRow(), 8);
                handleCastlingForColor(startPosition, rookPos, castleKMove, kingCastling, validMoves, theBoard.isCastleKingSideB());
                rookPos = new ChessPosition(startPosition.getRow(), 1);
                handleCastlingForColor(startPosition, rookPos, castleQMove, queenCastling, validMoves, theBoard.isCastleQueenSideB());
            }
        }
    }

    private void handleCastlingForColor(ChessPosition startPosition, ChessPosition rookPos, ChessMove chessMove, ChessMove castleMove, Collection<ChessMove> validMoves, boolean canCastle) {
        ChessGame checkGame = new ChessGame(theBoard.getPiece(startPosition).getTeamColor(), theBoard);
        if (canCastle && isValidMove(startPosition, chessMove) && isValidMove(startPosition,castleMove)) {
            checkGame.getBoard().addPiece(chessMove.getEndPosition(), checkGame.getBoard().getPiece(startPosition));
            checkGame.getBoard().addPiece(startPosition, null);
            if (!checkGame.isInCheck(theBoard.getPiece(startPosition).getTeamColor()) && theBoard.getPiece(rookPos) != null && theBoard.getPiece(chessMove.getEndPosition())==null && validMoves(rookPos).contains(new ChessMove(rookPos, chessMove.getEndPosition()))) {
                validMoves.add(castleMove);
            }
        }
    }

    private void handleEnPassant(ChessPosition startPosition, TeamColor teamColor, Collection<ChessMove> moves) {
        int rowDirection = (teamColor == TeamColor.WHITE) ? 1 : -1;
        int enPassantRow = (teamColor == TeamColor.WHITE) ? 5 : 4;
        int leftColumn = startPosition.getColumn() - 1;
        int rightColumn = startPosition.getColumn() + 1;

        if (startPosition.getRow() == enPassantRow) {
            addEnPassantMove(startPosition, rowDirection, leftColumn, moves);
            addEnPassantMove(startPosition, rowDirection, rightColumn, moves);
        }
    }

    private void addEnPassantMove(ChessPosition startPosition, int rowDirection, int column, Collection<ChessMove> moves) {
        if (column >= 1 && column <= 8) {
            ChessPosition enPassantPosition = new ChessPosition(startPosition.getRow() + rowDirection, column);
            ChessPosition postPosition = new ChessPosition(startPosition.getRow(), column);
            ChessPiece enPassantPiece = theBoard.getPiece(startPosition);
            ChessPiece postPiece = theBoard.getPiece(postPosition);

            if (enPassantPiece != null && enPassantPiece.getPieceType() == ChessPiece.PieceType.PAWN && postPiece != null && preGame.getBoard().getPiece(postPosition) == null) {
                moves.add(new ChessMove(startPosition, enPassantPosition));
                if(startPosition.getColumn()<column){
                    enPassantR = true;
                }else{
                    enPassantL = true;
                }
            }
        }
    }

    private boolean isValidMove(ChessPosition startPosition, ChessMove move) {
        ChessGame checkGame = new ChessGame(theBoard.getPiece(startPosition).getTeamColor(), theBoard);
        checkGame.getBoard().addPiece(move.getEndPosition(), checkGame.getBoard().getPiece(move.getStartPosition()));
        checkGame.getBoard().addPiece(move.getStartPosition(), null);
        return !checkGame.isInCheck(theBoard.getPiece(startPosition).getTeamColor());
    }


    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        if(theBoard.getPiece(move.getStartPosition())==null){
            throw new InvalidMoveException("Invalid Move: No piece found!");
        }
        ArrayList<ChessMove> validMoves = new ArrayList<>(validMoves(move.getStartPosition()));
        if(!validMoves.contains(move)){
            throw new InvalidMoveException("Invalid Move: Piece can't go there!");
        }
        if(theBoard.getPiece(move.getStartPosition()).getTeamColor()!=turn){
            throw new InvalidMoveException("Invalid Move: Piece move out of order!");
        }
        if(!isValidMove(move.getStartPosition(),move)){
            throw new InvalidMoveException("Invalid Move: In check!");
        }
        //Makes the move.
        preGame = new ChessGame(turn, theBoard);
        if(move.getPromotionPiece()!=null){
            theBoard.addPiece(move.getEndPosition(), new ChessPiece(turn,move.getPromotionPiece()));
        }else {
            theBoard.addPiece(move.getEndPosition(), theBoard.getPiece(move.getStartPosition()));
        }
        theBoard.addPiece(move.getStartPosition(), null);
        //Handle special cases
        makeCastlingMove(move);
        checkCastling(move);
        checkEnPassant(move);
        //Assuming the move is valid, and made. Switch turns.
        switch (turn) {
            case WHITE -> setTeamTurn(TeamColor.BLACK);
            case BLACK -> setTeamTurn(TeamColor.WHITE);
        }
    }

    private void makeCastlingMove(ChessMove move){
        if((preGame.getBoard().getPiece(move.getStartPosition())!=null)&&(preGame.getBoard().getPiece(move.getStartPosition()).getPieceType()== ChessPiece.PieceType.KING)&&((move.getEndPosition().getColumn()-move.getStartPosition().getColumn()==2))){
            theBoard.addPiece(new ChessPosition(move.getStartPosition().getRow(),move.getStartPosition().getColumn()+1),new ChessPiece(turn, ChessPiece.PieceType.ROOK));
            theBoard.addPiece(new ChessPosition(move.getStartPosition().getRow(),8),null);
        }else if((preGame.getBoard().getPiece(move.getStartPosition())!=null)&&(preGame.getBoard().getPiece(move.getStartPosition()).getPieceType()== ChessPiece.PieceType.KING)&&((move.getEndPosition().getColumn()-move.getStartPosition().getColumn()==-2))){
            theBoard.addPiece(new ChessPosition(move.getStartPosition().getRow(),move.getStartPosition().getColumn()-1),new ChessPiece(turn, ChessPiece.PieceType.ROOK));
            theBoard.addPiece(new ChessPosition(move.getStartPosition().getRow(),1),null);
        }
    }
    private void checkCastling(ChessMove move){
        switch (turn) {
            case WHITE -> {
                if(((preGame.getBoard().getPiece(move.getStartPosition())!=null)&&(preGame.getBoard().getPiece(move.getStartPosition()).getPieceType()==ChessPiece.PieceType.KING))||((preGame.getBoard().getPiece(move.getStartPosition())!=null)&&(preGame.getBoard().getPiece(move.getStartPosition()).getPieceType()==ChessPiece.PieceType.ROOK)&&(Objects.equals(move.getStartPosition(), new ChessPosition(1, 8))))){
                    theBoard.setCastleKingSideW(false);
                }
                if(((preGame.getBoard().getPiece(move.getStartPosition())!=null)&&(preGame.getBoard().getPiece(move.getStartPosition()).getPieceType()==ChessPiece.PieceType.KING))||((preGame.getBoard().getPiece(move.getStartPosition())!=null)&&(preGame.getBoard().getPiece(move.getStartPosition()).getPieceType()==ChessPiece.PieceType.ROOK)&&(Objects.equals(move.getStartPosition(), new ChessPosition(1, 1))))){
                    theBoard.setCastleQueenSideW(false);
                }
            }
            case BLACK -> {
                if(((preGame.getBoard().getPiece(move.getStartPosition())!=null)&&(preGame.getBoard().getPiece(move.getStartPosition()).getPieceType()==ChessPiece.PieceType.KING))||((preGame.getBoard().getPiece(move.getStartPosition())!=null)&&(preGame.getBoard().getPiece(move.getStartPosition()).getPieceType()==ChessPiece.PieceType.ROOK)&&(Objects.equals(move.getStartPosition(), new ChessPosition(8, 8))))){
                    theBoard.setCastleKingSideB(false);
                }
                if(((preGame.getBoard().getPiece(move.getStartPosition())!=null)&&(preGame.getBoard().getPiece(move.getStartPosition()).getPieceType()==ChessPiece.PieceType.KING))||((preGame.getBoard().getPiece(move.getStartPosition())!=null)&&(preGame.getBoard().getPiece(move.getStartPosition()).getPieceType()==ChessPiece.PieceType.ROOK)&&(Objects.equals(move.getStartPosition(), new ChessPosition(8, 1))))){
                    theBoard.setCastleQueenSideB(false);
                }
            }
        }
    }
    private void checkEnPassant(ChessMove move){
        switch (turn){
            case WHITE -> {
                if(enPassantL&&(move.getStartPosition().getColumn()-1==move.getEndPosition().getColumn())){
                    theBoard.addPiece(new ChessPosition(move.getEndPosition().getRow()-1,move.getEndPosition().getColumn()), null);
                }else if(enPassantR&&(move.getStartPosition().getColumn()+1==move.getEndPosition().getColumn())){
                    theBoard.addPiece(new ChessPosition(move.getEndPosition().getRow()-1,move.getEndPosition().getColumn()), null);
                }
            }
            case BLACK -> {
                if(enPassantL&&(move.getStartPosition().getColumn()-1==move.getEndPosition().getColumn())){
                    theBoard.addPiece(new ChessPosition(move.getEndPosition().getRow()+1,move.getEndPosition().getColumn()), null);
                }else if(enPassantR&&(move.getStartPosition().getColumn()+1==move.getEndPosition().getColumn())){
                    theBoard.addPiece(new ChessPosition(move.getEndPosition().getRow()+1,move.getEndPosition().getColumn()), null);
                }
            }
        }
        enPassantL = false;
        enPassantR = false;
    }
    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingPos = null;
        ChessPosition curPos;
        ArrayList<ChessMove> pieceMoves = new ArrayList<>();
        for(int i = 1; i <= 8; i++){         //Iterating through the rows
            for(int j = 1; j <= 8; j++){     //Iterating through the columns
                curPos = new ChessPosition(i,j);
                if((theBoard.getPiece(curPos)!=null)&&(theBoard.getPiece(curPos).getPieceType()==ChessPiece.PieceType.KING)&&(theBoard.getPiece(curPos).getTeamColor())==teamColor){
                    kingPos = curPos;
                }
                if((theBoard.getPiece(curPos)!=null)&&theBoard.getPiece(curPos).getTeamColor()!=teamColor){
                    pieceMoves.addAll(theBoard.getPiece(curPos).pieceMoves(theBoard, curPos));
                }
            }
        }
        for(ChessMove pieceMove : pieceMoves){
            if(pieceMove.getEndPosition().equals(kingPos)){
                return true;
            }
        }
        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        if(isInCheck(teamColor)) {
            ArrayList<ChessMove> pieceMoves = new ArrayList<>();
            iteratePieceMoves(pieceMoves,teamColor);
            for (ChessMove pieceMove : pieceMoves) {
                if (isValidMove(pieceMove.getStartPosition(),pieceMove)) {
                    return false;
                }
            }
            return true;
        }else{
            return false;
        }
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {

        ArrayList<ChessMove> pieceMoves = new ArrayList<>();
        iteratePieceMoves(pieceMoves,teamColor);
        boolean stalemate = !isInCheckmate(teamColor);
        for(ChessMove pieceMove : pieceMoves){
            if(isValidMove(pieceMove.getStartPosition(),pieceMove)){
                stalemate = false;
            }
        }
        return stalemate;
    }

    private void iteratePieceMoves(ArrayList<ChessMove> pieceMoves, TeamColor teamColor){
        ChessPosition curPos;
        for(int i = 1; i <= 8; i++){         //Iterating through the rows
            for(int j = 1; j <= 8; j++){     //Iterating through the columns
                curPos = new ChessPosition(i,j);
                if((theBoard.getPiece(curPos)!=null)&&theBoard.getPiece(curPos).getTeamColor()==teamColor){
                    pieceMoves.addAll(theBoard.getPiece(curPos).pieceMoves(theBoard, curPos));
                }
            }
        }
    }
    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        theBoard = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return theBoard;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChessGame chessGame)) return false;
        return enPassantL == chessGame.enPassantL && enPassantR == chessGame.enPassantR && turn == chessGame.turn && Objects.equals(theBoard, chessGame.theBoard) && Objects.equals(preGame, chessGame.preGame);
    }

    @Override
    public int hashCode() {
        return Objects.hash(turn, theBoard, preGame, enPassantL, enPassantR);
    }
}
