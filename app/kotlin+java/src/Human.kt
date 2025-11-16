import kotlin.random.Random

open class Human(
    override val fullName: String,
    val age: Int,
    override var currentSpeed: Double,
    override var x: Double,
    override var y: Double
) : Movable {
    override fun move() {
        val dx = (Random.nextDouble() - 0.5) * currentSpeed
        val dy = (Random.nextDouble() - 0.5) * currentSpeed
        x += dx
        y += dy
        println("$fullName (Human) сейчас на (%.2f, %.2f)".format(x, y))
    }
}