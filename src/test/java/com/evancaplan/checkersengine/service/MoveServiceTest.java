package com.evancaplan.checkersengine.service;

import com.evancaplan.checkersengine.model.Board;
import com.evancaplan.checkersengine.model.Move;
import com.evancaplan.checkersengine.model.Piece;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MoveServiceTest {

    @InjectMocks
    private MoveService moveService;

    @Mock
    private Board board;

    private Move move;

    @BeforeEach
    void setUp() {
        Piece piece = new Piece(Piece.PieceColor.RED, 5, 2);
        move = Move.builder()
                .fromRow(5)
                .fromCol(2)
                .toRow(4)
                .toCol(3)
                .piece(piece)
                .build();
    }

    @Test
    void apply_validMoveReturnsTrue() {
        when(board.isValidMove(move)).thenReturn(true);
        
        boolean result = moveService.apply(board, move);
        
        assertTrue(result);
        verify(board).movePiece(move);
    }

    @Test
    void apply_invalidMoveReturnsFalse() {
        when(board.isValidMove(move)).thenReturn(false);
        
        boolean result = moveService.apply(board, move);
        
        assertFalse(result);
        verify(board, never()).movePiece(any());
    }

    @Test
    void generateAIMove_captureMovesPresentReturnsFirstCaptureMove() {
        List<Piece> blackPieces = new ArrayList<>();
        Piece blackPiece1 = new Piece(Piece.PieceColor.BLACK, 2, 3);
        Piece blackPiece2 = new Piece(Piece.PieceColor.BLACK, 4, 5);
        blackPieces.add(blackPiece1);
        blackPieces.add(blackPiece2);
        
        Move captureMove = Move.builder()
                .fromRow(2)
                .fromCol(3)
                .toRow(4)
                .toCol(5)
                .piece(blackPiece1)
                .build();
        
        List<Move> captureMoves = new ArrayList<>();
        captureMoves.add(captureMove);
        when(board.getBlackPieces()).thenReturn(blackPieces);
        when(board.getCaptureMovesForPiece(blackPiece1)).thenReturn(captureMoves);

        Move result = moveService.generateAIMove(board);
        
        assertEquals(captureMove, result);
        verify(board, never()).getSimpleMovesForPiece(any());
    }

    @Test
    void generateAIMove_noCaptureMovesReturnsFirstSimpleMove() {
        List<Piece> blackPieces = new ArrayList<>();
        Piece blackPiece = new Piece(Piece.PieceColor.BLACK, 2, 3);
        blackPieces.add(blackPiece);
        
        Move simpleMove = Move.builder()
                .fromRow(2)
                .fromCol(3)
                .toRow(3)
                .toCol(4)
                .piece(blackPiece)
                .build();
        
        List<Move> simpleMoves = new ArrayList<>();
        simpleMoves.add(simpleMove);
        
        when(board.getBlackPieces()).thenReturn(blackPieces);
        when(board.getCaptureMovesForPiece(blackPiece)).thenReturn(new ArrayList<>());
        when(board.getSimpleMovesForPiece(blackPiece)).thenReturn(simpleMoves);
        
        Move result = moveService.generateAIMove(board);
        
        assertEquals(simpleMove, result);
    }

    @Test
    void generateAIMove_noValidMovesReturnsNull() {
        List<Piece> blackPieces = new ArrayList<>();
        Piece blackPiece = new Piece(Piece.PieceColor.BLACK, 2, 3);
        blackPieces.add(blackPiece);
        
        when(board.getBlackPieces()).thenReturn(blackPieces);
        when(board.getCaptureMovesForPiece(blackPiece)).thenReturn(new ArrayList<>());
        when(board.getSimpleMovesForPiece(blackPiece)).thenReturn(new ArrayList<>());
        
        Move result = moveService.generateAIMove(board);
        
        assertNull(result);
    }
}
