package dartzee.helper

import dartzee.game.ClockType
import dartzee.game.RoundTheClockConfig
import dartzee.`object`.Dart

val inOrderGameDarts =
    makeClockRounds(
        true,
        Dart(1, 1),
        Dart(2, 1),
        Dart(3, 0),
        Dart(3, 1),
        Dart(18, 1),
        Dart(4, 1),
        Dart(5, 0),
        Dart(5, 2),
        Dart(6, 1),
        Dart(7, 3),
        Dart(8, 1),
        Dart(9, 3),
        Dart(10, 1),
        Dart(14, 1),
        Dart(9, 1),
        Dart(11, 1),
        Dart(12, 1),
        Dart(13, 1),
        Dart(11, 1),
        Dart(14, 1),
        Dart(15, 3),
        Dart(16, 0),
        Dart(16, 1),
        Dart(3, 1),
        Dart(19, 1),
        Dart(17, 1),
        Dart(18, 2),
        Dart(19, 1),
        Dart(20, 3)
    )

val GAME_WRAPPER_RTC_IN_ORDER =
    makeGameWrapper(
            gameParams = RoundTheClockConfig(ClockType.Standard, true).toJson(),
            finalScore = inOrderGameDarts.size
        )
        .also { inOrderGameDarts.forEach(it::addDart) }

val inOrderGameDarts2 =
    makeClockRounds(
        true,
        Dart(20, 1),
        Dart(18, 1),
        Dart(5, 1),
        Dart(1, 0),
        Dart(1, 3),
        Dart(2, 1),
        Dart(3, 0),
        Dart(19, 3),
        Dart(17, 1),
        Dart(3, 1),
        Dart(4, 1),
        Dart(5, 1),
        Dart(6, 1),
        Dart(7, 0),
        Dart(7, 1),
        Dart(8, 0),
        Dart(8, 1),
        Dart(9, 1),
        Dart(10, 1),
        Dart(14, 1),
        Dart(11, 1),
        Dart(12, 2),
        Dart(13, 3),
        Dart(11, 1),
        Dart(14, 1),
        Dart(15, 3),
        Dart(16, 1),
        Dart(17, 1),
        Dart(18, 0),
        Dart(18, 0),
        Dart(18, 1),
        Dart(19, 1),
        Dart(20, 0),
        Dart(20, 2)
    )

val GAME_WRAPPER_RTC_IN_ORDER_2 =
    makeGameWrapper(
            gameParams = RoundTheClockConfig(ClockType.Standard, true).toJson(),
            finalScore = inOrderGameDarts2.size
        )
        .also { inOrderGameDarts2.forEach(it::addDart) }

val outOfOrderGameDarts =
    makeClockRounds(
        false,
        Dart(14, 1),
        Dart(11, 1),
        Dart(9, 1),
        Dart(14, 1),
        Dart(12, 1),
        Dart(20, 1),
        Dart(5, 1),
        Dart(1, 1),
        Dart(18, 1),
        Dart(2, 1),
        Dart(3, 1),
        Dart(7, 3),
        Dart(13, 1),
        Dart(12, 1),
        Dart(16, 1),
        Dart(4, 1),
        Dart(6, 1),
        Dart(8, 3),
        Dart(10, 2),
        Dart(17, 1),
        Dart(2, 1),
        Dart(15, 1),
        Dart(19, 1)
    )

val GAME_WRAPPER_RTC_OUT_OF_ORDER =
    makeGameWrapper(
            gameParams = RoundTheClockConfig(ClockType.Standard, false).toJson(),
            finalScore = 23
        )
        .also { outOfOrderGameDarts.forEach(it::addDart) }
