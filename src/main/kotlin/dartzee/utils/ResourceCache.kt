package dartzee.utils

import dartzee.bean.PlayerAvatar
import dartzee.core.obj.HashMapList
import dartzee.logging.CODE_LOADED_RESOURCES
import dartzee.logging.CODE_NO_STREAMS
import dartzee.logging.CODE_RESOURCE_LOAD_ERROR
import dartzee.utils.InjectedThings.logger
import java.awt.Font
import java.io.BufferedInputStream
import java.net.URL
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem
import javax.swing.ImageIcon

/**
 * Simple class housing statics for various image/sound resources So that these can be pre-loaded on
 * start-up, rather than causing lag the first time they're required.
 */
object ResourceCache {
    val DART_IMG = ImageIcon(javaClass.getResource("/dartImage.png"))

    val ICON_RESUME = ImageIcon(javaClass.getResource("/buttons/resume.png"))
    val ICON_PAUSE = ImageIcon(javaClass.getResource("/buttons/pause.png"))
    val ICON_STATS_LARGE = ImageIcon(javaClass.getResource("/buttons/stats_large.png"))
    val ICON_RESET = ImageIcon(javaClass.getResource("/buttons/Reset.png"))
    val ICON_CONFIRM = ImageIcon(javaClass.getResource("/buttons/Confirm.png"))
    val ICON_GRAPH_DECREASING = ImageIcon(javaClass.getResource("/icons/graph_decreasing.png"))
    val ICON_DARTBOARD = ImageIcon(javaClass.getResource("/icons/dartboard.png"))
    val ICON_CALCULATOR = ImageIcon(javaClass.getResource("/icons/calculator_large.png"))

    val AVATAR_UNSET = ImageIcon(PlayerAvatar::class.java.getResource("/avatars/Unset.png"))

    val URL_ACHIEVEMENT_LOCKED: URL? = javaClass.getResource("/achievements/locked.png")
    val URL_ACHIEVEMENT_BEST_FINISH: URL? = javaClass.getResource("/achievements/bestFinish.png")
    val URL_ACHIEVEMENT_BEST_SCORE: URL? = javaClass.getResource("/achievements/bestScore.png")
    val URL_ACHIEVEMENT_CHECKOUT_COMPLETENESS: URL? =
        javaClass.getResource("/achievements/checkoutCompleteness.png")
    val URL_ACHIEVEMENT_HIGHEST_BUST: URL? = javaClass.getResource("/achievements/bust.png")
    val URL_ACHIEVEMENT_POINTS_RISKED: URL? =
        javaClass.getResource("/achievements/pointsRisked.png")
    val URL_ACHIEVEMENT_X01_GAMES_WON: URL? = javaClass.getResource("/achievements/trophyX01.png")
    val URL_ACHIEVEMENT_GOLF_GAMES_WON: URL? = javaClass.getResource("/achievements/trophyGolf.png")
    val URL_ACHIEVEMENT_CLOCK_GAMES_WON: URL? =
        javaClass.getResource("/achievements/trophyClock.png")
    val URL_ACHIEVEMENT_X01_BEST_GAME: URL? = javaClass.getResource("/achievements/podiumX01.png")
    val URL_ACHIEVEMENT_GOLF_BEST_GAME: URL? = javaClass.getResource("/achievements/podiumGolf.png")
    val URL_ACHIEVEMENT_CLOCK_BEST_GAME: URL? =
        javaClass.getResource("/achievements/podiumClock.png")
    val URL_ACHIEVEMENT_CLOCK_BRUCEY_BONUSES: URL? =
        javaClass.getResource("/achievements/Bruce.png")
    val URL_ACHIEVEMENT_X01_SHANGHAI: URL? = javaClass.getResource("/achievements/shanghai.png")
    val URL_ACHIEVEMENT_X01_HOTEL_INSPECTOR: URL? =
        javaClass.getResource("/achievements/hotelInspector.png")
    val URL_ACHIEVEMENT_X01_SUCH_BAD_LUCK: URL? =
        javaClass.getResource("/achievements/suchBadLuck.png")
    val URL_ACHIEVEMENT_X01_BTBF: URL? = javaClass.getResource("/achievements/BTBF.png")
    val URL_ACHIEVEMENT_CLOCK_BEST_STREAK: URL? =
        javaClass.getResource("/achievements/likeClockwork.png")
    val URL_ACHIEVEMENT_X01_NO_MERCY: URL? = javaClass.getResource("/achievements/noMercy.png")
    val URL_ACHIEVEMENT_GOLF_COURSE_MASTER: URL? =
        javaClass.getResource("/achievements/courseMaster.png")
    val URL_ACHIEVEMENT_DARTZEE_GAMES_WON: URL? =
        javaClass.getResource("/achievements/trophyDartzee.png")
    val URL_ACHIEVEMENT_DARTZEE_BEST_GAME: URL? =
        javaClass.getResource("/achievements/podiumDartzee.png")
    val URL_ACHIEVEMENT_DARTZEE_FLAWLESS: URL? = javaClass.getResource("/achievements/flawless.png")
    val URL_ACHIEVEMENT_DARTZEE_UNDER_PRESSURE: URL? =
        javaClass.getResource("/achievements/underPressure.png")
    val URL_ACHIEVEMENT_DARTZEE_BINGO: URL? = javaClass.getResource("/achievements/bingo.png")
    val URL_ACHIEVEMENT_DARTZEE_HALVED: URL? = javaClass.getResource("/achievements/bust.png")
    val URL_ACHIEVEMENT_X01_CHUCKLEVISION: URL? =
        javaClass.getResource("/achievements/chucklevision.png")
    val URL_ACHIEVEMENT_GOLF_ONE_HIT_WONDER: URL? =
        javaClass.getResource("/achievements/oneHitWonder.png")
    val URL_ACHIEVEMENT_GOLF_IN_BOUNDS: URL? = javaClass.getResource("/achievements/inBounds.png")
    val URL_ACHIEVEMENT_X01_WINNERS: URL? = javaClass.getResource("/achievements/x01Winners.png")
    val URL_ACHIEVEMENT_GOLF_WINNERS: URL? = javaClass.getResource("/achievements/golfWinners.png")
    val URL_ACHIEVEMENT_DARTZEE_WINNERS: URL? =
        javaClass.getResource("/achievements/dartzeeWinners.png")
    val URL_ACHIEVEMENT_CLOCK_WINNERS: URL? =
        javaClass.getResource("/achievements/clockWinners.png")
    val URL_ACHIEVEMENT_X01_STYLISH_FINISH: URL? =
        javaClass.getResource("/achievements/stylishFinish.png")

    val BASE_FONT: Font =
        Font.createFont(Font.TRUETYPE_FONT, javaClass.getResourceAsStream("/trebuc.ttf"))

    private val wavPoolLock = Any()
    private val hmWavToInputStreams = HashMapList<String, AudioInputStream>()

    var isInitialised = false

    fun initialiseResources() {
        try {
            val wavFiles = InjectedThings.animations.flatMap { it.value.getAllSounds() }.toSet()

            for (wavName in wavFiles) {
                repeat(3) {
                    val ais = getAudioInputStream("$wavName.wav")
                    ais.mark(Integer.MAX_VALUE)

                    hmWavToInputStreams.putInList(wavName, ais)
                }
            }

            logger.info(CODE_LOADED_RESOURCES, "Finished loading ${wavFiles.size} resources")
            isInitialised = true
        } catch (e: Exception) {
            logger.error(CODE_RESOURCE_LOAD_ERROR, "Failed to load resources", e)
        }
    }

    fun borrowInputStream(wavName: String): AudioInputStream? {
        synchronized(wavPoolLock) {
            // Return if the wav file doesn't exist
            val streams = hmWavToInputStreams[wavName] ?: return null

            if (streams.isEmpty()) {
                logger.warn(
                    CODE_NO_STREAMS,
                    "No streams left for WAV [$wavName], will spawn another",
                )

                val ais = getAudioInputStream("$wavName.wav")
                ais.mark(Integer.MAX_VALUE)

                return ais
            }

            val ais = streams.removeAt(0)
            ais.reset()
            return ais
        }
    }

    private fun getAudioInputStream(wavFile: String): AudioInputStream {
        val inputStream = javaClass.getResourceAsStream("/wav/$wavFile")
        val bis = BufferedInputStream(inputStream)

        try {
            return AudioSystem.getAudioInputStream(bis)
        } catch (e: Exception) {
            throw Exception("Failed to load audio for resource $wavFile", e)
        }
    }

    fun returnInputStream(wavName: String, stream: AudioInputStream) {
        synchronized(wavPoolLock) { hmWavToInputStreams.putInList(wavName, stream) }
    }

    fun resetCache() {
        isInitialised = false
        hmWavToInputStreams.clear()
    }
}
