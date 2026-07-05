package zoo;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import zoo.animal.Animal;

public class Enclosure<T extends Animal> {
    private final String name;
    private final Class<T> animalType;
    private final Set<T> inhabitants = new LinkedHashSet<>();

    public Enclosure(String name, Class<T> animalType) {
        this.name = requireNonBlank(name, "name");
        this.animalType = Objects.requireNonNull(animalType, "animalType");
    }

    public String getName() {
        return name;
    }

    public Class<T> getAnimalType() {
        return animalType;
    }

    public boolean add(T animal) {
        Objects.requireNonNull(animal, "animal");
        return inhabitants.add(animal);
    }

    public boolean remove(T animal) {
        Objects.requireNonNull(animal, "animal");
        return inhabitants.remove(animal);
    }

    public List<T> getInhabitants() {
        return List.copyOf(inhabitants);
    }

    public Optional<T> findAnimalByName(String animalName) {
        Objects.requireNonNull(animalName, "animalName");
        return inhabitants.stream()
                .filter(animal -> animal.name().equals(animalName))
                .findFirst();
    }
    public int size() {
        return inhabitants.size();
    }

    public boolean canHold(Animal animal) {
        return animalType.isInstance(animal);
    }

    public boolean addAnimal(Animal animal) {
        if (!canHold(animal)) {
            return false;
        }
        return add(animalType.cast(animal));
    }

    public boolean removeAnimal(Animal animal) {
        if (!canHold(animal)) {
            return false;
        }
        return remove(animalType.cast(animal));
    }

    @Override
    public String toString() {
        return "%s[name=%s, animalType=%s, inhabitants=%d]"
                .formatted(getClass().getSimpleName(), name, animalType.getSimpleName(), size());
    }

    private static String requireNonBlank(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName);
        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }
}

