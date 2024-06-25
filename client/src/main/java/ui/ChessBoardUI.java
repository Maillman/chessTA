package ui;

import chess.*;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Objects;

import static ui.EscapeSequences.*;

public class ChessBoardUI {
    private static final int BOARD_SIZE_IN_SQUARES = 8;
    private static final int LINE_WIDTH_IN_CHARS = 1;
    //private static final String EMPTY = "   ";
    private static final String[] LETTERS = new String[]{"a","b","c","d","e","f","g","h"};

    public enum Perspective {
        WHITE,
        BLACK,
        WATCH
    }

    public static void main(String[] args) {
        var out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        out.print(ERASE_SCREEN);
        ChessGame newGame = new ChessGame();
        ChessBoard initBoard = new ChessBoard();
        initBoard.resetBoard();
        newGame.setBoard(initBoard);
        out.println("White's Perspective:");
        drawChessBoard(out, newGame, Perspective.WHITE, null);
        out.println();
        out.println("Black's Perspective:");
        drawChessBoard(out, newGame, Perspective.BLACK, null);
        out.println();
        out.println("Watcher's Perspective:");
        drawChessBoard(out, newGame, Perspective.WATCH, null);
        out.print(SET_BG_COLOR_BLACK);
        out.print(SET_TEXT_COLOR_WHITE);
    }

    public static void drawChessBoard(PrintStream out, ChessGame theGame, Perspective perspective, ChessPosition chessPosition) {
        Collection<ChessMove> highlightMoves = null;
        if(chessPosition!=null){
            highlightMoves = theGame.validMoves(chessPosition);
        }
        System.out.println("The Board:");
        int boardRow, boardColumn;
        if (perspective== Perspective.BLACK) {
            boardRow = 0;
            boardColumn = 9;
        }else{
            boardRow = 9;
            boardColumn = 0;
        }
        while(boardRow>=0&&boardRow<BOARD_SIZE_IN_SQUARES+2){
            while(boardColumn>=0&&boardColumn<BOARD_SIZE_IN_SQUARES+2){
                if(boardRow==0||boardRow==9){
                    border(out, boardColumn);
                }else{
                    mainBoard(out, theGame, chessPosition, boardColumn, boardRow, highlightMoves);
                }
                //Ending statement
                if(perspective==Perspective.BLACK){
                    boardColumn--;
                }else{
                    boardColumn++;
                }
            }
            out.print(SET_BG_COLOR_DARK_GREY);
            out.println();
            //Ending statement
            if(perspective==Perspective.BLACK){
                boardRow++;
                boardColumn = 9;
            }else{
                boardRow--;
                boardColumn = 0;
            }
        }
    }

    private static void border(PrintStream out, int boardColumn) {
        //Letters
        setOuter(out);
        if(boardColumn ==0|| boardColumn ==9){
            //The corners
            out.print(EMPTY.repeat(LINE_WIDTH_IN_CHARS));
        }else{
            //The letters
            out.print("\u2009\u2002" + LETTERS[boardColumn -1] + "\u2002\u202F\u200A");
        }
    }

    private static void mainBoard(PrintStream out, ChessGame theGame, ChessPosition chessPosition, int boardColumn, int boardRow, Collection<ChessMove> highlightMoves) {
        //Main stuff
        if(boardColumn ==0|| boardColumn ==9){
            //The numbers
            setOuter(out);
            out.print("\u2009\u2002" + boardRow + "\u2002\u202F\u200A");
        }else{
            //Main board!
            ChessPiece piece = theGame.getBoard().getPiece(new ChessPosition(boardRow, boardColumn));
            if(boardRow %2==0 ^ boardColumn %2==0){
                setWhiteSquare(out);
                if((highlightMoves !=null)&& highlightMoves.contains(new ChessMove(chessPosition,new ChessPosition(boardRow, boardColumn)))){
                    setWhiteHighlight(out);
                }
            }else{
                setBlackSquare(out);
                if((highlightMoves !=null)&& highlightMoves.contains(new ChessMove(chessPosition,new ChessPosition(boardRow, boardColumn)))){
                    setBlackHighlight(out);
                }
            }
            if(Objects.equals(chessPosition, new ChessPosition(boardRow, boardColumn))){
                setHighlight(out);
            }
            if(piece!=null) {
                switch (piece.getTeamColor()){
                    case WHITE -> setWhitePiece(out);
                    case BLACK -> setBlackPiece(out);
                }
                switch (piece.getPieceType()) {
                    case BISHOP -> out.print(BLACK_BISHOP);
                    case ROOK -> out.print(BLACK_ROOK);
                    case QUEEN -> out.print(BLACK_QUEEN);
                    case KNIGHT -> out.print(BLACK_KNIGHT);
                    case KING -> out.print(BLACK_KING);
                    case PAWN -> out.print(BLACK_PAWN);
                }
            }else{
                out.print(EMPTY);
            }
        }
    }

    private static void setOuter(PrintStream out) {
        out.print(SET_BG_COLOR_DARK_GREEN);
        out.print(SET_TEXT_COLOR_WHITE);
    }

    private static void setBlackSquare(PrintStream out) {
        out.print(SET_BG_COLOR_DARK_GREY);
    }

    private static void setWhiteSquare(PrintStream out) {
        out.print(SET_BG_COLOR_LIGHT_GREY);
    }
    private static void setBlackHighlight(PrintStream out) {
        out.print(SET_BG_COLOR_OFF_GREEN);
    }
    private static void setWhiteHighlight(PrintStream out) {
        out.print(SET_BG_COLOR_OFF_LIGHT_GREEN);
    }
    private static void setHighlight(PrintStream out) {
        out.print(SET_BG_COLOR_DARK_YELLOW);
    }

    private static void setBlackPiece(PrintStream out) {
        out.print(SET_TEXT_COLOR_BLACK);
    }

    private static void setWhitePiece(PrintStream out) {
        out.print(SET_TEXT_COLOR_WHITE);
    }
}
