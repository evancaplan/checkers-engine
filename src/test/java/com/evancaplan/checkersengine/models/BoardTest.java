package com.evancaplan.checkersengine.models;

import com.evancaplan.checkersengine.model.Board;
import com.evancaplan.checkersengine.model.Move;
import com.evancaplan.checkersengine.model.Piece;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static com.evancaplan.checkersengine.model.Piece.PieceColor.*;

public class BoardTest {

    private Board board;

    @BeforeEach
    void setUp() {
        board = Board.builder().build();
    }

    @Test
    void createStandardBoard() {
        Board board = Board.createStandardBoard(true);

        // Verify board is initialized with correct number of pieces
        assertEquals(12, board.getBlackPieces().size());
        assertEquals(12, board.getRedPieces().size());

        // Verify initial turn is BLACK
        assertEquals(BLACK, board.getCurrentTurn());

        // Verify pieces are in correct positions
        // Check a few black pieces at the top
        assertNotNull(board.getPieceAt(0, 1));
        assertEquals(BLACK, board.getPieceAt(0, 1).getColor());
        assertNotNull(board.getPieceAt(2, 1));
        assertEquals(BLACK, board.getPieceAt(2, 1).getColor());

        // Check a few red pieces at the bottom
        assertNotNull(board.getPieceAt(5, 0));
        assertEquals(RED, board.getPieceAt(5, 0).getColor());
        assertNotNull(board.getPieceAt(7, 0));
        assertEquals(RED, board.getPieceAt(7, 0).getColor());

        // Check middle is empty
        assertNull(board.getPieceAt(3, 0));
        assertNull(board.getPieceAt(4, 1));
    }

    @Test
    void getCaptureMovesForPiece_noCapturesMoves() {
        Piece redPiece = new Piece(RED, 3, 3);
        Piece blackPiece = new Piece(BLACK, 5, 5);

        board.setPieceAt(redPiece.getRow(), redPiece.getCol(), redPiece);
        board.setPieceAt(blackPiece.getRow(), blackPiece.getCol(), blackPiece);

        board.getRedPieces().add(redPiece);
        board.getBlackPieces().add(blackPiece);

        List<Move> redCaptureMoves = board.getCaptureMovesForPiece(redPiece);
        List<Move> blackCaptureMoves = board.getCaptureMovesForPiece(blackPiece);

        assertTrue(redCaptureMoves.isEmpty());
        assertTrue(blackCaptureMoves.isEmpty());
    }

    @Test
    void getCaptureMovesForPiece_withCaptureMoves() {
        Piece redPiece = new Piece(RED, 3, 3);
        Piece blackPiece = new Piece(BLACK, 2, 2);

        board.setPieceAt(redPiece.getRow(), redPiece.getCol(), redPiece);
        board.setPieceAt(blackPiece.getRow(), blackPiece.getCol(), blackPiece);

        board.getRedPieces().add(redPiece);
        board.getBlackPieces().add(blackPiece);

        List<Move> redCaptureMoves = board.getCaptureMovesForPiece(redPiece);

        assertEquals(1, redCaptureMoves.size());
        Move captureMove = redCaptureMoves.get(0);
        assertEquals(3, captureMove.getFromRow());
        assertEquals(3, captureMove.getFromCol());
        assertEquals(1, captureMove.getToRow());
        assertEquals(1, captureMove.getToCol());
        assertEquals(1, captureMove.getCapturedPieces().size());
        assertEquals(blackPiece, captureMove.getCapturedPieces().getFirst());
    }

    @Test
    void movePiece_simpleMove() {
        // Set up a board with a simple move
        Piece redPiece = new Piece(RED, 3, 3);

        board.setPieceAt(redPiece.getRow(), redPiece.getCol(), redPiece);
        board.getRedPieces().add(redPiece);
        board.setCurrentTurn(RED);

        // Create a move
        Move move = Move.builder()
                .fromRow(3)
                .fromCol(3)
                .toRow(2)
                .toCol(2)
                .piece(redPiece)
                .build();

        // Execute the move
        board.movePiece(move);

        // Verify the piece moved
        assertNull(board.getPieceAt(3, 3));
        assertNotNull(board.getPieceAt(2, 2));
        assertEquals(RED, board.getPieceAt(2, 2).getColor());

        // Verify turn toggled (since there are no captures)
        assertEquals(BLACK, board.getCurrentTurn());
    }

    @Test
    void movePiece_captureMove() {
        // Set up a board with a capture move
        Piece redPiece = new Piece(RED, 3, 3);
        Piece blackPiece = new Piece(BLACK, 2, 2);

        board.setPieceAt(redPiece.getRow(), redPiece.getCol(), redPiece);
        board.setPieceAt(blackPiece.getRow(), blackPiece.getCol(), blackPiece);

        board.getRedPieces().add(redPiece);
        board.getBlackPieces().add(blackPiece);
        board.setCurrentTurn(RED);

        // Create a capture move
        Move move = Move.builder()
                .fromRow(3)
                .fromCol(3)
                .toRow(1)
                .toCol(1)
                .piece(redPiece)
                .build();

        move.getCapturedPieces().add(blackPiece);

        // Execute the move
        board.movePiece(move);

        // Verify the piece moved
        assertNull(board.getPieceAt(3, 3));
        assertNotNull(board.getPieceAt(1, 1));
        assertEquals(RED, board.getPieceAt(1, 1).getColor());

        // Verify the captured piece was removed
        assertNull(board.getPieceAt(2, 2));
        assertEquals(0, board.getBlackPieces().size());

        // Verify turn toggled (since there are no more captures)
        assertEquals(BLACK, board.getCurrentTurn());
    }
}
