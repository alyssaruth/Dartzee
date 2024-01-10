package dartzee.utils

import dartzee.core.util.DialogUtil
import dartzee.dartzee.DartzeeRoundResult
import dartzee.dartzee.DartzeeRuleDto
import dartzee.db.DartzeeRuleEntity
import dartzee.db.DartzeeTemplateEntity
import dartzee.db.EntityName
import dartzee.db.GameEntity
import dartzee.game.GameType
import dartzee.`object`.Dart
import dartzee.`object`.DartboardSegment
import dartzee.`object`.SegmentType
import dartzee.utils.InjectedThings.mainDatabase
import java.awt.Color
import java.awt.Component
import javax.swing.JOptionPane

fun Component.setColoursForDartzeeResult(success: Boolean) {
    if (success) {
        background = Color.GREEN
        foreground = DartsColour.getProportionalColourRedToGreen(1.0, 1, 0.5)
    } else {
        background = Color.RED
        foreground = DartsColour.getProportionalColourRedToGreen(0.0, 1, 0.5)
    }
}

fun factoryHighScoreResult(darts: List<Dart>) =
    DartzeeRoundResult(-1, success = true, score = sumScore(darts))

fun insertDartzeeRules(
    gameId: String,
    dartzeeDtos: List<DartzeeRuleDto>? = null,
    database: Database = mainDatabase
) {
    dartzeeDtos ?: return

    dartzeeDtos.forEachIndexed { ix, dto ->
        val dao = dto.toEntity(ix + 1, EntityName.Game, gameId, database)
        dao.saveToDatabase()
    }
}

fun getAllSegmentsForDartzee(): List<DartboardSegment> {
    val segments = getAllNonMissSegments()
    return segments + DartboardSegment(SegmentType.MISS, 20)
}

fun saveDartzeeTemplate(name: String, dtos: List<DartzeeRuleDto>): DartzeeTemplateEntity {
    val template = DartzeeTemplateEntity.factoryAndSave(name)
    dtos.forEachIndexed { ix, rule ->
        val entity = rule.toEntity(ix + 1, EntityName.DartzeeTemplate, template.rowId)
        entity.saveToDatabase()
    }

    return template
}

fun generateDartzeeTemplateFromGame(
    game: GameEntity,
    dtos: List<DartzeeRuleDto>
): DartzeeTemplateEntity? {
    val templateName =
        DialogUtil.showInput<String>("Template Name", "Please enter a name for the template")
            ?: return null

    val template = saveDartzeeTemplate(templateName, dtos)
    game.gameParams = template.rowId
    game.saveToDatabase()

    DialogUtil.showInfoOLD("Template '$templateName' successfully created.")

    return template
}

fun deleteDartzeeTemplate(template: DartzeeTemplateEntity, gameCount: Int): Boolean {
    val message =
        when (gameCount) {
            0 -> "Are you sure you want to delete the ${template.name} Template?"
            else ->
                "You have played $gameCount games using the ${template.name} Template." +
                    "\n\nThese will become custom games if you delete it. Are you sure you want to continue?"
        }

    val ans = DialogUtil.showQuestionOLD(message)
    if (ans != JOptionPane.YES_OPTION) {
        return false
    }

    template.deleteFromDatabase()
    DartzeeRuleEntity().deleteForTemplate(template.rowId)

    val gameSql =
        "UPDATE Game SET GameParams = '' WHERE GameType = '${GameType.DARTZEE}' AND GameParams = '${template.rowId}'"
    mainDatabase.executeUpdate(gameSql)
    return true
}
