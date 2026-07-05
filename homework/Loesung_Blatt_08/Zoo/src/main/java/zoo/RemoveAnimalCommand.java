package zoo;

import java.util.Objects;
import zoo.animal.Animal;

public final class RemoveAnimalCommand<T extends Animal> implements Command<Enclosure<? super T>> {
    private final T animal;
    private boolean executed;

    public RemoveAnimalCommand(T animal) {
        this.animal = Objects.requireNonNull(animal, "animal");
    }

    @Override
    public Result<ZooError, CommandReport> execute(Enclosure<? super T> target) {
        Objects.requireNonNull(target, "target");
        if (!target.remove(animal)) {
            return Result.failure(ZooError.ANIMAL_NOT_FOUND);
        }
        executed = true;
        return Result.success(report(target, "Tier entnommen: " + animal.name()));
    }

    @Override
    public Result<ZooError, CommandReport> undo(Enclosure<? super T> target) {
        Objects.requireNonNull(target, "target");
        if (!executed) {
            return Result.failure(ZooError.COMMAND_NOT_EXECUTED);
        }
        if (!target.add(animal)) {
            return Result.failure(ZooError.ANIMAL_ALREADY_PRESENT);
        }
        executed = false;
        return Result.success(report(target, "Entnahme rueckgaengig: " + animal.name()));
    }

    @Override
    public String description() {
        return "RemoveAnimalCommand[" + animal.name() + "]";
    }

    private CommandReport report(Enclosure<? super T> target, String description) {
        return new CommandReport(description, target.getName(), target.size());
    }
}
