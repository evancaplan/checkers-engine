package com.evancaplan.checkersengine.models;

import com.evancaplan.checkersengine.model.Board;
import com.evancaplan.checkersengine.model.Piece;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static com.evancaplan.checkersengine.model.Piece.PieceColor.*;

public class PieceTest {
    @Test
    void makeKing() {
        Piece piece = new Piece(RED, 3, 4);
        assertFalse(piece.isKing());
        piece.makeKing();
        assertTrue(piece.isKing());
    }

    @Test
    void makeKing_alreadyKingStaysKing() {
        Piece piece = Piece.builder()
                .color(BLACK)
                .king(true)
                .row(5)
                .column(2)
                .build();
        assertTrue(piece.isKing());

        piece.makeKing();

        assertTrue(piece.isKing());
    }

    @Test
    void checkForPromotion_redPiece() {
        Piece piece = new Piece(RED, 0, 3);

        assertFalse(piece.isKing());

        piece.checkForPromotion();

        assertTrue(piece.isKing());
    }

    @Test
    void checkForPromotion_blackPiece() {
        Piece piece = new Piece(BLACK, Board.BOARD_SIZE - 1, 2);

        assertFalse(piece.isKing());

        piece.checkForPromotion();

        assertTrue(piece.isKing());
    }

    @Test
    void isValidDirectionalMove_redPieceIsValid() {
        Piece piece = new Piece(RED, 3, 4);

        assertTrue(piece.isValidDirectionalMove(-1));
    }

    @Test
    void isValidDirectionalMove_redPieceIsInvalid() {
        Piece piece = new Piece(RED, 3, 4);

        assertFalse(piece.isValidDirectionalMove(1));
    }

    @Test
    void isValidDirectionalMove_blackPieceIsValid() {
        Piece piece = new Piece(BLACK, 3, 4);

        assertTrue(piece.isValidDirectionalMove(1));
    }

    @Test
    void isValidDirectionalMove_blackPieceIsInvalid() {
        Piece piece = new Piece(BLACK, 3, 4);

        assertFalse(piece.isValidDirectionalMove(-1));
    }

    @Test
    void isValidDirectionalMove_kingPieceMovingAnyDirection() {
        Piece piece = Piece.builder()
                .color(RED)
                .king(true)
                .row(3)
                .column(4)
                .build();

        assertTrue(piece.isValidDirectionalMove(-1));
        assertTrue(piece.isValidDirectionalMove(1));
    }
}