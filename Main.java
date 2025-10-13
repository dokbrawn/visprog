package com.dokbrawn.visprog;

import java.util.Random;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        Human h1 = new Human(""Alice"", 20, 0, 0, 1, 0.5, 0.85, 1.2, 0.4, new Random());
        Human h2 = new Human(""Bob"", 21, 1, 0, 1, 0.5, 0.85, 1.2, 0.4, new Random());
        Driver d1 = new Driver(""Charlie"", 25, 0, 0, 1, 0);

        List<Runnable> movers = new ArrayList<>();
        movers.add(h1::move);
        movers.add(h2::move);
        movers.add(d1);

        List<Thread> threads = new ArrayList<>();
        for(Runnable r : movers){
            Thread t = new Thread(r);
            threads.add(t);
            t.start();
        }

        for(Thread t : threads){
            t.join();
        }

        System.out.println(""Позиции после одного шага:"");
        System.out.printf(""Alice: (%.2f, %.2f)\n"", h1.getX(), h1.getY());
        System.out.printf(""Bob:   (%.2f, %.2f)\n"", h2.getX(), h2.getY());
        System.out.printf(""Charlie: (%.2f, %.2f)\n"", d1.getX(), d1.getY());
    }
}
