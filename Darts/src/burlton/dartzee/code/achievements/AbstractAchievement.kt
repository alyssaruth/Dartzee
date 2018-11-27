package burlton.dartzee.code.achievements

abstract class AbstractAchievement
{
    abstract val name : String

    abstract fun runConversion()

    override fun toString(): String
    {
        return name
    }
}
