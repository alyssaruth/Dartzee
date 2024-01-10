package dartzee.logging

import dartzee.utils.AwsUtils
import java.util.concurrent.Executors

const val ELASTICSEARCH_URL =
    "https://search-dartzee-nfqeufkxsx6cu7sybhm53dts7e.eu-west-2.es.amazonaws.com"
private const val INDEX_PATH = "dartzee"

object LoggerFactory {
    fun constructElasticsearchDestination(): LogDestinationElasticsearch {
        val poster =
            AwsUtils.readCredentials("AWS_LOGS")?.let {
                ElasticsearchPoster(it, ELASTICSEARCH_URL, INDEX_PATH)
            }
        return LogDestinationElasticsearch(poster, Executors.newScheduledThreadPool(1))
    }
}
