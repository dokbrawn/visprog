# Практическое задание: 'Ходячий' — Gauss–Markov Mobility Model

## Описание
Моделирование движения группы людей (Human) с помощью Gauss–Markov Mobility Model.

## Классы
- Human: name, age, x, y, vx, vy, currentSpeed, alpha, vBar, sigma
- Main: создает массив Human и запускает симуляцию

## Модель движения
v_x(t + Δt) = α·v_x(t) + (1-α)·v̄ + σ·N(0,1)
v_y(t + Δt) = α·v_y(t) + (1-α)·v̄ + σ·N(0,1)
x(t + Δt) = x(t) + v_x·Δt
y(t + Δt) = y(t) + v_y·Δt

## Запуск
javac -d out src/main/java/com/dokbrawn/visprog/*.java
java -cp out com.dokbrawn.visprog.Main
