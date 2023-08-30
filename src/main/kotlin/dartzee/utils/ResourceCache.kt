package dartzee.utils

import dartzee.bean.PlayerAvatar
import dartzee.core.obj.HashMapList
import dartzee.core.util.DialogUtil
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
 * Simple class housing statics for various image/sound resources
 * So that these can be pre-loaded on start-up, rather than causing lag the first time they're required.
 */
object ResourceCache
{
    val DART_IMG = ImageIcon(javaClass.getResource("/dartImage.png"))

    val IMG_BRUCE = ImageIcon(javaClass.getResource("/horrific/forsyth.png"))
    val IMG_DEV = ImageIcon(javaClass.getResource("/horrific/dev.png"))
    val IMG_MITCHELL = ImageIcon(javaClass.getResource("/horrific/mitchell.png"))
    val IMG_SPENCER = ImageIcon(javaClass.getResource("/horrific/spencer.png"))
    val IMG_BASIL = ImageIcon(javaClass.getResource("/horrific/basil.png"))
    val IMG_CHUCKLE = ImageIcon(javaClass.getResource("/horrific/chuckle.png"))
    val ICON_RESUME = ImageIcon(javaClass.getResource("/buttons/resume.png"))
    val ICON_PAUSE = ImageIcon(javaClass.getResource("/buttons/pause.png"))
    val ICON_STATS_LARGE = ImageIcon(javaClass.getResource("/buttons/stats_large.png"))

    val AVATAR_UNSET = ImageIcon(PlayerAvatar::class.java.getResource("/avatars/Unset.png"))

    val URL_ACHIEVEMENT_LOCKED: URL? = javaClass.getResource("/achievements/locked.png")
    val URL_ACHIEVEMENT_BEST_FINISH: URL? = javaClass.getResource("/achievements/bestFinish.png")
    val URL_ACHIEVEMENT_BEST_SCORE: URL? = javaClass.getResource("/achievements/bestScore.png")
    val URL_ACHIEVEMENT_CHECKOUT_COMPLETENESS: URL? = javaClass.getResource("/achievements/checkoutCompleteness.png")
    val URL_ACHIEVEMENT_HIGHEST_BUST: URL? = javaClass.getResource("/achievements/bust.png")
    val URL_ACHIEVEMENT_POINTS_RISKED: URL? = javaClass.getResource("/achievements/pointsRisked.png")
    val URL_ACHIEVEMENT_X01_GAMES_WON: URL? = javaClass.getResource("/achievements/trophyX01.png")
    val URL_ACHIEVEMENT_GOLF_GAMES_WON: URL? = javaClass.getResource("/achievements/trophyGolf.png")
    val URL_ACHIEVEMENT_CLOCK_GAMES_WON: URL? = javaClass.getResource("/achievements/trophyClock.png")
    val URL_ACHIEVEMENT_X01_BEST_GAME: URL? = javaClass.getResource("/achievements/podiumX01.png")
    val URL_ACHIEVEMENT_GOLF_BEST_GAME: URL? = javaClass.getResource("/achievements/podiumGolf.png")
    val URL_ACHIEVEMENT_CLOCK_BEST_GAME: URL? = javaClass.getResource("/achievements/podiumClock.png")
    val URL_ACHIEVEMENT_CLOCK_BRUCEY_BONUSES: URL? = javaClass.getResource("/achievements/Bruce.png")
    val URL_ACHIEVEMENT_X01_SHANGHAI: URL? = javaClass.getResource("/achievements/shanghai.png")
    val URL_ACHIEVEMENT_X01_HOTEL_INSPECTOR: URL? = javaClass.getResource("/achievements/hotelInspector.png")
    val URL_ACHIEVEMENT_X01_SUCH_BAD_LUCK: URL? = javaClass.getResource("/achievements/suchBadLuck.png")
    val URL_ACHIEVEMENT_X01_BTBF: URL? = javaClass.getResource("/achievements/BTBF.png")
    val URL_ACHIEVEMENT_CLOCK_BEST_STREAK: URL? = javaClass.getResource("/achievements/likeClockwork.png")
    val URL_ACHIEVEMENT_X01_NO_MERCY: URL? = javaClass.getResource("/achievements/noMercy.png")
    val URL_ACHIEVEMENT_GOLF_COURSE_MASTER: URL? = javaClass.getResource("/achievements/courseMaster.png")
    val URL_ACHIEVEMENT_DARTZEE_GAMES_WON: URL? = javaClass.getResource("/achievements/trophyDartzee.png")
    val URL_ACHIEVEMENT_DARTZEE_BEST_GAME: URL? = javaClass.getResource("/achievements/podiumDartzee.png")
    val URL_ACHIEVEMENT_DARTZEE_FLAWLESS: URL? = javaClass.getResource("/achievements/flawless.png")
    val URL_ACHIEVEMENT_DARTZEE_UNDER_PRESSURE: URL? = javaClass.getResource("/achievements/underPressure.png")
    val URL_ACHIEVEMENT_DARTZEE_BINGO: URL? = javaClass.getResource("/achievements/bingo.png")
    val URL_ACHIEVEMENT_DARTZEE_HALVED: URL? = javaClass.getResource("/achievements/bust.png")
    val URL_ACHIEVEMENT_X01_CHUCKLEVISION: URL? = javaClass.getResource("/achievements/chucklevision.png")
    val URL_ACHIEVEMENT_GOLF_ONE_HIT_WONDER: URL? = javaClass.getResource("/achievements/oneHitWonder.png")
    val URL_ACHIEVEMENT_GOLF_IN_BOUNDS: URL? = javaClass.getResource("/achievements/inBounds.png")
    val URL_ACHIEVEMENT_X01_WINNERS: URL? = javaClass.getResource("/achievements/x01Winners.png")
    val URL_ACHIEVEMENT_GOLF_WINNERS: URL? = javaClass.getResource("/achievements/golfWinners.png")
    val URL_ACHIEVEMENT_DARTZEE_WINNERS: URL? = javaClass.getResource("/achievements/dartzeeWinners.png")
    val URL_ACHIEVEMENT_CLOCK_WINNERS: URL? = javaClass.getResource("/achievements/clockWinners.png")
    val URL_ACHIEVEMENT_X01_STYLISH_FINISH: URL? = javaClass.getResource("/achievements/stylishFinish.png")

    val BASE_FONT: Font = Font.createFont(Font.TRUETYPE_FONT, javaClass.getResourceAsStream("/trebuc.ttf"))

    private val wavPoolLock = Any()
    private val hmWavToInputStreams = HashMapList<String, AudioInputStream>()

    var isInitialised = false

    private fun getWavFiles(): List<String>
    {
        return listOf("60.wav", "100.wav", "140.wav", "180.wav", "badmiss1.wav", "badmiss2.wav", "badmiss3.wav", "badmiss4.wav",
                "basil1.wav", "basil2.wav", "basil3.wav", "basil4.wav", "bull.wav", "damage.wav", "forsyth1.wav", "forsyth2.wav",
                "forsyth3.wav", "forsyth4.wav", "fourTrimmed.wav", "badLuck1.wav", "badLuck2.wav", "chucklevision1.wav", "chucklevision2.wav", "chucklevision3.wav")
        }

    fun initialiseResources()
    {
        try
        {
            DialogUtil.showLoadingDialogOLD("Loading resources...")

            val wavFiles = getWavFiles()

            for (wavFile in wavFiles)
            {
                repeat(3)
                {
                    val ais = getAudioInputStream(wavFile)
                    ais.mark(Integer.MAX_VALUE)

                    hmWavToInputStreams.putInList(wavFile, ais)
                }
            }

            logger.info(CODE_LOADED_RESOURCES, "Finished loading ${wavFiles.size} resources")
            isInitialised = true
        }
        catch (e: Exception)
        {
            logger.error(CODE_RESOURCE_LOAD_ERROR, "Failed to load resources", e)
        }
        finally
        {
            DialogUtil.dismissLoadingDialogOLD()
        }
    }

    fun borrowInputStream(wavName: String): AudioInputStream?
    {
        synchronized(wavPoolLock)
        {
            val wavFile = "$wavName.wav"

            //Return if the wav file doesn't exist
            val streams = hmWavToInputStreams[wavFile] ?: return null

            if (streams.isEmpty())
            {
                logger.warn(CODE_NO_STREAMS, "No streams left for WAV [$wavName], will spawn another")

                val ais = getAudioInputStream(wavFile)
                ais.mark(Integer.MAX_VALUE)

                return ais
            }

            val ais = streams.removeAt(0)
            ais.reset()
            return ais
        }
    }

    private fun getAudioInputStream(wavFile: String): AudioInputStream
    {
        val inputStream = javaClass.getResourceAsStream("/wav/$wavFile")
        val bis = BufferedInputStream(inputStream)

        return AudioSystem.getAudioInputStream(bis)
    }

    fun returnInputStream(wavName: String, stream: AudioInputStream)
    {
        synchronized (wavPoolLock)
        {
            hmWavToInputStreams.putInList("$wavName.wav", stream)
        }
    }

    fun resetCache()
    {
        isInitialised = false
        hmWavToInputStreams.clear()
    }
}
