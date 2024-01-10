package dartzee.game

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.module.kotlin.readValue
import dartzee.core.util.jsonMapper

enum class ClockType {
    Standard,
    Doubles,
    Trebles
}

@JsonIgnoreProperties("description", "orderStr")
data class RoundTheClockConfig(val clockType: ClockType, val inOrder: Boolean) {
    fun toJson(): String = jsonMapper().writeValueAsString(this)

    fun getDescription() = "$clockType - ${getOrderStr()}"

    fun getOrderStr() = if (inOrder) "in order" else "any order"

    companion object {
        fun fromJson(json: String) = jsonMapper().readValue<RoundTheClockConfig>(json)
    }
}
