package zoo;

import zoo.animal.Mammal;

public final class MammalHouse<T extends Mammal> extends Enclosure<T> {
    public MammalHouse(String name, Class<T> mammalType) {
        super(name, mammalType);
    }
}
