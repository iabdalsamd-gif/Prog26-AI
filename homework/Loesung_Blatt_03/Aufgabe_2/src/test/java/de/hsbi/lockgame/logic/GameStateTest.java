package de.hsbi.lockgame.logic;

import static org.junit.jupiter.api.Assertions.*;

import de.hsbi.lockgame.model.CellType;
import de.hsbi.lockgame.model.Direction;
import de.hsbi.lockgame.model.Level;
import de.hsbi.lockgame.model.Pin;
import de.hsbi.lockgame.model.Position;
import de.hsbi.lockgame.model.Snake;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GameStateTest {

    private static Level level(Position snakeStart, List<Pin> pins, Position... walls) {
        int width = 5;
        int height = 5;
        CellType[][] cells = emptyCells(width, height);

        for (Position wall : walls) {
            cells[wall.x()][wall.y()] = CellType.WALL;
        }

        for (Pin pin : pins) {
            cells[pin.position().x()][pin.position().y()] = CellType.PIN_SLOT;
        }

        return new Level(width, height, cells, pins, snakeStart);
    }

    private static CellType[][] emptyCells(int width, int height) {
        CellType[][] cells = new CellType[width][height];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                cells[x][y] = CellType.EMPTY;
            }
        }

        return cells;
    }

    private static GameState runningState(
        Level level, Snake snake, List<Pin> pins, Direction pendingDirection) {
        return new GameState(level, snake, pins, GameState.Status.RUNNING, pendingDirection);
    }


    // Prüft, ob der GameState nach dem Erstellen alle übergebenen Werte korrekt speichert
    // und über die Getter wieder zurückgibt.
    @Test
    @DisplayName("givenInitialValues_whenGameStateCreated_thenGettersReturnValues")
    void givenInitialValues_whenGameStateCreated_thenGettersReturnValues() {
        Position start = new Position(2, 2);
        List<Pin> pins = List.of();
        Level level = level(start, pins);
        Snake snake = new Snake(List.of(start));

        GameState state =
            new GameState(level, snake, pins, GameState.Status.RUNNING, Direction.NONE);

        assertEquals(level, state.level());
        assertEquals(snake, state.snake());
        assertEquals(pins, state.pins());
        assertEquals(GameState.Status.RUNNING, state.status());
        assertEquals(Direction.NONE, state.pendingDirection());
    }

    // Prüft, dass sich der Spielzustand nicht verändert,
    // wenn keine Bewegungsrichtung gesetzt ist.
    @Test
    @DisplayName("givenNoPendingDirection_whenTick_thenStateDoesNotChange")
    void givenNoPendingDirection_whenTick_thenStateDoesNotChange() {
        Position start = new Position(2, 2);
        List<Pin> pins = List.of();
        Level level = level(start, pins);
        Snake snake = new Snake(List.of(start));
        GameState state = runningState(level, snake, pins, Direction.NONE);

        GameState next = state.tick();

        assertSame(state, next);
    }

    // Prüft eine normale Bewegung auf ein freies Feld:
    // Die Schlange bewegt sich nach rechts und wächst um ein Segment.
    @Test
    @DisplayName("givenFreeCellRight_whenTick_thenSnakeGrowsToRight")
    void givenFreeCellRight_whenTick_thenSnakeGrowsToRight() {
        Position start = new Position(2, 2);
        List<Pin> pins = List.of();
        Level level = level(start, pins);
        Snake snake = new Snake(List.of(start));
        GameState state = runningState(level, snake, pins, Direction.RIGHT);

        GameState next = state.tick();

        assertEquals(GameState.Status.RUNNING, next.status());
        assertEquals(new Position(3, 2), next.snake().head());
        assertEquals(2, next.snake().body().size());
        assertEquals(Direction.RIGHT, next.pendingDirection());
    }

    // Prüft, dass eine Wand die Bewegung blockiert:
    // Die Schlange bleibt stehen, das Spiel läuft weiter und die Richtung wird zurückgesetzt.
    @Test
    @DisplayName("givenWallInFront_whenTick_thenMovementIsBlocked")
    void givenWallInFront_whenTick_thenMovementIsBlocked() {
        Position start = new Position(2, 2);
        Position wall = new Position(3, 2);
        List<Pin> pins = List.of();
        Level level = level(start, pins, wall);
        Snake snake = new Snake(List.of(start));
        GameState state = runningState(level, snake, pins, Direction.RIGHT);

        GameState next = state.tick();

        assertEquals(GameState.Status.RUNNING, next.status());
        assertEquals(start, next.snake().head());
        assertEquals(1, next.snake().body().size());
        assertEquals(Direction.NONE, next.pendingDirection());
    }

    // Prüft, dass das Spiel verloren ist,
    // wenn die Schlange das Spielfeld verlassen würde.
    @Test
    @DisplayName("givenSnakeWouldLeaveLevel_whenTick_thenGameIsLostOutOfBounds")
    void givenSnakeWouldLeaveLevel_whenTick_thenGameIsLostOutOfBounds() {
        Position start = new Position(0, 2);
        List<Pin> pins = List.of();
        Level level = level(start, pins);
        Snake snake = new Snake(List.of(start));
        GameState state = runningState(level, snake, pins, Direction.LEFT);

        GameState next = state.tick();

        assertEquals(GameState.Status.LOST_OUT_OF_BOUNDS, next.status());
        assertEquals(start, next.snake().head());
        assertEquals(Direction.NONE, next.pendingDirection());
    }
    // Prüft, dass das Spiel verloren ist,
    // wenn die Schlange in ihren eigenen Körper läuft.
    @Test
    @DisplayName("givenSnakeWouldHitItself_whenTick_thenGameIsLostBySelfCollision")
    void givenSnakeWouldHitItself_whenTick_thenGameIsLostBySelfCollision() {
        Position head = new Position(2, 2);
        Position bodyPart = new Position(3, 2);
        List<Pin> pins = List.of();
        Level level = level(head, pins);
        Snake snake = new Snake(List.of(head, bodyPart));
        GameState state = runningState(level, snake, pins, Direction.RIGHT);

        GameState next = state.tick();

        assertEquals(GameState.Status.LOST_SELF_COLLISION, next.status());
        assertEquals(head, next.snake().head());
        assertEquals(Direction.NONE, next.pendingDirection());
    }
    // Prüft, dass ein Pin die Bewegung blockiert,
    // wenn die Schlange ihn aus der falschen Richtung erreicht.
    @Test
    @DisplayName("givenPinHitFromWrongDirection_whenTick_thenPinBlocksMovement")
    void givenPinHitFromWrongDirection_whenTick_thenPinBlocksMovement() {
        Position start = new Position(2, 2);
        Pin pin = new Pin(new Position(3, 2), Pin.State.LOW, Direction.LEFT);
        List<Pin> pins = List.of(pin);
        Level level = level(start, pins);
        Snake snake = new Snake(List.of(start));
        GameState state = runningState(level, snake, pins, Direction.RIGHT);

        GameState next = state.tick();

        assertEquals(GameState.Status.RUNNING, next.status());
        assertEquals(start, next.snake().head());
        assertEquals(Pin.State.LOW, next.pins().getFirst().state());
        assertEquals(Direction.NONE, next.pendingDirection());
    }

    // Prüft, dass ein Pin aktiviert wird,
    // wenn die Schlange ihn aus der richtigen Richtung erreicht.
    @Test
    @DisplayName("givenPinHitFromCorrectDirection_whenTick_thenPinIsActivated")
    void givenPinHitFromCorrectDirection_whenTick_thenPinIsActivated() {
        Position start = new Position(2, 2);
        Pin pin = new Pin(new Position(3, 2), Pin.State.LOW, Direction.RIGHT);
        List<Pin> pins = List.of(pin);
        Level level = level(start, pins);
        Snake snake = new Snake(List.of(start));
        GameState state = runningState(level, snake, pins, Direction.RIGHT);

        GameState next = state.tick();

        assertEquals(start, next.snake().head());
        assertEquals(Pin.State.HIGH, next.pins().getFirst().state());
        assertEquals(Direction.NONE, next.pendingDirection());
    }

    // Prüft die Gewinnbedingung:
    // Wenn nach der Aktivierung alle Pins gesetzt sind, ist das Spiel gewonnen.
    @Test
    @DisplayName("givenAllPinsSetAfterActivation_whenTick_thenGameIsWon")
    void givenAllPinsSetAfterActivation_whenTick_thenGameIsWon() {
        Position start = new Position(2, 2);
        Pin pin = new Pin(new Position(3, 2), Pin.State.LOW, Direction.RIGHT);
        List<Pin> pins = List.of(pin);
        Level level = level(start, pins);
        Snake snake = new Snake(List.of(start));
        GameState state = runningState(level, snake, pins, Direction.RIGHT);

        GameState next = state.tick();

        assertEquals(GameState.Status.WON, next.status());
        assertEquals(Pin.State.HIGH, next.pins().getFirst().state());
    }

    // Prüft, dass ein bereits gesetzter Pin die Bewegung blockiert
    // und nicht erneut verändert wird.
    @Test
    @DisplayName("givenAlreadySetPinInFront_whenTick_thenMovementIsBlocked")
    void givenAlreadySetPinInFront_whenTick_thenMovementIsBlocked() {
        Position start = new Position(2, 2);
        Pin pin = new Pin(new Position(3, 2), Pin.State.HIGH, Direction.RIGHT);
        List<Pin> pins = List.of(pin);
        Level level = level(start, pins);
        Snake snake = new Snake(List.of(start));
        GameState state = runningState(level, snake, pins, Direction.RIGHT);

        GameState next = state.tick();

        assertEquals(GameState.Status.RUNNING, next.status());
        assertEquals(start, next.snake().head());
        assertEquals(Pin.State.HIGH, next.pins().getFirst().state());
        assertEquals(Direction.NONE, next.pendingDirection());
    }

    // Prüft, dass ein bereits beendetes Spiel durch tick()
    // nicht weiter verändert wird.
    @Test
    @DisplayName("givenGameAlreadyWon_whenTick_thenStateDoesNotChange")
    void givenGameAlreadyWon_whenTick_thenStateDoesNotChange() {
        Position start = new Position(2, 2);
        List<Pin> pins = List.of();
        Level level = level(start, pins);
        Snake snake = new Snake(List.of(start));

        GameState state =
            new GameState(level, snake, pins, GameState.Status.WON, Direction.RIGHT);

        GameState next = state.tick();

        assertSame(state, next);
    }
}
