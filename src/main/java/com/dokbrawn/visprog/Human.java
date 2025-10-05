package com.dokbrawn.visprog;

import java.util.Random;

public class Human {
    private String fullName;
    private int age;
    private double currentSpeed;
    private double x, y;
    private double vx, vy;

    private final double alpha;
    private final double vBar;
    private final double sigma;
    private final Random rnd;

    public Human(String fullName, int age, double x0, double y0,
                 double initialVx, double initialVy,
                 double alpha, double vBar, double sigma, Random rnd) {
        this.fullName = fullName;
        this.age = age;
        this.x = x0;
        this.y = y0;
        this.vx = initialVx;
        this.vy = initialVy;
        this.currentSpeed = Math.hypot(vx, vy);
        this.alpha = alpha;
        this.vBar = vBar;
        this.sigma = sigma;
        this.rnd = (rnd == null) ? new Random() : rnd;
    }

    public String getFullName() { return fullName; }
    public int getAge() { return age; }
    public double getCurrentSpeed() { return currentSpeed; }
    public double getX() { return x; }
    public double getY() { return y; }

    public void move(double dt) {
        double speed = Math.hypot(vx, vy);
        double ux, uy;
        if (speed < 1e-8) {
            double theta = 2 * Math.PI * rnd.nextDouble();
            ux = Math.cos(theta);
            uy = Math.sin(theta);
        } else {
            ux = vx / speed;
            uy = vy / speed;
        }

        double noiseFactor = Math.sqrt(Math.max(0.0, 1 - alpha*alpha)) * sigma;
        double gaussianX = rnd.nextGaussian();
        double gaussianY = rnd.nextGaussian();

        double newVx = alpha * vx + (1 - alpha) * vBar * ux + noiseFactor * gaussianX;
        double newVy = alpha * vy + (1 - alpha) * vBar * uy + noiseFactor * gaussianY;

        vx = newVx;
        vy = newVy;
        currentSpeed = Math.hypot(vx, vy);

        x += vx * dt;
        y += vy * dt;
    }

    @Override
    public String toString() {
        return String.format("%s (age %d) � pos=(%.3f, %.3f) speed=%.3f", fullName, age, x, y, currentSpeed);
    }
}
