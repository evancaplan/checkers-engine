# Checkers Engine

A lightweight Spring Boot implementation of a checkers game engine with REST API endpoints. This engine provides the core functionality for playing checkers, including move validation, piece promotion, and an optional AI opponent.

MoveRequest

## API Documentation

### Start a New Game

**Endpoint:** `POST /api/game/new`

**Request Body:**
```json
{
  "singlePlayer": true
}
```

**Response:**
```json
{
  "gameId": "550e8400-e29b-41d4-a716-446655440000",
  "message": "New game started successfully"
}
```

### Get Board State

**Endpoint:** `GET /api/game/state?gameId={gameId}`

**Response:**
```json
{
  "gameId": "550e8400-e29b-41d4-a716-446655440000",
  "pieces": [
    {
      "color": "BLACK",
      "king": false,
      "row": 0,
      "col": 1
    },
    {
      "color": "BLACK",
      "king": false,
      "row": 0,
      "col": 3
    },
    {
      "color": "RED",
      "king": false,
      "row": 7,
      "col": 0
    }
  ],
  "currentTurn": "BLACK",
  "gameOver": false,
  "winner": null,
  "singlePlayer": true
}
```

### Make a Move

**Endpoint:** `POST /api/game/move`

**Request Body:**
```json
{
  "gameId": "550e8400-e29b-41d4-a716-446655440000",
  "fromRow": 2,
  "fromCol": 1,
  "toRow": 3,
  "toCol": 0
}
```

**Response (Success):**
```json
{
  "gameId": "550e8400-e29b-41d4-a716-446655440000",
  "message": "Move applied successfully"
}
```

**Response (Invalid Move):**
```json
{
  "gameId": "550e8400-e29b-41d4-a716-446655440000",
  "message": "Illegal move – please try again"
}
```