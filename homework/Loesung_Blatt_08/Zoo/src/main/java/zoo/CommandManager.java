package zoo;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class CommandManager<T> {
    private static final Logger LOGGER = Logger.getLogger(CommandManager.class.getName());

    private final Deque<Command<? super T>> undoStack = new ArrayDeque<>();
    private final Deque<Command<? super T>> redoStack = new ArrayDeque<>();

    public Result<ZooError, CommandReport> executeCommand(Command<? super T> command, T target) {
        Objects.requireNonNull(command, "command");
        Objects.requireNonNull(target, "target");
        LOGGER.info(() -> "executeCommand(command=%s, target=%s)".formatted(command.description(), target));

        Result<ZooError, CommandReport> result = command.execute(target);
        switch (result) {
            case Result.Success<ZooError, CommandReport> success -> {
                undoStack.push(command);
                redoStack.clear();
                logSuccess("Command ausgefuehrt", success.value());
            }
            case Result.Failure<ZooError, CommandReport> failure ->
                    logFailure("Command nicht ausgefuehrt", failure.error(), command.description());
        }
        logStacks();
        return result;
    }

    public Result<ZooError, CommandReport> undo(T target) {
        Objects.requireNonNull(target, "target");
        LOGGER.info(() -> "undo(target=%s)".formatted(target));
        if (undoStack.isEmpty()) {
            Result<ZooError, CommandReport> result = Result.failure(ZooError.UNDO_STACK_EMPTY);
            logFailure("Undo nicht moeglich", ZooError.UNDO_STACK_EMPTY, target.toString());
            return result;
        }

        Command<? super T> command = undoStack.pop();
        Result<ZooError, CommandReport> result = command.undo(target);
        switch (result) {
            case Result.Success<ZooError, CommandReport> success -> {
                redoStack.push(command);
                logSuccess("Undo ausgefuehrt", success.value());
            }
            case Result.Failure<ZooError, CommandReport> failure -> {
                undoStack.push(command);
                logFailure("Undo fehlgeschlagen", failure.error(), command.description());
            }
        }
        logStacks();
        return result;
    }

    public Result<ZooError, CommandReport> redo(T target) {
        Objects.requireNonNull(target, "target");
        LOGGER.info(() -> "redo(target=%s)".formatted(target));
        if (redoStack.isEmpty()) {
            Result<ZooError, CommandReport> result = Result.failure(ZooError.REDO_STACK_EMPTY);
            logFailure("Redo nicht moeglich", ZooError.REDO_STACK_EMPTY, target.toString());
            return result;
        }

        Command<? super T> command = redoStack.pop();
        Result<ZooError, CommandReport> result = command.execute(target);
        switch (result) {
            case Result.Success<ZooError, CommandReport> success -> {
                undoStack.push(command);
                logSuccess("Redo ausgefuehrt", success.value());
            }
            case Result.Failure<ZooError, CommandReport> failure -> {
                redoStack.push(command);
                logFailure("Redo fehlgeschlagen", failure.error(), command.description());
            }
        }
        logStacks();
        return result;
    }

    private void logSuccess(String action, CommandReport report) {
        LOGGER.fine(() -> "%s: %s in %s, Bewohner=%d"
                .formatted(action, report.description(), report.enclosureName(), report.inhabitants()));
    }

    private void logFailure(String action, ZooError error, String context) {
        LOGGER.log(Level.WARNING, () -> "%s: %s (%s)".formatted(action, error, context));
    }

    private void logStacks() {
        LOGGER.fine(() -> "undoStack=%d, redoStack=%d".formatted(undoStack.size(), redoStack.size()));
    }
}
