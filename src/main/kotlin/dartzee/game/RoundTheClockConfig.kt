package dartzee.game

import com.fasterxml.jackson.module.kotlin.readValue
import dartzee.core.util.jsonMapper

enum class ClockType
{
    Standard,
    Doubles,
    Trebles
}

data class RoundTheClockConfig(val clockType: ClockType, val inOrder: Boolean)
{
    fun toJson(): String = jsonMapper().writeValueAsString(this)

    companion object
    {
        fun fromJson(json: String) = jsonMapper().readValue<RoundTheClockConfig>(json)
    }
}
