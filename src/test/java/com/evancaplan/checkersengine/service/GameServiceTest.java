package com.evancaplan.checkersengine.service;

import com.evancaplan.checkersengine.dto.MoveRequest;
import com.evancaplan.checkersengine.dto.StartGameRequest;
import com.evancaplan.checkersengine.model.Board;
import com.evancaplan.checkersengine.model.Piece;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameServiceTest {
    @Mock
    private MoveService moveService;

    @InjectMocks
    private GameService gameService;

    private Board board;

    @BeforeEach
    void setUp() {
        board = Board.builder().build();
        board.setCurrentTurn(Piece.PieceColor.RED);
        board.setRedPieces(new ArrayList<>());
        board.setBlackPieces(new ArrayList<>());
    }

    @Test
    void startNewGame_shouldCreateNewGameAndReturnId() {
        String gameId = gameService.startNewGame(StartGameRequest.builder().build());
        
        assertNotNull(gameId);
        assertTrue(gameService.gameExists(gameId));
    }

    @Test
    void getBoardState_returnsBoardForExistingGame() {
        String gameId = gameService.startNewGame(StartGameRequest.builder().build());
        
        Board board = gameService.getBoardState(gameId);
        
        assertNotNull(board);
        // Don't compare with this.board as startNewGame() creates a standard board with pieces
        assertEquals(Piece.PieceColor.BLACK, board.getCurrentTurn());
        assertEquals(12, board.getBlackPieces().size());
        assertEquals(12, board.getRedPieces().size());
    }

    @Test
    void getBoardState_returnsNullForNonExistentGame() {
        Board board = gameService.getBoardState("non-existent-id");
        
        assertNull(board);
    }

    @Test
    void makeMove_returnsFalseForNonExistentGame() {
        boolean result = gameService.makeMove(MoveRequest.builder().gameId("non-existent-id").build());
        assertFalse(result);
        verify(moveService, never()).apply(any(), any());
    }

    @Test
    void makeMove_returnsFalseWhenNotPlayersTurn() {
        String gameId = gameService.startNewGame(StartGameRequest.builder().build());
        board.setCurrentTurn(Piece.PieceColor.BLACK);
        
        boolean result = gameService.makeMove(MoveRequest.builder().gameId(gameId).build());
        
        assertFalse(result);
        verify(moveService, never()).apply(any(), any());
    }

    @Test
    void makeMove_returnsFalseWhenMoveIsInvalid() {
        // Create a real board with a standard setup
        String gameId = gameService.startNewGame(StartGameRequest.builder().build());
        Board board = gameService.getBoardState(gameId);
        
        // Ensure the board is in the expected state
        assertEquals(Piece.PieceColor.BLACK, board.getCurrentTurn());
        
        // Force the turn to RED so our move is allowed
        board.setCurrentTurn(Piece.PieceColor.RED);
        
        // Setup the moveService to return false for any move
        when(moveService.apply(any(), any())).thenReturn(false);
        
        // Create a move request with coordinates that have a RED piece
        MoveRequest request = MoveRequest.builder()
                .gameId(gameId)
                .fromRow(5)  // Position with a RED piece in standard board
                .fromCol(0)
                .toRow(4)
                .toCol(1)
                .build();
        
        // Execute the method under test
        boolean result = gameService.makeMove(request);
        
        // Verify the result and interactions
        assertFalse(result);
        verify(moveService, times(1)).apply(any(), any());
        verify(moveService, never()).generateAIMove(any());
    }
    
    @Test
    void makeMove_returnsTrueWhenMoveIsValid() {
        // Create a real board with a standard setup
        String gameId = gameService.startNewGame(StartGameRequest.builder().build());
        Board board = gameService.getBoardState(gameId);
        
        // Force the turn to BLACK so our move is allowed
        board.setCurrentTurn(Piece.PieceColor.BLACK);
        
        // Setup the moveService to return true for any move
        when(moveService.apply(any(), any())).thenReturn(true);
        
        // Setup the moveService to return null for AI move to simplify the test
        when(moveService.generateAIMove(any())).thenReturn(null);
        
        // Create a move request with coordinates that have a BLACK piece
        MoveRequest request = MoveRequest.builder()
                .gameId(gameId)
                .fromRow(2)  // Position with a BLACK piece in standard board
                .fromCol(7)
                .toRow(3)
                .toCol(6)
                .build();
        
        // Execute the method under test
        boolean result = gameService.makeMove(request);
        
        // Verify the result and interactions
        assertTrue(result);
        verify(moveService, times(1)).apply(any(), any());
    }

    @Test
    void gameExists_returnsTrueForExistingGame() {
        String gameId = gameService.startNewGame(StartGameRequest.builder().build());
        assertTrue(gameService.gameExists(gameId));
    }

    @Test
    void gameExists_returnsFalseForNonExistentGame() {
        assertFalse(gameService.gameExists("non-existent-id"));
    }
}