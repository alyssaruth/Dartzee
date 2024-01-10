package dartzee.helper

import dartzee.`object`.Dart

private val gameOneRounds =
    makeX01Rounds(
        301,
        listOf(Dart(20, 3), Dart(20, 2), Dart(20, 1)), // 120 (181)
        listOf(Dart(20, 1), Dart(5, 1), Dart(20, 1)), // 45   (136)
        listOf(Dart(20, 1), Dart(20, 1), Dart(20, 1)), // 60   (76)
        listOf(Dart(12, 3), Dart(20, 2)) // 76 (0)
    )
val GAME_WRAPPER_301_1 =
    makeGameWrapper(gameParams = "301", finalScore = 11, localId = 1L).also {
        gameOneRounds.flatten().forEach(it::addDart)
    }

private val gameTwoRounds =
    makeX01Rounds(
        301,
        listOf(Dart(5, 1), Dart(20, 1), Dart(20, 1)), // 45 (256)
        listOf(Dart(12, 3), Dart(20, 1), Dart(4, 1)), // 60 (196)
        listOf(Dart(19, 1), Dart(19, 1), Dart(19, 1)), // 57 (139)
        listOf(Dart(1, 0), Dart(1, 1), Dart(1, 0)), // 1     (138)
        listOf(Dart(20, 1), Dart(20, 1), Dart(18, 1)), // 58  (80)
        listOf(Dart(20, 2), Dart(20, 2)) // 80 (0)
    )
val GAME_WRAPPER_301_2 =
    makeGameWrapper(gameParams = "301", finalScore = 17, localId = 2L).also {
        gameTwoRounds.flatten().forEach(it::addDart)
    }
