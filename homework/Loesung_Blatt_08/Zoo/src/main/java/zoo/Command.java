package zoo;

public interface Command<T> {
    Result<ZooError, CommandReport> execute(T target);

    Result<ZooError, CommandReport> undo(T target);

    String description();
}
