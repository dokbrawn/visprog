package com.dokbrawn.visprog;

public interface Movable {
    void move(double dt);
    double getX();
    double getY();
    double getCurrentSpeed();
}
