package com.evancaplan.checkersengine.model;

import lombok.*;

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
    // valid diagonal movements
    private static final int[][] BASIC_MOVE_DIRECTIONS = {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
    public static final int BOARD_SIZE = 8;
    public static final int NUM_PIECES = 12;

    // lockdown getter in case someone wants to manipulate the board directly
    @Builder.Default
    @Getter(AccessLevel.NONE)
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
        board.initializePieces();
        return board;
    }

    private void initializePieces() {
        // set black on rows 0-2
        initializePieces(BLACK, 0, 2);
        // set red on rows 5-7
        initializePieces(RED, BOARD_SIZE - 3, BOARD_SIZE - 1);
    }

    private void initializePieces(Piece.PieceColor color, int fromRow, int toRow) {
        for (int row = fromRow; row <= toRow; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                // only place pieces on dark squares (row+column is odd)
                if ((row + col) % 2 == 1) {
                    Piece piece = new Piece(color, row, col);
                    squares[row][col] = piece;
                    (color == RED ? redPieces : blackPieces).add(piece);
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

    // public only for test set up purposes
    public void setPieceAt(int row, int col, Piece piece) {
        if (isOnBoard(row, col)) {
            squares[row][col] = piece;
            if (piece != null) {
                piece.setRow(row);
                piece.setColumn(col);
            }
        }
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

    // check for possible captures for a given piece
    public List<Move> getCaptureMovesForPiece(Piece piece) {
        return getDiagonalDestinationsForPiece(piece, 2).stream()
                .filter(dest -> getPieceAt(dest.row(), dest.column()) == null)
                .map(dest -> buildCaptureMove(piece, dest))
                .filter(Objects::nonNull)
                .toList();
    }

    // check for simple diagonal moves for a given piece
    public List<Move> getSimpleMovesForPiece(Piece piece) {
        return getDiagonalDestinationsForPiece(piece, 1).stream()
                .filter(destination -> getPieceAt(destination.row(), destination.column()) == null)
                .filter(destination -> piece.isValidDirectionalMove(destination.deltaRow()))
                .map(destination -> Move.from(piece, destination))
                .toList();
    }

    public void movePiece(Move move) {
        Piece piece = getPieceAt(move.getFromRow(), move.getFromCol());
        if (piece == null) {
            return;
        }

        int changeInRow = move.getToRow() - move.getFromRow();
        int changeInColumn = move.getToCol() - move.getFromCol();

        // check if it is a capture (row increases by 2)
        if (Math.abs(changeInRow) == 2) {
            Piece jumped = getPieceAt(move.getFromRow() + changeInRow / 2, move.getFromCol() + changeInColumn / 2);
            if (jumped != null && jumped.getColor() != piece.getColor()) {
                move.getCapturedPieces().add(jumped);
            }
        }

        // move the piece
        setPieceAt(move.getFromRow(), move.getFromCol(), null);
        setPieceAt(move.getToRow(), move.getToCol(), piece);

        piece.checkForPromotion();

        // reset moved capture pieces
        move.getCapturedPieces().forEach(this::removePiece);

        // change turn only if no further capture is mandatory
        if (!hasAnyCapturePossibilities(currentTurn)) {
            toggleTurn();
        }
    }

    public boolean isValidMove(Move move) {
        // standard checks
        Piece piece = move.getPiece();
        if (piece == null) {
            return false;
        }
        // it is not this colors turn
        if (piece.getColor() != currentTurn) {
            return false;
        }
        // the proposed move is out of bounds
        if (!isOnBoard(move.getToRow(), move.getToCol())) {
            return false;
        }
        // there is a piece of the same color already in that spot
        if (getPieceAt(move.getToRow(), move.getToCol()) != null) {
            return false;
        }

        // check it is diagonal (change in row does not equal change in column)
        int changeInRow = move.getToRow() - move.getFromRow();
        int changeInColumn = move.getToCol() - move.getFromCol();
        if (Math.abs(changeInColumn) != Math.abs(changeInRow)) {
            return false;
        }

        // check that the piece is moving in the correct direction
        if (Math.abs(changeInRow) == 1) {
            // cannot make a simple move when any capture is available
            if (hasAnyCapturePossibilities(currentTurn)) {
                return false;
            }

            return piece.isValidDirectionalMove(changeInRow);
        }

        // check that if jumping a piece it is in a valid direction and the piece isn't the same color
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

    private void removePiece(Piece piece) {
        if (piece != null) {
            setPieceAt(piece.getRow(), piece.getColumn(), null);
            if (piece.getColor() == RED) {
                redPieces.remove(piece);
            } else {
                blackPieces.remove(piece);
            }
        }
    }

    private boolean isOnBoard(int row, int col) {
        return row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE;
    }

    private void toggleTurn() {
        currentTurn = (currentTurn == BLACK) ? RED : BLACK;
    }

    private List<Piece> getPiecesForColor(Piece.PieceColor color) {
        return color == RED ? redPieces : blackPieces;
    }

    private boolean canCapture(Piece piece) {
        return !getCaptureMovesForPiece(piece).isEmpty();
    }

    private boolean hasAnyCapturePossibilities(Piece.PieceColor color) {
        return getPiecesForColor(color).stream().anyMatch(this::canCapture);
    }

    private Move buildCaptureMove(Piece piece, Delta destination) {
       // calculate the square for the piece to be jumped
        int midRow = piece.getRow() + destination.deltaRow() / 2;
        int midCol = piece.getColumn() + destination.deltaCol() / 2;
        Piece jumped = getPieceAt(midRow, midCol);

        if (jumped == null || jumped.getColor() == piece.getColor()) {
            return null;
        }
        if (!piece.isValidDirectionalMove(destination.deltaRow())) {
            return null;
        }

        Move move = Move.from(piece, destination);

        move.getCapturedPieces().add(jumped);

        return move;
    }

    private List<Delta> getDiagonalDestinationsForPiece(Piece piece, int distance) {
        // iterate over basic directional moves and return a list of possible destinations on the board
        return Arrays.stream(BASIC_MOVE_DIRECTIONS)
                .map(direction -> new Delta(
                        piece.getRow() + direction[0] * distance,
                        piece.getColumn() + direction[1] * distance,
                        direction[0] * distance,
                        direction[1] * distance))
                .filter(delta -> isOnBoard(delta.row(), delta.column()))
                .toList();
    }

    // represents a diagonal landing square
    public record Delta(int row, int column, int deltaRow, int deltaCol) {
    }
}