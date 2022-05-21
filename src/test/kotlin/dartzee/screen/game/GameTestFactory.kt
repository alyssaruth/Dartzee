package dartzee.screen.game

import dartzee.db.ParticipantEntity
import dartzee.db.PlayerEntity
import dartzee.game.state.SingleParticipant
import dartzee.helper.insertParticipant

fun makeSingleParticipant(player: PlayerEntity) = makeSingleParticipant(insertParticipant(playerId = player.rowId))
fun makeSingleParticipant(pt: ParticipantEntity = insertParticipant()) = SingleParticipant(pt)