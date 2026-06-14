package de.hsbi.lockgame.logic;

import de.hsbi.lockgame.model.*;
import java.util.List;

public final class GameState {
    // Speichert den vollständigen Spielzustand zu einem bestimmten Zeitpunkt.
    private final Level level;
    private final Snake snake;
    private final List<Pin> pins;
    private final Status status;
    private final Direction pendingDirection;

    // Erstellt einen neuen Spielzustand aus Level, Schlange, Pins, Status und Eingaberichtung.
    public GameState(
            Level level, Snake snake, List<Pin> pins, Status status, Direction pendingDirection) {
        // TODO: lege einen neuen GameState mit den übergebenen Informationen an
        this.level = level;
        this.snake = snake;
        this.pins = List.copyOf(pins);
        this.status = status;
        this.pendingDirection = pendingDirection;

    }

    public Level level() {
        // TODO: Getter
        return level;
    }

    public Snake snake() {
        // TODO: Getter
        return snake;
    }

    public List<Pin> pins() {
        // TODO: Getter
        return pins;
    }

    public Status status() {
        // TODO: Getter
        return status;
    }

    public Direction pendingDirection() {
        // TODO: Getter
        return  pendingDirection;
    }

    public GameState tick() {
        // TODO: diese Methode lässt das Spiel einen Schritt laufen (berechnet den Spielzustand im
        // nächsten Schritt)

        // TODO: early exit: wenn das Spiel nicht läuft oder keine Blickrichtung gesetzt ist: keine
        // Änderung
        if (!status.isRunning() || pendingDirection == Direction.NONE) {
            return this;
        }

        Position nextHead = snake.nextHead(pendingDirection);

        // TODO: prüfe die folgenden Bedingungen:
        // (a) Schlange würde das Spielfeld verlassen: Spiel verloren
        if (!level.isInside(nextHead)) {
            return new GameState(level, snake, pins, Status.LOST_OUT_OF_BOUNDS, Direction.NONE);
        }

        // (b) Schlange würde in ein Wandelement gehen: Blockiert
        if (level.cellAt(nextHead) == CellType.WALL) {
            return new GameState(level, snake, pins, Status.RUNNING, Direction.NONE);
        }

        // (c) Schlange beisst sich: Spiel verloren
        if (snake.occupies(nextHead)) {
            return new GameState(level, snake, pins, Status.LOST_SELF_COLLISION, Direction.NONE);
        }

        Pin pin = pinAt(nextHead);

        // (d) Schlange würde auf einen Pin gehen
        if (pin != null) {
            if (pin.state().isSet() || pendingDirection != pin.activationDirection()) {
                return new GameState(level, snake, pins, Status.RUNNING, Direction.NONE);
            }

            // TODO: aktiviere einen noch nicht gesetzten Pin, wenn die Schlange in der richtigen
            // Richtung auf den Pin gehen würde
            List<Pin> newPins = pins.stream()
                .map(p -> p.position().equals(nextHead) ? p.withState(Pin.State.HIGH) : p)
                .toList();

            Status newStatus = newPins.stream()
                .map(Pin::state)
                .allMatch(Pin.State::isSet)
                ? Status.WON
                : Status.RUNNING;

            return new GameState(level, snake, newPins, newStatus, Direction.NONE);
        }

        // TODO: anderenfalls: bewege die Schlange um einen Schritt in Blickrichtung
        return new GameState(
            level,
            snake.grow(pendingDirection),
            pins,
            Status.RUNNING,
            pendingDirection);
    }




    // Gibt es an der nächsten Position einen Pin?
    private Pin pinAt(Position position) {
        return pins.stream()
            .filter(pin -> pin.position().equals(position))
            .findFirst()
            .orElse(null);
    }
    public enum Status {
        RUNNING,
        WON,
        LOST_SELF_COLLISION,
        LOST_OUT_OF_BOUNDS;

        public boolean isRunning() {
            return this == RUNNING;
        }
    }
}
