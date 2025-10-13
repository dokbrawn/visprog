package com.dokbrawn.visprog;

import java.util.Random;

public class Main {
    public static void main(String[] args) {
        int N = 3;
        double dt = 0.5;

        Human[] humans = new Human[N];
        Random rnd = new Random(123);

        for (int i=0; i<N; i++) {
            humans[i] = new Human(""Person_""+(i+1), 20+i, i, 0, 1, 1, 0.85, 1.2, 0.4, rnd);
        }

        Driver driver = new Driver(""Driver1"", 30, 0, 0, 1.5);

        Thread[] threads = new Thread[N+1];
        for (int i=0; i<N; i++) {
            int idx = i;
            threads[i] = new Thread(() -> humans[idx].move(dt));
        }
        threads[N] = new Thread(driver);

        for (Thread t: threads) t.start();
        for (Thread t: threads) {
            try { t.join(); } catch (InterruptedException e) { e.printStackTrace(); }
        }

        for (Human h: humans) {
            System.out.printf(""%s: x=%.2f y=%.2f speed=%.2f%n"",
                    h.toString(), h.getX(), h.getY(), h.getCurrentSpeed());
        }
        System.out.printf(""%s: x=%.2f y=%.2f speed=%.2f%n"",
                driver.toString(), driver.getX(), driver.getY(), driver.getCurrentSpeed());
    }
}
