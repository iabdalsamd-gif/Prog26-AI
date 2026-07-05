# Blatt 07 und 08

## Zoo

Das Projekt modelliert einen kleinen Zoo mit sealed Interfaces, Records, generischen Gehegen, Stream-Abfragen, Logging, Optional, Commands und einem einfachen Result-Fehlermodell.

- `Animal` ist die sealed Oberklasse fuer `Mammal`, `Bird`, `Fish` und `Reptile`.
- `Primate`, `Rodent` und `Cat` sind spezialisierte sealed Unterfamilien von `Mammal`.
- Konkrete Tiere sind Records, z.B. `Lion`, `Tiger`, `Trout`, `Snake`.
- `Enclosure<T extends Animal>` nutzt intern ein `LinkedHashSet<T>`. So kann dasselbe Tier nicht doppelt im selben Gehege vorkommen. Die Einfuegereihenfolge bleibt trotzdem erhalten.
- `Zoo` verwaltet `List<Enclosure<? extends Animal>>`, weil ein Zoo unterschiedliche Gehegetypen enthalten soll.

## Generics

- Generics verhindern falsche Tier-Gehege-Kombinationen bereits beim Kompilieren.
- `Aquarium<Fish>` kann Fische aufnehmen, aber keinen `Eagle`.
- `CatHouse<Lion>` ist auf Loewen festgelegt. Ein `Tiger` passt dort nicht in die Methode `add(Lion animal)`.
- `T extends Animal` verhindert ausserdem Gehege wie `Enclosure<String>`.
- Bei den Commands ist `AddAnimalCommand<Lion>` mit `Enclosure<Mammal>` verwendbar, weil ein Saeugetiergehege Loewen aufnehmen kann. Ein `AddAnimalCommand<Shark>` passt dort dagegen nicht.

## Optional

- `Enclosure<T>.findAnimalByName` gibt `Optional<T>` zurueck, weil in einem konkreten Gehege auch der konkrete Tier-Typ `T` bekannt ist.
- `Zoo.findAnimalByName` gibt `Optional<Animal>` zurueck, weil der Zoo verschiedene Gehegetypen enthaelt und das gefundene Tier allgemein nur als `Animal` beschrieben werden kann.
- `Optional` macht den Fall "nicht gefunden" sichtbar und vermeidet `null` als Rueckgabewert.

## Command-Pattern

- `Command<T>` beschreibt eine Aktion auf einem Zielobjekt `T` mit `execute`, `undo` und `description`.
- `AddAnimalCommand<T extends Animal>` und `RemoveAnimalCommand<T extends Animal>` arbeiten auf `Enclosure<? super T>`. Das folgt dem PECS-Prinzip: Das Gehege konsumiert Tiere vom Typ `T`, deshalb wird `super` verwendet.
- `CommandManager<T>` verwaltet einen Undo- und einen Redo-Stack. Erfolgreich ausgefuehrte Commands landen auf dem Undo-Stack. Ein neues Command leert den Redo-Stack.
- In der Demo werden Tiere per Command aufgenommen, entfernt, rueckgaengig gemacht und wiederholt.

## Result und Fehler

- `Result<E, R>` ist als sealed Interface mit `Success` und `Failure` umgesetzt.
- Typische Fehler stehen in `ZooError`, z.B. `ANIMAL_ALREADY_PRESENT`, `ANIMAL_NOT_FOUND`, `UNDO_STACK_EMPTY` und `REDO_STACK_EMPTY`.
- Die Commands entscheiden nur fachlich ueber Erfolg oder Fehler. Das Logging liegt zentral im `CommandManager`.
- Der `CommandManager` wertet `Result` mit Pattern Matching aus und loggt erfolgreiche Aktionen auf `FINE`, fehlgeschlagene oder nicht moegliche Aktionen auf `WARNING`.

## Logging

- Ein Logger ist sinnvoller als `IO.println`, weil Ausgaben nach Level gefiltert, zentral konfiguriert und bei Bedarf abgeschaltet oder umgeleitet werden koennen.
- `INFO`: normale Methodenaufrufe mit wichtigen Parametern.
- `FINE`: Details nach erfolgreicher Ausfuehrung, z.B. Anzahl der Tiere oder Stack-Groessen.
- `WARNING`: Gehege oder Tier wurde nicht gefunden, ein Command scheitert oder Undo/Redo ist nicht moeglich.
- `SEVERE`: interne Inkonsistenzen.

## Streams

- Streams sind besonders hilfreich fuer Abfragen ueber alle Gehege hinweg, z.B. mit `flatMap` von Gehegen auf Tiere.
- `filter`, `map`, `toList` und `Collectors.groupingBy` machen Such- und Zaehloperationen kompakt.
- Unuebersichtlich werden Streams, wenn neben der Abfrage auch viel Zustand geaendert wird. Bei `moveAnimal` sind einzelne Schritte lesbarer als eine lange Stream-Kette.

## Ausfuehren

```powershell
.\gradlew build
.\gradlew run
```

In IntelliJ kann der Ordner als Gradle-Projekt geoeffnet werden. Die Startklasse ist `zoo.Demo`.