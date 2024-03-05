package dartzee.game

import com.fasterxml.jackson.module.kotlin.readValue
import dartzee.core.util.jsonMapper

val X01_PARTY_CONFIG = X01Config(301, FinishType.Any)

enum class FinishType {
    Any,
    Doubles
}

data class X01Config(val target: Int, val finishType: FinishType) {
    fun toJson(): String = jsonMapper().writeValueAsString(this)

    fun description() = "$target${finishTypeDesc()}"

    private fun finishTypeDesc() = if (finishType == FinishType.Any) " (relaxed finish)" else ""

    companion object {
        fun fromJson(json: String) = jsonMapper().readValue<X01Config>(json)
    }
}
