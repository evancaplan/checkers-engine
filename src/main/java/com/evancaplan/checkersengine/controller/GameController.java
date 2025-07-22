package com.evancaplan.checkersengine.controller;

import com.evancaplan.checkersengine.dto.BoardStateResponse;
import com.evancaplan.checkersengine.dto.GameResponse;
import com.evancaplan.checkersengine.dto.MoveRequest;
import com.evancaplan.checkersengine.dto.StartGameRequest;
import com.evancaplan.checkersengine.model.Board;
import com.evancaplan.checkersengine.service.GameService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/game")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;

    @PostMapping("/new")
    public ResponseEntity<GameResponse> startNewGame(@RequestBody StartGameRequest startGameRequest) {
        String gameId = gameService.startNewGame(startGameRequest);

        return ResponseEntity.ok(
                GameResponse.builder()
                        .gameId(gameId)
                        .message("New game started successfully")
                        .build());
    }


    @PostMapping("/move")
    public ResponseEntity<GameResponse> makeMove(@Valid @RequestBody MoveRequest moveRequest) {

        if (!gameService.gameExists(moveRequest.getGameId())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(buildNotFoundResponse(moveRequest.getGameId()));
        }

        boolean moveSuccess = gameService.makeMove(moveRequest);

        GameResponse response = GameResponse.builder()
                .gameId(moveRequest.getGameId())
                .message(moveSuccess ? "Move applied successfully" : "Illegal move! Please try again")
                .build();

        return moveSuccess
                ? ResponseEntity.ok(response)
                : ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }


    @GetMapping("/state")
    public ResponseEntity<?> getBoardState(@RequestParam String gameId) {
        if (!gameService.gameExists(gameId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(buildNotFoundResponse(gameId));
        }
        var board = gameService.getBoardState(gameId);
        BoardStateResponse boardResponse = BoardStateResponse.fromBoard(gameId, board);
        return ResponseEntity.ok(boardResponse);
    }


    private static GameResponse buildNotFoundResponse(String gameId) {
        return GameResponse.builder()
                .gameId(gameId)
                .message("Game with id '" + gameId + "' not found")
                .build();
    }
}