package dartzee.utils

import dartzee.dartzee.DartzeeRoundResult
import dartzee.dartzee.DartzeeRuleDto
import dartzee.db.EntityName
import dartzee.`object`.Dart
import dartzee.`object`.DartboardSegment
import dartzee.`object`.SegmentType
import dartzee.utils.InjectedThings.mainDatabase
import java.awt.Color
import java.awt.Component

fun Component.setColoursForDartzeeResult(success: Boolean)
{
    if (success)
    {
        background = Color.GREEN
        foreground = DartsColour.getProportionalColourRedToGreen(1.0, 1, 0.5)
    }
    else
    {
        background = Color.RED
        foreground = DartsColour.getProportionalColourRedToGreen(0.0, 1, 0.5)
    }
}

fun factoryHighScoreResult(darts: List<Dart>): DartzeeRoundResult
{
    return DartzeeRoundResult(-1, success = true, score = sumScore(darts))
}

fun insertDartzeeRules(gameId: String, dartzeeDtos: List<DartzeeRuleDto>? = null, database: Database = mainDatabase)
{
    dartzeeDtos ?: return

    dartzeeDtos.forEachIndexed { ix, dto ->
        val dao = dto.toEntity(ix + 1, EntityName.Game, gameId, database)
        dao.saveToDatabase()
    }
}

fun getAllSegmentsForDartzee(): List<DartboardSegment>
{
    val segments = getAllNonMissSegments()
    return segments + DartboardSegment(SegmentType.MISS, 20)
}