package com.dokbrawn.visprog;

public class Driver extends Human implements Runnable {
    public Driver(String name, int age, double x, double y, double speed) {
        super(name, age, x, y, speed, 0.0, 1.0, speed, 0.0, null);
    }

    @Override
    public void move(double dt) {
        // Прямолинейное движение вдоль X
        double x = getX() + getCurrentSpeed() * dt;
    }

    @Override
    public void run() {
        move(1.0);
    }
}
