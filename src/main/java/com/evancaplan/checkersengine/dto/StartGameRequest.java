package com.evancaplan.checkersengine.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class StartGameRequest {
    @Builder.Default
    private Boolean singlePlayer = true;
}
