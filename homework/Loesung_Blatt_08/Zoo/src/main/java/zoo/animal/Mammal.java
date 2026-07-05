package zoo.animal;

public sealed interface Mammal extends Animal permits Cat, Elephant, Primate, Rodent, Wolf {
}
