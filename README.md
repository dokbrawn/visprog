# Практическое задание: Интерфейсы

## Описание
- Создан интерфейс Movable с методами движения и доступа к координатам и скорости.
- Классы Human и Driver реализуют этот интерфейс.
- Проведён рефакторинг: каждый класс/интерфейс находится в отдельном файле.

## Файлы
- Movable.java — интерфейс с методами move(), getX(), getY(), getCurrentSpeed().
- Human.java — движение по модели Gauss–Markov.
- Driver.java — прямолинейное движение, реализует Runnable.
- Main.java — демонстрация параллельного движения Human и Driver с использованием потоков.

## Запуск
\\\ash
javac -d out src/main/java/com/dokbrawn/visprog/*.java
java -cp out com.dokbrawn.visprog.Main
\\\

## Принцип работы
- Human движется согласно Gauss–Markov.
- Driver движется прямолинейно.
- Параллельное движение через Thread.
