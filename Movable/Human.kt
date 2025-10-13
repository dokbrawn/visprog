package com.dokbrawn.visprog;

import java.util.Random;

public class Human implements Movable {
    private String name;
    private int age;
    private double x, y;
    private double vx, vy;
    private double currentSpeed;
    private double alpha, vBar, sigma;
    private Random rnd;

    public Human(String name, int age, double x, double y,
                 double vx, double vy, double alpha, double vBar,
                 double sigma, Random rnd) {
        this.name = name;
        this.age = age;
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.alpha = alpha;
        this.vBar = vBar;
        this.sigma = sigma;
        this.rnd = rnd;
        this.currentSpeed = Math.sqrt(vx*vx + vy*vy);
    }

    @Override
    public void move(double dt) {
        vx = alpha * vx + (1-alpha) * vBar + sigma * rnd.nextGaussian();
        vy = alpha * vy + (1-alpha) * vBar + sigma * rnd.nextGaussian();
        x += vx * dt;
        y += vy * dt;
        currentSpeed = Math.sqrt(vx*vx + vy*vy);
    }

    @Override
    public double getX() { return x; }
    @Override
    public double getY() { return y; }
    @Override
    public double getCurrentSpeed() { return currentSpeed; }

    @Override
    public String toString() { return name; }
}
