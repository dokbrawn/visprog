fun main() {
    val humans = listOf(
        Human("Алексей", 30, 1.0, 0.0, 0.0),
        Human("Мария", 25, 1.2, 0.0, 0.0)
    )

    val driver = Driver("Сергей", 35, 2.5, 0.0, 0.0, 10)
    val allObjects = humans + driver
    val simulationTime = 10

    allObjects.forEach { obj ->
        Thread {
            for (timeStep in 1..simulationTime) {
                obj.move()
                Thread.sleep(100)
            }
        }.start()
    }
}