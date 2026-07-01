package zoo.animal;

public sealed interface Primate extends Mammal permits Chimpanzee, Gorilla {
}
