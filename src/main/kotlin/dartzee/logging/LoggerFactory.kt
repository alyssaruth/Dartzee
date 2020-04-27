package dartzee.logging

import com.amazonaws.auth.BasicAWSCredentials
import dartzee.screen.ScreenCache
import dartzee.utils.InjectedThings
import java.nio.charset.Charset
import java.util.*

const val ELASTICSEARCH_URL = "https://search-dartzee-nfqeufkxsx6cu7sybhm53dts7e.eu-west-2.es.amazonaws.com"
private const val INDEX_PATH = "dartzee"

object LoggerFactory
{
    fun constructLogger(): Logger
    {
        val poster = readCredentials()?.let { ElasticsearchPoster(it, ELASTICSEARCH_URL, INDEX_PATH) }
        val esDestination = LogDestinationElasticsearch(poster)

        return Logger(listOf(ScreenCache.loggingConsole, LogDestinationSystemOut(), esDestination))
    }

    fun readCredentials(): BasicAWSCredentials?
    {
        try
        {
            val awsCredentials = javaClass.getResource("/aws").readText()
            val decoded = Base64.getDecoder().decode(awsCredentials).toString(Charset.forName("UTF-8"))
            val lines = decoded.lines()
            return BasicAWSCredentials(lines[0], lines[1])
        }
        catch (t: Throwable)
        {
            InjectedThings.logger.error(CODE_ELASTICSEARCH_ERROR, "Failed to read in AWS credentials", t)
            return null
        }
    }
}