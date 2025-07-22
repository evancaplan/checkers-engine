package com.evancaplan.checkersengine.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MoveRequest {
    @NotBlank
    private String gameId;

    @NotNull
    private int fromRow;

    @NotNull
    private int fromCol;

    @NotNull
    private int toRow;

    @NotNull
    private int toCol;
}