package com.dokbrawn.visprog;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

public class Main {
    public static void main(String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("inheritance")) {
            runInheritanceDemo();
        } else {
            runDefaultSimulation();
        }
    }

    // --- старое поведение: та же симуляция, что был у тебя ранее ---
    private static void runDefaultSimulation() {
        int groupNumber = 5; // замените на ваш номер в списке
        int N = groupNumber;
        double simulationSeconds = 30.0;
        double dt = 0.5;

        Human[] people = new Human[N];
        Random rnd = new Random(12345);

        double alpha = 0.85;
        double vBar = 1.2;
        double sigma = 0.4;

        for (int i = 0; i < N; i++) {
            double x0 = i * 1.0;
            double y0 = 0.0;
            double initVx = vBar * Math.cos(i * 0.5);
            double initVy = vBar * Math.sin(i * 0.5);
            people[i] = new Human("Person_" + (i+1), 20 + i, x0, y0, initVx, initVy, alpha, vBar, sigma, rnd);
        }

        int steps = (int) Math.ceil(simulationSeconds / dt);
        String csvName = "trajectories.csv";

        try (PrintWriter pw = new PrintWriter(new FileWriter(csvName))) {
            pw.println("time,id,x,y,speed");
            double time = 0.0;
            for (int step = 0; step < steps; step++) {
                for (int i = 0; i < N; i++) {
                    people[i].move(dt);
                }
                time += dt;
                for (int i = 0; i < N; i++) {
                    Human h = people[i];
                    pw.printf("%.3f,%d,%.6f,%.6f,%.6f%n", time, i+1, h.getX(), h.getY(), h.getCurrentSpeed());
                }
            }
            System.out.println("Лог траекторий записан в " + csvName);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Симуляция завершена.");
    }

    // --- новый режим: наследование + параллельное движение ---
    private static void runInheritanceDemo() {
        Random rnd = new Random(12345);

        // Создаём 3 Human и 1 Driver
        Human[] humans = new Human[3];
        for (int i = 0; i < humans.length; i++) {
            humans[i] = new Human("Human_" + (i+1), 20 + i, i * 1.0, 0.0,
                    1.0, 0.0, 0.85, 1.2, 0.4, rnd);
        }

        Driver driver = new Driver("Driver_1", 30, 0.0, 1.0,
                2.0, 0.0, 0.85, 2.0, 0.0, rnd);

        final double dt = 0.5;
        final int steps = 60; // например 60 шагов (30 секунд при dt=0.5)

        Thread[] threads = new Thread[humans.length + 1];

        // Потоки для Human
        for (int i = 0; i < humans.length; i++) {
            final Human h = humans[i];
            threads[i] = new Thread(() -> {
                for (int t = 0; t < steps; t++) {
                    h.move(dt);
                    System.out.printf("t=%.2f | %s%n", t * dt, h);
                    try { Thread.sleep(50); } catch (InterruptedException ignored) {}
                }
            }, "human-" + (i+1));
        }

        // Поток для Driver
        threads[humans.length] = new Thread(() -> {
            for (int t = 0; t < steps; t++) {
                driver.move(dt);
                System.out.printf("t=%.2f | %s%n", t * dt, driver);
                try { Thread.sleep(50); } catch (InterruptedException ignored) {}
            }
        }, "driver");

        // Запуск всех потоков
        for (Thread th : threads) th.start();

        // Ожидание завершения
        for (Thread th : threads) {
            try { th.join(); } catch (InterruptedException ignored) {}
        }

        System.out.println("Inheritance demo finished.");
    }
}
