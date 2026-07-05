package zoo;

import zoo.animal.Fish;

public final class Aquarium<T extends Fish> extends Enclosure<T> {
    public Aquarium(String name, Class<T> fishType) {
        super(name, fishType);
    }
}
