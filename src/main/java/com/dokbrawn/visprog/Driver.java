package com.dokbrawn.visprog;

public class Driver extends Human {

    public Driver(String fullName, int age, double x0, double y0,
                  double initialVx, double initialVy,
                  double alpha, double vBar, double sigma, java.util.Random rnd) {
        super(fullName, age, x0, y0, initialVx, initialVy, alpha, vBar, sigma, rnd);
    }

    @Override
    public void move(double dt) {
        // Прямолинейное движение (vx и vy остаются постоянными)
        double newX = getX() + getCurrentSpeed() * dt;
        double newY = getY(); // движение только по x
        setX(newX);
        setY(newY);
    }
}
