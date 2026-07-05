package zoo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import zoo.animal.Animal;
import zoo.animal.Bird;
import zoo.animal.Fish;
import zoo.animal.Mammal;
import zoo.animal.Reptile;

public final class Zoo {
    private static final Logger LOGGER = Logger.getLogger(Zoo.class.getName());

    private final List<Enclosure<? extends Animal>> enclosures = new ArrayList<>();

    public boolean addEnclosure(Enclosure<? extends Animal> enclosure) {
        LOGGER.log(Level.INFO, () -> "addEnclosure(enclosure=%s)".formatted(enclosure));
        Objects.requireNonNull(enclosure, "enclosure");

        boolean nameAlreadyUsed = enclosures.stream()
                .anyMatch(existing -> existing.getName().equals(enclosure.getName()));
        if (nameAlreadyUsed) {
            LOGGER.log(Level.WARNING, () -> "Gehege nicht hinzugefuegt, Name existiert bereits: "
                    + enclosure.getName());
            return false;
        }

        boolean added = enclosures.add(enclosure);
        logState("Gehege hinzugefuegt");
        return added;
    }

    public List<Enclosure<? extends Animal>> getEnclosures() {
        LOGGER.info("getEnclosures()");
        List<Enclosure<? extends Animal>> result = List.copyOf(enclosures);
        LOGGER.fine(() -> "Gehege gefunden: " + result.size());
        return result;
    }

    public Optional<Enclosure<? extends Animal>> findEnclosureByName(String name) {
        LOGGER.log(Level.INFO, () -> "findEnclosureByName(name=%s)".formatted(name));
        Optional<Enclosure<? extends Animal>> result = findEnclosure(name);
        if (result.isEmpty()) {
            LOGGER.log(Level.WARNING, () -> "Gehege nicht gefunden: " + name);
        }
        LOGGER.fine(() -> "Suchergebnis vorhanden: " + result.isPresent());
        return result;
    }

    public List<Animal> getAllAnimals() {
        LOGGER.info("getAllAnimals()");
        List<Animal> animals = allAnimalStream().toList();
        LOGGER.fine(() -> "Tiere gefunden: " + animals.size());
        return animals;
    }

    public Optional<Animal> findAnimalByName(String animalName) {
        LOGGER.log(Level.INFO, () -> "findAnimalByName(animalName=%s)".formatted(animalName));
        Objects.requireNonNull(animalName, "animalName");
        Optional<Animal> result = enclosures.stream()
                .map(enclosure -> enclosure.findAnimalByName(animalName))
                .flatMap(Optional::stream)
                .map(Animal.class::cast)
                .findFirst();
        if (result.isEmpty()) {
            LOGGER.warning("Tier nicht gefunden: " + animalName);
        }
        LOGGER.fine(() -> "Suchergebnis vorhanden: " + result.isPresent());
        return result;
    }
    public List<Mammal> getAllMammals() {
        LOGGER.info("getAllMammals()");
        List<Mammal> mammals = allAnimalStream()
                .filter(Mammal.class::isInstance)
                .map(Mammal.class::cast)
                .toList();
        LOGGER.fine(() -> "Saeugetiere gefunden: " + mammals.size());
        return mammals;
    }

    public List<Animal> getAnimalsByPredicate(Predicate<Animal> predicate) {
        LOGGER.info("getAnimalsByPredicate(predicate)");
        Objects.requireNonNull(predicate, "predicate");
        List<Animal> animals = allAnimalStream()
                .filter(predicate)
                .toList();
        LOGGER.fine(() -> "Tiere nach Praedikat gefunden: " + animals.size());
        return animals;
    }

    public Map<Class<? extends Animal>, Long> countAnimalsByType() {
        LOGGER.info("countAnimalsByType()");
        Map<Class<? extends Animal>, Long> counts = allAnimalStream()
                .collect(Collectors.groupingBy(
                        Animal::getClass,
                        LinkedHashMap::new,
                        Collectors.counting()));
        LOGGER.fine(() -> "Tierarten gezaehlt: " + counts.size());
        return counts;
    }

    public List<Enclosure<? extends Animal>> getOvercrowdedEnclosures(int maxAnimals) {
        LOGGER.log(Level.INFO, () -> "getOvercrowdedEnclosures(maxAnimals=%d)".formatted(maxAnimals));
        if (maxAnimals < 0) {
            LOGGER.severe("maxAnimals ist negativ: " + maxAnimals);
            throw new IllegalArgumentException("maxAnimals must not be negative");
        }

        List<Enclosure<? extends Animal>> overcrowded = enclosures.stream()
                .filter(enclosure -> enclosure.size() > maxAnimals)
                .toList();
        LOGGER.fine(() -> "Ueberfuellte Gehege gefunden: " + overcrowded.size());
        return overcrowded;
    }

    public String summary() {
        LOGGER.info("summary()");
        long animalCount = allAnimalStream().count();
        String groups = animalGroupNames().stream()
                .map(entry -> "%d %s".formatted(countAnimalsOfType(entry.getKey()), entry.getValue()))
                .collect(Collectors.joining(", "));
        String summary = "Zoo mit %d Gehegen und %d Tieren: %s"
                .formatted(enclosures.size(), animalCount, groups);
        LOGGER.fine(summary);
        return summary;
    }

    public boolean addAnimal(String enclosureName, Animal animal) {
        LOGGER.log(Level.INFO, () -> "addAnimal(enclosureName=%s, animal=%s)".formatted(enclosureName, animal));
        Objects.requireNonNull(animal, "animal");

        Optional<Enclosure<? extends Animal>> enclosure = findEnclosure(enclosureName);
        if (enclosure.isEmpty()) {
            LOGGER.warning("Tier konnte nicht aufgenommen werden, Gehege fehlt: " + enclosureName);
            return false;
        }
        if (!enclosure.get().canHold(animal)) {
            LOGGER.warning(() -> "Tier passt nicht in das Gehege: %s -> %s".formatted(animal, enclosure.get()));
            return false;
        }

        boolean added = enclosure.get().addAnimal(animal);
        if (!added) {
            LOGGER.warning("Tier war bereits im Gehege: " + animal);
        }
        logState("Tier aufgenommen");
        return added;
    }

    public boolean removeAnimal(String enclosureName, Animal animal) {
        LOGGER.log(Level.INFO, () -> "removeAnimal(enclosureName=%s, animal=%s)".formatted(enclosureName, animal));
        Objects.requireNonNull(animal, "animal");

        Optional<Enclosure<? extends Animal>> enclosure = findEnclosure(enclosureName);
        if (enclosure.isEmpty()) {
            LOGGER.warning("Tier konnte nicht abgegeben werden, Gehege fehlt: " + enclosureName);
            return false;
        }

        boolean removed = enclosure.get().removeAnimal(animal);
        if (!removed) {
            LOGGER.warning("Tier nicht im Gehege gefunden: " + animal);
        }
        logState("Tier abgegeben");
        return removed;
    }

    public boolean moveAnimal(Animal animal, String sourceEnclosureName, String targetEnclosureName) {
        LOGGER.log(Level.INFO, () -> "moveAnimal(animal=%s, source=%s, target=%s)"
                .formatted(animal, sourceEnclosureName, targetEnclosureName));
        Objects.requireNonNull(animal, "animal");

        Optional<Enclosure<? extends Animal>> source = findEnclosure(sourceEnclosureName);
        Optional<Enclosure<? extends Animal>> target = findEnclosure(targetEnclosureName);
        if (source.isEmpty() || target.isEmpty()) {
            LOGGER.warning(() -> "Umsetzen fehlgeschlagen, Gehege fehlt: source=%s, target=%s"
                    .formatted(sourceEnclosureName, targetEnclosureName));
            return false;
        }
        if (!source.get().getInhabitants().contains(animal)) {
            LOGGER.warning("Umsetzen fehlgeschlagen, Tier nicht im Ausgangsgehege: " + animal);
            return false;
        }
        if (!target.get().canHold(animal)) {
            LOGGER.warning(() -> "Umsetzen fehlgeschlagen, Zielgehege hat falschen Typ: " + target.get());
            return false;
        }

        boolean added = target.get().addAnimal(animal);
        if (!added) {
            LOGGER.warning("Umsetzen fehlgeschlagen, Tier ist schon im Zielgehege: " + animal);
            return false;
        }
        boolean removed = source.get().removeAnimal(animal);
        if (!removed) {
            LOGGER.severe("Inkonsistenz: Tier wurde ins Zielgehege kopiert, aber nicht aus Quelle entfernt");
            target.get().removeAnimal(animal);
            return false;
        }

        logState("Tier umgesetzt");
        return true;
    }

    private Optional<Enclosure<? extends Animal>> findEnclosure(String name) {
        return enclosures.stream()
                .filter(enclosure -> enclosure.getName().equals(name))
                .findFirst();
    }

    private Stream<Animal> allAnimalStream() {
        return enclosures.stream()
                .flatMap(enclosure -> enclosure.getInhabitants().stream())
                .map(Animal.class::cast);
    }

    private long countAnimalsOfType(Class<? extends Animal> type) {
        return allAnimalStream()
                .filter(type::isInstance)
                .count();
    }

    private List<Map.Entry<Class<? extends Animal>, String>> animalGroupNames() {
        return List.of(
                Map.entry(Mammal.class, "Mammals"),
                Map.entry(Bird.class, "Birds"),
                Map.entry(Fish.class, "Fish"),
                Map.entry(Reptile.class, "Reptiles"));
    }

    private void logState(String action) {
        LOGGER.fine(() -> "%s: %d Gehege, %d Tiere"
                .formatted(action, enclosures.size(), allAnimalStream().count()));
    }

    public String animalsSortedByName() {
        LOGGER.info("animalsSortedByName()");
        String result = allAnimalStream()
                .sorted(Comparator.comparing(Animal::name).thenComparing(animal -> animal.getClass().getSimpleName()))
                .map(animal -> "%s (%s)".formatted(animal.name(), animal.getClass().getSimpleName()))
                .collect(Collectors.joining(", "));
        LOGGER.fine(() -> "Sortierte Tiere: " + result);
        return result;
    }
}

