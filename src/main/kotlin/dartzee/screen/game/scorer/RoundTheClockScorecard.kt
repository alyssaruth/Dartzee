package dartzee.screen.game.scorer

import dartzee.core.bean.ScrollTable
import dartzee.core.util.TableUtil
import dartzee.game.state.ClockPlayerState
import java.awt.Dimension

class RoundTheClockScorecard : ScrollTable() {
    private val tm = TableUtil.DefaultModel()

    init {
        model = tm
        (0..3).forEach { _ -> model.addColumn("") }
        (0..3).forEach { ix -> getColumn(ix).cellRenderer = RoundTheClockScorecardRenderer() }
        setRowHeight(25)

        preferredSize = Dimension(100, 140)
        setShowRowCount(false)
    }

    fun stateChanged(state: ClockPlayerState, paused: Boolean) {
        tm.clear()

        val results = (1..20).map { makeClockResult(it, state, paused) }
        results.chunked(4).forEach(::addRow)
    }

    private fun makeClockResult(
        target: Int,
        state: ClockPlayerState,
        paused: Boolean,
    ): ClockResult {
        val hit = state.hasHitTarget(target)
        val isCurrentTarget = state.findCurrentTarget() == target && state.isActive && !paused
        return ClockResult(target, hit, isCurrentTarget)
    }
}

data class ClockResult(val value: Int, val hit: Boolean, val isCurrentTarget: Boolean)
