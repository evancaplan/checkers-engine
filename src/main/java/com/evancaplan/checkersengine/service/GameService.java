package com.evancaplan.checkersengine.service;

import com.evancaplan.checkersengine.dto.MoveRequest;
import com.evancaplan.checkersengine.dto.StartGameRequest;
import com.evancaplan.checkersengine.model.Board;
import com.evancaplan.checkersengine.model.Move;
import com.evancaplan.checkersengine.model.Piece;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.evancaplan.checkersengine.model.Piece.PieceColor.*;

@Service
@RequiredArgsConstructor
public class GameService {

    private final MoveService moveService;

    private final Map<String, Board> games = new ConcurrentHashMap<>();

    public String startNewGame(StartGameRequest request) {
        String id = UUID.randomUUID().toString();
        games.put(id, Board.createStandardBoard(request.getSinglePlayer()));
        return id;
    }

    public Board getBoardState(String gameId) {
        return games.get(gameId);
    }

    public boolean makeMove(MoveRequest request) {
        Board board = games.get(request.getGameId());

        if (board == null) {
            return false;
        }

        Piece piece = board.getPieceAt(request.getFromRow(), request.getFromCol());

        if (piece == null || piece.getColor() != board.getCurrentTurn()) {
            return false;
        }

        Move playerMove = Move.fromRequest(piece, request);

        if (!moveService.apply(board, playerMove)) {
            return false;
        }

        if (board.isGameOver()) {
            return true;
        }

        // only generate AI move if the player is BLACK and the flag for single player is true
        if (board.isSinglePlayer() && piece.getColor() == BLACK) {
            Move aiMove = moveService.generateAIMove(board);
            if (aiMove != null) {
                moveService.apply(board, aiMove);
            }
        }
        
        return true;
    }

    public boolean gameExists(String gameId) {
        return games.containsKey(gameId);
    }
}
