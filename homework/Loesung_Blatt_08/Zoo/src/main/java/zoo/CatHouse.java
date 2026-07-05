package zoo;

import java.lang.reflect.Modifier;
import zoo.animal.Cat;

public final class CatHouse<T extends Cat> extends Enclosure<T> {
    public CatHouse(String name, Class<T> catType) {
        super(name, requireConcreteCatType(catType));
    }

    private static <T extends Cat> Class<T> requireConcreteCatType(Class<T> catType) {
        if (catType.isInterface() || Modifier.isAbstract(catType.getModifiers())) {
            throw new IllegalArgumentException("CatHouse needs a concrete Cat type");
        }
        return catType;
    }
}
