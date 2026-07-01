# Blatt 07

## Zoo

Das Projekt modelliert einen kleinen Zoo mit sealed Interfaces, Records, generischen Gehegen, Stream-Abfragen und `java.util.logging`.

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
- Bei `findEnclosureByName` wird `Optional<Enclosure<? extends Animal>>` genutzt, weil ein Gehege auch fehlen kann.

## Logging

- Ein Logger ist sinnvoller als `IO.println`, weil Ausgaben nach Level gefiltert, zentral konfiguriert und bei Bedarf abgeschaltet oder umgeleitet werden koennen.
- `INFO`: normale Methodenaufrufe mit wichtigen Parametern.
- `FINE`: Details nach erfolgreicher Ausfuehrung, z.B. Anzahl der Tiere.
- `WARNING`: Gehege oder Tier wurde nicht gefunden, oder ein Tier passt nicht in ein Gehege.
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
