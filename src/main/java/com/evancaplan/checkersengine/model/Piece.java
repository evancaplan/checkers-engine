package com.evancaplan.checkersengine.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.evancaplan.checkersengine.model.Piece.PieceColor.BLACK;
import static com.evancaplan.checkersengine.model.Piece.PieceColor.RED;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Piece {
    private PieceColor color;
    private boolean king;
    private int row;
    private int col;

    public enum PieceColor {
        BLACK, RED
    }

    public Piece(PieceColor color, int row, int col) {
        this.color = color;
        this.king = false;
        this.row = row;
        this.col = col;
    }

    public void makeKing() {
        this.king = true;
    }

    public void checkForPromotion() {
        if (!king) {
            if (color == RED && row == 0 || color == BLACK && getRow() == Board.BOARD_SIZE - 1) {
                makeKing();
            }
        }
    }

    public boolean isValidDirectionalMove(int rowChange) {
        if (king) {
            return true;
        }
        return (color == PieceColor.RED && rowChange == -1) ||
                (color == PieceColor.BLACK && rowChange == 1) ||
                (Math.abs(rowChange) == 2);
    }
}