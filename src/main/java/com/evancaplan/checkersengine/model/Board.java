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
    private static final int[][] BASIC_MOVE_DIRECTIONS = {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
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
        board.initializePieces();
        return board;
    }

    private void initializePieces() {
        // set black on rows 0-2
        initializePieces(BLACK, 0, 2);
        // set red on rows 5-7
        initializePieces(RED, BOARD_SIZE - 3, BOARD_SIZE - 1);
    }

    private void initializePieces(Piece.PieceColor color, int fromRow, int toRowInclusive) {
        for (int row = fromRow; row <= toRowInclusive; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                // only place pieces dark squares (row+col is odd) hold pieces at start
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
                piece.setCol(col);
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

    public List<Move> getCaptureMovesForPiece(Piece piece) {
        return getDiagonalDestinationsForPiece(piece, 2).stream()
                .filter(dest -> getPieceAt(dest.row(), dest.col()) == null)
                .map(dest -> buildCaptureMove(piece, dest))
                .filter(Objects::nonNull)
                .toList();
    }

    public List<Move> getSimpleMovesForPiece(Piece piece) {
        return getDiagonalDestinationsForPiece(piece, 1).stream()
                .filter(dest -> getPieceAt(dest.row(), dest.col()) == null)
                .filter(dest -> piece.isValidDirectionalMove(dest.deltaRow()))
                .map(dest -> Move.builder()
                        .fromRow(piece.getRow())
                        .fromCol(piece.getCol())
                        .toRow(dest.row())
                        .toCol(dest.col())
                        .piece(piece)
                        .build())
                .toList();
    }

    public void movePiece(Move move) {
        Piece piece = getPieceAt(move.getFromRow(), move.getFromCol());
        if (piece == null) {
            return;
        }

        int changeInRow = move.getToRow() - move.getFromRow();
        int changeInColumn = move.getToCol() - move.getFromCol();

        // check if it is a capture
        if (Math.abs(changeInRow) == 2) {
            Piece jumped = getPieceAt(move.getFromRow() + changeInRow / 2, move.getFromCol() + changeInColumn / 2);
            if (jumped != null && jumped.getColor() != piece.getColor()) {
                move.getCapturedPieces().add(jumped);
            }
        }

        // physically move the piece
        setPieceAt(move.getFromRow(), move.getFromCol(), null);
        setPieceAt(move.getToRow(), move.getToCol(), piece);

        piece.checkForPromotion();

        // reset moved capture pieces
        move.getCapturedPieces().forEach(this::removePiece);

        // change turn only if no further capture is mandatory
        if (!hasAnyCapture(currentTurn)) {
            toggleTurn();
        }
    }

    public boolean isValidMove(Move move) {
        // standard checks
        Piece piece = move.getPiece();
        if (piece == null) {
            return false;
        }
        if (piece.getColor() != currentTurn) {
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

    private void removePiece(Piece piece) {
        if (piece != null) {
            setPieceAt(piece.getRow(), piece.getCol(), null);
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

    private boolean hasAnyCapture(Piece.PieceColor color) {
        return getPiecesForColor(color).stream().anyMatch(this::canCapture);
    }

    private Move buildCaptureMove(Piece piece, Delta destination) {
        int midRow = piece.getRow() + destination.deltaRow() / 2;
        int midCol = piece.getCol() + destination.deltaCol() / 2;
        Piece jumped = getPieceAt(midRow, midCol);

        if (jumped == null || jumped.getColor() == piece.getColor()) {
            return null;
        }
        if (!piece.isValidDirectionalMove(destination.deltaRow())) {
            return null;
        }

        Move move = Move.builder()
                .fromRow(piece.getRow())
                .fromCol(piece.getCol())
                .toRow(destination.row())
                .toCol(destination.col())
                .piece(piece)
                .build();

        move.getCapturedPieces().add(jumped);

        return move;
    }

    private List<Delta> getDiagonalDestinationsForPiece(Piece piece, int distance) {
       // iterate over basic directional moves and return a list of possible destinations
        return Arrays.stream(BASIC_MOVE_DIRECTIONS)
                .map(dir -> new Delta(
                        piece.getRow() + dir[0] * distance,
                        piece.getCol() + dir[1] * distance,
                        dir[0] * distance,
                        dir[1] * distance))
                .filter(d -> isOnBoard(d.row(), d.col()))
                .toList();
    }

    private record Delta(int row, int col, int deltaRow, int deltaCol) { }
}