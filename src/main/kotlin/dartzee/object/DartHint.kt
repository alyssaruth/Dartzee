package dartzee.`object`

class DartHint(score: Int, multiplier: Int): Dart(score, multiplier)

fun factoryDartHintFromString(dartStr: String): DartHint
{
    return if (dartStr.startsWith("D"))
    {
        val score = dartStr.replace("D", "").toInt()
        DartHint(score, 2)
    }
    else if (dartStr.startsWith("T"))
    {
        val score = dartStr.replace("T", "").toInt()
        DartHint(score, 3)
    }
    else
    {
        val score = dartStr.toInt()
        DartHint(score, 1)
    }
}