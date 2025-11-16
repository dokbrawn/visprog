class Driver(
    fullName: String,
    age: Int,
    currentSpeed: Double,
    x: Double,
    y: Double,
    val drivingExperience: Int
) : Human(fullName, age, currentSpeed, x, y) {

    override fun move() {
        val dx = currentSpeed
        x += dx
        println("$fullName (Driver) сейчас на (%.2f, %.2f)".format(x, y))
    }
}