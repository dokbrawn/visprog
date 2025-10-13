package com.dokbrawn.visprog;

public class Driver extends Human implements Runnable {
    private double speed;

    public Driver(String name, int age, double x, double y, double vx, double vy) {
        super(name, age, x, y, vx, vy, 1.0, 1.0, 0.0, null);
        this.speed = Math.sqrt(vx*vx + vy*vy);
    }

    @Override
    public void move(double dt) {
        // прямолинейное движение
        setX(getX() + speed*dt);
        setY(getY());
    }

    @Override
    public void run() {
        move(0.5); // шаг времени можно менять
    }
}
