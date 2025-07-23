package com.evancaplan.checkersengine.service;

import com.evancaplan.checkersengine.model.Board;
import com.evancaplan.checkersengine.model.Move;
import com.evancaplan.checkersengine.model.Piece;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MoveService {

    public boolean apply(Board board, Move move) {
        if (!board.isValidMove(move)) {
            return false;
        }

        board.movePiece(move);

        return true;
    }

    // Simple AI move generator
    // First capture it can make, else first legal step.
    public Move generateAIMove(Board board) {
        List<Piece> redPieces = board.getRedPieces();
        
        for (Piece piece : redPieces) {
            List<Move> captureMoves = board.getCaptureMovesForPiece(piece);
            if (!captureMoves.isEmpty()) {
                return captureMoves.getFirst();
            }
        }
        
       for (Piece piece : redPieces) {
            List<Move> simpleMoves = board.getSimpleMovesForPiece(piece);
            if (!simpleMoves.isEmpty()) {
                return simpleMoves.getFirst();
            }
        }
        
        // No valid moves found
        return null;
    }
    
}

