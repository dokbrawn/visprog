# Практическое задание: Интерфейсы — Gauss–Markov Mobility Model

## Описание

В рамках задания реализован интерфейс `Movable`, который определяет необходимые свойства и методы для движения объектов. Все классы, связанные с движением, теперь реализуют этот интерфейс, что обеспечивает единообразие и расширяемость кода.


## Описание файлов

### `Movable.java`

Интерфейс, определяющий основные свойства и методы для объектов, которые могут двигаться.

```java
public interface Movable {
    double getX();
    double getY();
    double getVx();
    double getVy();
    void move();
}
````

### `Human.java`

Класс, представляющий человека, реализующий интерфейс `Movable`.

```java
public class Human implements Movable {
    private double x, y, vx, vy;

    @Override
    public void move() {
        // Реализация метода движения
    }

    // Геттеры и сеттеры
}
```

### `Main.java`

Основной класс, запускающий симуляцию движения группы людей.

```java
public class Main {
    public static void main(String[] args) {
        // Инициализация и запуск симуляции
    }
}
```

## Запуск проекта

1. Скомпилируйте проект:

   ```bash
   javac -d out src/main/java/com/dokbrawn/visprog/*.java
   ```

2. Запустите симуляцию:

   ```bash
   java -cp out com.dokbrawn.visprog.Main
   ```
 
```
