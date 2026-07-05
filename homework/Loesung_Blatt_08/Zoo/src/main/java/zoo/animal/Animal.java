package zoo.animal;

public sealed interface Animal permits Bird, Fish, Mammal, Reptile {
    String name();
}
