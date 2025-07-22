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
        // Create an empty board for each test
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
    void hasAnyCapture_noCaptures() {
        // Set up a board with no capture opportunities
        Piece redPiece = new Piece(RED, 3, 3);
        Piece blackPiece = new Piece(BLACK, 5, 5);
        
        board.setPieceAt(redPiece.getRow(), redPiece.getCol(), redPiece);
        board.setPieceAt(blackPiece.getRow(), blackPiece.getCol(), blackPiece);
        
        board.getRedPieces().add(redPiece);
        board.getBlackPieces().add(blackPiece);
        
        // Test
        assertFalse(board.hasAnyCapture(RED));
        assertFalse(board.hasAnyCapture(BLACK));
    }
    
    @Test
    void hasAnyCapture_withCaptures() {
        // Set up a board with a capture opportunity for red
        // Place a red piece that can capture a black piece
        // The black piece cannot capture because of directional constraints
        
        // Red piece at (3,3) can capture black piece at (2,2) by moving to (1,1)
        Piece redPiece = new Piece(RED, 3, 3);
        Piece blackPiece = new Piece(BLACK, 2, 2);
        
        // Block the position (4,4) to prevent black from capturing red
        Piece blockingPiece = new Piece(BLACK, 4, 4);
        
        board.setPieceAt(redPiece.getRow(), redPiece.getCol(), redPiece);
        board.setPieceAt(blackPiece.getRow(), blackPiece.getCol(), blackPiece);
        board.setPieceAt(blockingPiece.getRow(), blockingPiece.getCol(), blockingPiece);
        
        board.getRedPieces().add(redPiece);
        board.getBlackPieces().add(blackPiece);
        board.getBlackPieces().add(blockingPiece);
        
        // Red can capture, Black cannot
        assertTrue(board.hasAnyCapture(RED));
        assertFalse(board.hasAnyCapture(BLACK));
    }
    
    @Test
    void canCapture_pieceCanCapture() {
        // Set up a board with a capture opportunity for red
        // The black piece cannot capture because of directional constraints or blocking
        
        // Red piece at (3,3) can capture black piece at (2,2) by moving to (1,1)
        Piece redPiece = new Piece(RED, 3, 3);
        Piece blackPiece = new Piece(BLACK, 2, 2);
        
        // Block the position (4,4) to prevent black from capturing red
        Piece blockingPiece = new Piece(BLACK, 4, 4);
        
        board.setPieceAt(redPiece.getRow(), redPiece.getCol(), redPiece);
        board.setPieceAt(blackPiece.getRow(), blackPiece.getCol(), blackPiece);
        board.setPieceAt(blockingPiece.getRow(), blockingPiece.getCol(), blockingPiece);
        
        board.getRedPieces().add(redPiece);
        board.getBlackPieces().add(blackPiece);
        board.getBlackPieces().add(blockingPiece);
        
        // Red can capture, Black cannot
        assertTrue(board.canCapture(redPiece));
        assertFalse(board.canCapture(blackPiece));
    }
    
    @Test
    void canCapture_pieceCannotCapture() {
        Piece redPiece = new Piece(RED, 3, 3);
        Piece blackPiece = new Piece(BLACK, 5, 5);
        
        board.setPieceAt(redPiece.getRow(), redPiece.getCol(), redPiece);
        board.setPieceAt(blackPiece.getRow(), blackPiece.getCol(), blackPiece);
        
        board.getRedPieces().add(redPiece);
        board.getBlackPieces().add(blackPiece);
        
        assertFalse(board.canCapture(redPiece));
        assertFalse(board.canCapture(blackPiece));
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
    void getCaptureMovesForPiece_withKingPiece() {
        // Set up a board with a king piece that can capture in multiple directions
        Piece redKing = new Piece(RED, 3, 3);
        redKing.makeKing();
        
        Piece blackPiece1 = new Piece(BLACK, 2, 2);
        Piece blackPiece2 = new Piece(BLACK, 4, 2);
        
        board.setPieceAt(redKing.getRow(), redKing.getCol(), redKing);
        board.setPieceAt(blackPiece1.getRow(), blackPiece1.getCol(), blackPiece1);
        board.setPieceAt(blackPiece2.getRow(), blackPiece2.getCol(), blackPiece2);
        
        board.getRedPieces().add(redKing);
        board.getBlackPieces().add(blackPiece1);
        board.getBlackPieces().add(blackPiece2);
        
        List<Move> kingCaptureMoves = board.getCaptureMovesForPiece(redKing);
        
        assertEquals(2, kingCaptureMoves.size());
        
        // Verify captures in both directions
        boolean foundUpLeftCapture = false;
        boolean foundDownLeftCapture = false;
        
        for (Move move : kingCaptureMoves) {
            if (move.getToRow() == 1 && move.getToCol() == 1) {
                foundUpLeftCapture = true;
                assertEquals(blackPiece1, move.getCapturedPieces().get(0));
            }
            if (move.getToRow() == 5 && move.getToCol() == 1) {
                foundDownLeftCapture = true;
                assertEquals(blackPiece2, move.getCapturedPieces().get(0));
            }
        }
        
        assertTrue(foundUpLeftCapture);
        assertTrue(foundDownLeftCapture);
    }
    
    @Test
    void getSimpleMovesForPiece_noSimpleMoves() {
        Piece redPiece = new Piece(RED, 3, 3);
        Piece blackPiece1 = new Piece(BLACK, 2, 2);
        Piece blackPiece2 = new Piece(BLACK, 2, 4);
        
        board.setPieceAt(redPiece.getRow(), redPiece.getCol(), redPiece);
        board.setPieceAt(blackPiece1.getRow(), blackPiece1.getCol(), blackPiece1);
        board.setPieceAt(blackPiece2.getRow(), blackPiece2.getCol(), blackPiece2);
        
        board.getRedPieces().add(redPiece);
        board.getBlackPieces().add(blackPiece1);
        board.getBlackPieces().add(blackPiece2);
        
        List<Move> simpleMoves = board.getSimpleMovesForPiece(redPiece);
        
        assertTrue(simpleMoves.isEmpty());
    }
    
    @Test
    void getSimpleMovesForPiece_withSimpleMoves() {
        // Set up a board with simple moves available
        Piece redPiece = new Piece(RED, 3, 3);
        
        board.setPieceAt(redPiece.getRow(), redPiece.getCol(), redPiece);
        board.getRedPieces().add(redPiece);
        
        List<Move> simpleMoves = board.getSimpleMovesForPiece(redPiece);
        
        assertEquals(2, simpleMoves.size());
        
        // Verify moves in both valid directions for red (up-left and up-right)
        boolean foundUpLeftMove = false;
        boolean foundUpRightMove = false;
        
        for (Move move : simpleMoves) {
            if (move.getToRow() == 2 && move.getToCol() == 2) {
                foundUpLeftMove = true;
            }
            if (move.getToRow() == 2 && move.getToCol() == 4) {
                foundUpRightMove = true;
            }
        }
        
        assertTrue(foundUpLeftMove);
        assertTrue(foundUpRightMove);
    }
    
    @Test
    void getSimpleMovesForPiece_withKingPiece() {
        // Set up a board with a king piece that can move in all directions
        Piece redKing = new Piece(RED, 3, 3);
        redKing.makeKing();
        
        board.setPieceAt(redKing.getRow(), redKing.getCol(), redKing);
        board.getRedPieces().add(redKing);
        
        // Test
        List<Move> kingMoves = board.getSimpleMovesForPiece(redKing);
        
        assertEquals(4, kingMoves.size());
        
        // Verify moves in all four directions
        boolean foundUpLeftMove = false;
        boolean foundUpRightMove = false;
        boolean foundDownLeftMove = false;
        boolean foundDownRightMove = false;
        
        for (Move move : kingMoves) {
            if (move.getToRow() == 2 && move.getToCol() == 2) {
                foundUpLeftMove = true;
            }
            if (move.getToRow() == 2 && move.getToCol() == 4) {
                foundUpRightMove = true;
            }
            if (move.getToRow() == 4 && move.getToCol() == 2) {
                foundDownLeftMove = true;
            }
            if (move.getToRow() == 4 && move.getToCol() == 4) {
                foundDownRightMove = true;
            }
        }
        
        assertTrue(foundUpLeftMove);
        assertTrue(foundUpRightMove);
        assertTrue(foundDownLeftMove);
        assertTrue(foundDownRightMove);
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
    
    @Test
    void movePiece_captureWithMoreCaptures() {
        // Set up a board with multiple capture opportunities
        Piece redPiece = new Piece(RED, 5, 5);
        Piece blackPiece1 = new Piece(BLACK, 4, 4);
        Piece blackPiece2 = new Piece(BLACK, 2, 2);
        
        board.setPieceAt(redPiece.getRow(), redPiece.getCol(), redPiece);
        board.setPieceAt(blackPiece1.getRow(), blackPiece1.getCol(), blackPiece1);
        board.setPieceAt(blackPiece2.getRow(), blackPiece2.getCol(), blackPiece2);
        
        board.getRedPieces().add(redPiece);
        board.getBlackPieces().add(blackPiece1);
        board.getBlackPieces().add(blackPiece2);
        board.setCurrentTurn(RED);
        
        // Create a capture move for the first capture
        Move move = Move.builder()
                .fromRow(5)
                .fromCol(5)
                .toRow(3)
                .toCol(3)
                .piece(redPiece)
                .build();
        
        move.getCapturedPieces().add(blackPiece1);
        
        // Execute the move
        board.movePiece(move);
        
        // Verify the piece moved
        assertNull(board.getPieceAt(5, 5));
        assertNotNull(board.getPieceAt(3, 3));
        assertEquals(RED, board.getPieceAt(3, 3).getColor());
        
        // Verify the captured piece was removed
        assertNull(board.getPieceAt(4, 4));
        assertEquals(1, board.getBlackPieces().size());
        
        // Verify turn did NOT toggle (since there are more captures)
        assertEquals(RED, board.getCurrentTurn());
    }
    
    @Test
    void movePiece_promotion() {
        // Set up a board with a piece about to be promoted
        Piece redPiece = new Piece(RED, 1, 1);
        
        board.setPieceAt(redPiece.getRow(), redPiece.getCol(), redPiece);
        board.getRedPieces().add(redPiece);
        board.setCurrentTurn(RED);
        
        Move move = Move.builder()
                .fromRow(1)
                .fromCol(1)
                .toRow(0)
                .toCol(0)
                .piece(redPiece)
                .build();
        
        board.movePiece(move);
        
        // Verify the piece moved and was promoted
        assertNull(board.getPieceAt(1, 1));
        assertNotNull(board.getPieceAt(0, 0));
        assertEquals(RED, board.getPieceAt(0, 0).getColor());
        assertTrue(board.getPieceAt(0, 0).isKing());
        
        // Verify turn toggled
        assertEquals(BLACK, board.getCurrentTurn());
    }
}
