package com.evancaplan.checkersengine.model;

import com.evancaplan.checkersengine.dto.MoveRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Move {
    private int fromRow;
    private int fromCol;
    private int toRow;
    private int toCol;
    private Piece piece;
    @Builder.Default
    private List<Piece> capturedPieces = new ArrayList<>();
    private boolean kingMove;

    public static Move fromRequest(Piece piece, MoveRequest req) {
        return Move.builder()
                .fromRow(req.getFromRow())
                .fromCol(req.getFromCol())
                .toRow(req.getToRow())
                .toCol(req.getToCol())
                .piece(piece)
                .build();
    }
    
    public boolean isCapture() {
        return capturedPieces != null && !capturedPieces.isEmpty();
    }
}