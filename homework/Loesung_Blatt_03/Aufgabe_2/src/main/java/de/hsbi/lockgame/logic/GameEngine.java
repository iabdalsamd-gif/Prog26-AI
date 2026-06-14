package de.hsbi.lockgame.logic;

import de.hsbi.lockgame.model.Direction;
import de.hsbi.lockgame.model.Level;
import de.hsbi.lockgame.ui.GamePanel;
import de.hsbi.lockgame.model.Snake;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

// TODO: Die GameEngine verwaltet den GameState.

// TODO: Die GameEngine wird durch den Timer im main() getriggert ("tick") und lässt den GameState
// daraufhin einen Schritt ausführen. Dann müssen alle für den GameState registrierten Observer
// benachrichtigt werden, damit das Spielfeld neu gezeichnet werden kann o.ä.

// TODO: Die GameEngine beobachtet die Tastatureingaben (gesetzt in GamePanel.setupKeyBindings()),
// die in Direction übersetzt und an GameEngine.update() übergeben werden. Wenn es eine neue Eingabe
// gibt, wird die "update"-Methode von GameEngine aufgerufen, und die GameEngine muss die
// Blickrichtung der Schlange aktualisieren und diese GameState-Änderung den für den GameState
// registrierten Observer mitteilen.

// TODO: Die GameEngine ist ein Observer für Direction: GameEngine.update(Direction)
// TODO: Die GameEngine ist ein Observable für GameState: GamePanel.update(GameState)
public final class GameEngine {
    private GameState state;
    private final List<Consumer<GameState>> observers = new ArrayList<>();

    public GameEngine(Level level) {
        // TODO: lege eine neue GameEngine mit den übergebenen Informationen an
        this.state =
            new GameState(
                level,
                new Snake(List.of(level.snakeStart())),
                level.pins(),
                GameState.Status.RUNNING,
                Direction.NONE);
    }

    public GameState state() {
        // TODO: gebe den aktuellen Spielzustand zurück
        return state;
    }

    public void setGamePanel(GamePanel panel) {
        // TODO: Setter
        observers.add(panel::update);
    }

    public void update(Direction d) {
        // TODO: aktualisiere den Blickwinkel der Schlange (GameState)
        if (!state.status().isRunning()) {
            return;
        }

        state = new GameState(
            state.level(),
            state.snake(),
            state.pins(),
            state.status(),
            d
        );

        // TODO: benachrichtige alle Observer und gibt den neuen Spielzustand mit (Neuzeichnen der
        // Spielfläche)
        notifyObservers();
    }
    private void notifyObservers() {
        observers.forEach(observer -> observer.accept(state));
    }
    public void tick() {
        // TODO: lasse den GameState einen Schritt ausführen
        state = state.tick();

        // TODO: benachrichtige alle Observer und gibt den neuen Spielzustand mit (Neuzeichnen der
        // Spielfläche)
        notifyObservers();
    }
}
