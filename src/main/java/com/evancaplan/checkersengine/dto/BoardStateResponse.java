package com.evancaplan.checkersengine.dto;

import com.evancaplan.checkersengine.model.Board;
import com.evancaplan.checkersengine.model.Piece;
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
public class BoardStateResponse {
    private String gameId;
    @Builder.Default
    private List<PieceDto> pieces = new ArrayList<>();
    private String currentTurn;
    private boolean gameOver;
    private String winner;
    private boolean singlePlayer;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PieceDto {
        private String color;
        private boolean king;
        private int row;
        private int col;
    }

    public static BoardStateResponse fromBoard(String gameId, Board board) {
        BoardStateResponse.BoardStateResponseBuilder responseBuilder = BoardStateResponse.builder()
                .gameId(gameId)
                .pieces(new ArrayList<>())
                .currentTurn(board.getCurrentTurn().toString())
                .gameOver(board.isGameOver())
                .singlePlayer(board.isSinglePlayer());

        // Set winner if game is over
        if (board.isGameOver() && board.getWinner() != null) {
            responseBuilder.winner(board.getWinner().toString());
        }

        BoardStateResponse response = responseBuilder.build();

        // Add all pieces to the response
        for (int row = 0; row < Board.BOARD_SIZE; row++) {
            for (int col = 0; col < Board.BOARD_SIZE; col++) {
                Piece piece = board.getPieceAt(row, col);
                if (piece != null) {
                    PieceDto pieceDto = PieceDto.builder()
                            .color(piece.getColor().toString())
                            .king(piece.isKing())
                            .row(piece.getRow())
                            .col(piece.getCol())
                            .build();
                    response.getPieces().add(pieceDto);
                }
            }
        }

        return response;
    }
}