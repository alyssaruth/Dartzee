package dartzee.ai

import java.awt.Point

data class SimulationWrapper(val averageDart: Double,
                             val missPercent: Double,
                             val finishPercent: Double,
                             val treblePercent: Double,
                             val hmPointToCount: Map<Point, Int>)
