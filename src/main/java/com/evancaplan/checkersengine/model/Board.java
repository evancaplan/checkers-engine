package com.evancaplan.checkersengine.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static com.evancaplan.checkersengine.model.Piece.PieceColor.BLACK;
import static com.evancaplan.checkersengine.model.Piece.PieceColor.RED;

@Data
@AllArgsConstructor
@Builder
public class Board {
    public static final int BOARD_SIZE = 8;
    public static final int NUM_PIECES = 12;

    @Builder.Default
    private Piece[][] squares = new Piece[BOARD_SIZE][BOARD_SIZE];
    @Builder.Default
    private List<Piece> redPieces = new ArrayList<>();
    @Builder.Default
    private List<Piece> blackPieces = new ArrayList<>();

    // default to black first
    @Builder.Default
    private Piece.PieceColor currentTurn = BLACK;

    private boolean isSinglePlayer;

    public static Board createStandardBoard(boolean isSinglePlayer) {
        Board board = Board.builder().isSinglePlayer(isSinglePlayer).build();
        board.initializeBoard();
        return board;
    }

    private void initializeBoard() {
        // initialize black pieces
        for (int i = 0; i < NUM_PIECES; i++) {
            int row = i / (BOARD_SIZE / 2);
            int col = (2 * (i % (BOARD_SIZE / 2))) + ((row % 2 == 0) ? 1 : 0);
            Piece piece = new Piece(BLACK, row, col);
            squares[row][col] = piece;
            blackPieces.add(piece);
        }

        // initialize red pieces
        for (int i = 0; i < NUM_PIECES; i++) {
            int row = BOARD_SIZE - 1 - (i / (BOARD_SIZE / 2));
            int col = (2 * (i % (BOARD_SIZE / 2))) + ((row % 2 == 0) ? 1 : 0);
            Piece piece = new Piece(RED, row, col);
            squares[row][col] = piece;
            redPieces.add(piece);
        }

        // initialize remaining squares to null
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                if (squares[row][col] == null) {
                    squares[row][col] = null;
                }
            }
        }
    }

    public Piece getPieceAt(int row, int col) {
        if (isOnBoard(row, col)) {
            return squares[row][col];
        }
        return null;
    }

    public void setPieceAt(int row, int col, Piece piece) {
        if (isOnBoard(row, col)) {
            squares[row][col] = piece;
            if (piece != null) {
                piece.setRow(row);
                piece.setCol(col);
            }
        }
    }

    public void movePiece(Move move) {
        Piece piece = getPieceAt(move.getFromRow(), move.getFromCol());
        if (piece != null) {
            // check if this is a capture move (diagonal move of distance 2)
            int changeInRow = move.getToRow() - move.getFromRow();
            int changeInColumn = move.getToCol() - move.getFromCol();
            
            if (Math.abs(changeInRow) == 2 && Math.abs(changeInColumn) == 2) {
                // calculate the position of the jumped piece
                int jumpedRow = move.getFromRow() + changeInRow / 2;
                int jumpedCol = move.getFromCol() + changeInColumn / 2;
                Piece jumped = getPieceAt(jumpedRow, jumpedCol);
                
                // if there's a piece of the opposite color, add it to the captured pieces
                if (jumped != null && jumped.getColor() != piece.getColor()) {
                    move.getCapturedPieces().add(jumped);
                }
            }
            
            // remove piece from original position
            setPieceAt(move.getFromRow(), move.getFromCol(), null);

            // place piece at new position
            setPieceAt(move.getToRow(), move.getToCol(), piece);

            // check if piece should be kinged
            piece.checkForPromotion();

            // remove any captured pieces
            if (move.isCapture()) {
                for (Piece capturedPiece : move.getCapturedPieces()) {
                    removePiece(capturedPiece);
                }
            }

            // switch turns if no more captures available
            if (!hasAnyCapture(currentTurn)) {
                toggleTurn();
            }
        }
    }

    public void removePiece(Piece piece) {
        if (piece != null) {
            setPieceAt(piece.getRow(), piece.getCol(), null);
            if (piece.getColor() == RED) {
                redPieces.remove(piece);
            } else {
                blackPieces.remove(piece);
            }
        }
    }

    public boolean isOnBoard(int row, int col) {
        return row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE;
    }

    public boolean isGameOver() {
        return redPieces.isEmpty() || blackPieces.isEmpty();
    }

    public Piece.PieceColor getWinner() {
        if (blackPieces.isEmpty()) {
            return RED;
        } else if (redPieces.isEmpty()) {
            return BLACK;
        }
        // No winner yet
        return null;
    }

    public void toggleTurn() {
        currentTurn = (currentTurn == BLACK) ? RED : BLACK;
    }

    public List<Piece> getPiecesForColor(Piece.PieceColor color) {
        if (color == RED) {
            return redPieces;
        } else {
            return blackPieces;
        }
    }
    
    private static final int[][] BASIC_MOVE_DIRECTIONS = {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
    
    public boolean hasAnyCapture(Piece.PieceColor color) {
        List<Piece> pieces = getPiecesForColor(color);
        for (Piece piece : pieces) {
            if (canCapture(piece)) {
                return true;
            }
        }
        return false;
    }
    
    public boolean canCapture(Piece piece) {
        return !getCaptureMovesForPiece(piece).isEmpty();
    }
    
    public List<Move> getCaptureMovesForPiece(Piece piece) {
        return getDiagonalDestinationsForPiece(piece, 2)
                .stream()
                // filter out diagonals with no piece present
                .filter(dest -> getPieceAt(dest.row, dest.col) == null)
                // check if any pieces can be jumped
                .map(dest -> {
                    int midRow = piece.getRow() + dest.changInRow / 2;
                    int midCol = piece.getCol() + dest.changeInColumn / 2;
                    Piece jumped = getPieceAt(midRow, midCol);
                    if (jumped != null && jumped.getColor() != piece.getColor() && piece.isValidDirectionalMove(dest.changInRow)) {
                        Move move = Move.builder()
                                .fromRow(piece.getRow())
                                .fromCol(piece.getCol())
                                .toRow(dest.row)
                                .toCol(dest.col)
                                .piece(piece)
                                .build();
                        move.getCapturedPieces().add(jumped);
                        return move;
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .toList();
    }
    
    public List<Move> getSimpleMovesForPiece(Piece piece) {
        return getDiagonalDestinationsForPiece(piece, 1)
                .stream()
                .filter(dest -> getPieceAt(dest.row, dest.col) == null)
                .filter(dest -> piece.isValidDirectionalMove(dest.changInRow))
                .map(dest -> Move.builder()
                        .fromRow(piece.getRow())
                        .fromCol(piece.getCol())
                        .toRow(dest.row)
                        .toCol(dest.col)
                        .piece(piece)
                        .build())
                .toList();
    }
    
    public List<Delta> getDiagonalDestinationsForPiece(Piece piece, int distance) {
        return Arrays.stream(BASIC_MOVE_DIRECTIONS)
                .map(direction -> new Delta(
                        piece.getRow() + direction[0] * distance,
                        piece.getCol() + direction[1] * distance,
                        direction[0] * distance,
                        direction[1] * distance))
                .filter(d -> isOnBoard(d.row, d.col))
                .toList();
    }
    
    public record Delta(int row, int col, int changInRow, int changeInColumn) {
    }
    
    public boolean isValidMove(Move move) {
        // standard checks
        Piece piece = move.getPiece();
        if (piece == null) {
            return false;
        }
        if (piece.getColor() != currentTurn){
            return false;
        }
        if (!isOnBoard(move.getToRow(), move.getToCol())) {
            return false;
        }
        if (getPieceAt(move.getToRow(), move.getToCol()) != null) {
            return false;
        }

        // check it is diagonal
        int changeInRow = move.getToRow() - move.getFromRow();
        int changeInColumn = move.getToCol() - move.getFromCol();
        if (Math.abs(changeInColumn) != Math.abs(changeInRow)) {
            return false;
        }

        if (Math.abs(changeInRow) == 1) {
            // cannot make a simple move when any capture is available
            if (hasAnyCapture(currentTurn)) {
                return false;
            }

            return piece.isValidDirectionalMove(changeInRow);
        }

        if (Math.abs(changeInRow) == 2) {
            int jumpedRow = move.getFromRow() + changeInRow / 2;
            int jumpedCol = move.getFromCol() + changeInColumn / 2;
            Piece jumped = getPieceAt(jumpedRow, jumpedCol);

            return jumped != null && 
                   jumped.getColor() != piece.getColor() && 
                   piece.isValidDirectionalMove(changeInRow);
        }

        return false;
    }
}