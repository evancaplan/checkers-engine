package com.evancaplan.checkersengine.controller;

import com.evancaplan.checkersengine.dto.MoveRequest;
import com.evancaplan.checkersengine.dto.StartGameRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class GameControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void startNewGame_returnsGameIdAndSuccessMessage() throws Exception {
        StartGameRequest request = StartGameRequest.builder().singlePlayer(true).build();

        mockMvc.perform(post("/api/game/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.gameId", notNullValue()))
                .andExpect(jsonPath("$.message", is("New game started successfully")));
    }

    @Test
    public void makeMove() throws Exception {
        MvcResult startGameResult = startGame();

        String responseJson = startGameResult.getResponse().getContentAsString();
        String gameId = objectMapper.readTree(responseJson).get("gameId").asText();

        MoveRequest moveRequest = new MoveRequest(gameId, 2, 1, 3, 0);

        mockMvc.perform(post("/api/game/move")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(moveRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId", is(gameId)))
                .andExpect(jsonPath("$.message", containsString("Move applied")));
    }

    @Test
    public void makeMove_returnsNotFoundForNonExistentGame() throws Exception {
        MoveRequest moveRequest = new MoveRequest("non-existent-id", 2, 1, 3, 2);

        mockMvc.perform(post("/api/game/move")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(moveRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.gameId", is("non-existent-id")))
                .andExpect(jsonPath("$.message", containsString("Game with id 'non-existent-id' not found")));
    }

    @Test
    public void getBoardState() throws Exception {
        MvcResult startGameResult = startGame();

        String responseJson = startGameResult.getResponse().getContentAsString();
        String gameId = objectMapper.readTree(responseJson).get("gameId").asText();

        mockMvc.perform(get("/api/game/state")
                        .param("gameId", gameId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId", is(gameId)))
                .andExpect(jsonPath("$.pieces", hasSize(24)))
                .andExpect(jsonPath("$.currentTurn", is("BLACK")))
                .andExpect(jsonPath("$.gameOver", is(false)))
                .andExpect(jsonPath("$.winner", nullValue()))
                .andExpect(jsonPath("$.singlePlayer", is(true)));
    }

    @Test
    public void getBoardState_returnsNotFoundForNonExistentGame() throws Exception {
        mockMvc.perform(get("/api/game/state")
                        .param("gameId", "non-existent-id"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.gameId", is("non-existent-id")))
                .andExpect(jsonPath("$.message", containsString("Game with id 'non-existent-id' not found")));
    }

    private MvcResult startGame() throws Exception {
        StartGameRequest request = StartGameRequest.builder().singlePlayer(true).build();

        return mockMvc.perform(post("/api/game/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn();
    }
}