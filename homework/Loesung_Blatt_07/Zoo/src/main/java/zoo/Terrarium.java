package zoo;

import zoo.animal.Reptile;

public final class Terrarium<T extends Reptile> extends Enclosure<T> {
    public Terrarium(String name, Class<T> reptileType) {
        super(name, reptileType);
    }
}
