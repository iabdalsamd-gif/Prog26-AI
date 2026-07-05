package zoo.animal;

public sealed interface Rodent extends Mammal permits Beaver, Mouse {
}
