package zoo;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import zoo.animal.Eagle;
import zoo.animal.Elephant;
import zoo.animal.Fish;
import zoo.animal.Gorilla;
import zoo.animal.Lion;
import zoo.animal.Mammal;
import zoo.animal.Mouse;
import zoo.animal.Snake;
import zoo.animal.Tiger;
import zoo.animal.Trout;

public final class Demo {
    private Demo() {
    }

    public static void main(String[] args) {
        configureLogger(Zoo.class.getName(), Level.INFO);
        configureLogger(CommandManager.class.getName(), Level.FINE);

        Zoo zoo = new Zoo();
        zoo.addEnclosure(new Aquarium<Fish>("Grosses Aquarium", Fish.class));
        zoo.addEnclosure(new Terrarium<>("Wuesten-Terrarium", Snake.class));
        zoo.addEnclosure(new MammalHouse<>("Elefantenhaus", Elephant.class));
        zoo.addEnclosure(new MammalHouse<>("Primatenhaus", Gorilla.class));
        zoo.addEnclosure(new MammalHouse<>("Nagerhaus", Mouse.class));
        zoo.addEnclosure(new CatHouse<>("Loewenhaus", Lion.class));
        zoo.addEnclosure(new CatHouse<>("Savanne", Lion.class));
        zoo.addEnclosure(new CatHouse<>("Tigerhaus", Tiger.class));

        Lion simba = new Lion("Simba");
        zoo.addAnimal("Grosses Aquarium", new Trout("Toni"));
        zoo.addAnimal("Wuesten-Terrarium", new Snake("Sissi"));
        zoo.addAnimal("Elefantenhaus", new Elephant("Ella"));
        zoo.addAnimal("Primatenhaus", new Gorilla("Gina"));
        zoo.addAnimal("Nagerhaus", new Mouse("Mika"));
        zoo.addAnimal("Loewenhaus", simba);
        zoo.addAnimal("Tigerhaus", new Tiger("Tara"));
        zoo.addAnimal("Loewenhaus", new Eagle("Erna"));

        System.out.println(zoo.summary());
        System.out.println("Saeugetiere: " + zoo.getAllMammals());
        System.out.println("Suche Tara: " + zoo.findAnimalByName("Tara"));

        configureLogger(Zoo.class.getName(), Level.FINE);
        zoo.findEnclosureByName("Pinguinbecken");
        zoo.moveAnimal(simba, "Loewenhaus", "Savanne");
        zoo.removeAnimal("Savanne", simba);
        System.out.println("Sortiert: " + zoo.animalsSortedByName());

        demonstrateCommands();
    }

    private static void demonstrateCommands() {
        Enclosure<Mammal> mammals = new MammalHouse<>("Command-Saeuger", Mammal.class);
        CommandManager<Enclosure<Mammal>> mammalManager = new CommandManager<>();

        Lion leo = new Lion("Leo");
        Gorilla kiki = new Gorilla("Kiki");
        mammalManager.executeCommand(new AddAnimalCommand<>(leo), mammals);
        mammalManager.executeCommand(new AddAnimalCommand<>(kiki), mammals);
        mammalManager.executeCommand(new RemoveAnimalCommand<>(leo), mammals);
        mammalManager.undo(mammals);
        mammalManager.redo(mammals);
        mammalManager.redo(mammals);
        System.out.println("Command-Saeuger: " + mammals.getInhabitants());

        Enclosure<Lion> lions = new CatHouse<>("Command-Loewen", Lion.class);
        CommandManager<Enclosure<Lion>> lionManager = new CommandManager<>();
        lionManager.executeCommand(new AddAnimalCommand<>(new Lion("Nala")), lions);
        lionManager.undo(lions);
        lionManager.redo(lions);
        System.out.println("Command-Loewen: " + lions.getInhabitants());
    }

    private static void configureLogger(String loggerName, Level level) {
        Logger logger = Logger.getLogger(loggerName);
        logger.setUseParentHandlers(false);
        logger.setLevel(level);

        if (logger.getHandlers().length == 0) {
            ConsoleHandler handler = new ConsoleHandler();
            logger.addHandler(handler);
        }
        for (var handler : logger.getHandlers()) {
            handler.setLevel(level);
        }
    }
}