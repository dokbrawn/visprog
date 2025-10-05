package com.dokbrawn.visprog;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

public class Main {
    public static void main(String[] args) {
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
            // Заголовок: time, id, x, y, vx, vy, speed
            pw.println("time,id,x,y,speed");
            double time = 0.0;
            for (int step = 0; step < steps; step++) {
                for (int i = 0; i < N; i++) {
                    people[i].move(dt);
                }
                time += dt;
                // Запись состояния всех агентов
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
}
