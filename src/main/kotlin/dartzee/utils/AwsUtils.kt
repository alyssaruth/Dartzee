package dartzee.utils

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import java.nio.charset.Charset
import java.util.*

object AwsUtils
{
    fun readCredentials(resourceName: String) =
        try
        {
            val awsCredentials = getAwsCredentialsStr(resourceName)
            val decoded = Base64.getDecoder().decode(awsCredentials).toString(Charset.forName("UTF-8"))
            val lines = decoded.lines()
            BasicAWSCredentials(lines[0], lines[1])
        }
        catch (t: Throwable)
        {
            println("Failed to read in AWS credentials: $t")
            t.printStackTrace()
            null
        }

    private fun getAwsCredentialsStr(resourceName: String): String
    {
        val rsrc = javaClass.getResource("/$resourceName")
        return rsrc?.readText() ?: System.getenv(resourceName)
    }

    fun makeS3Client(): AmazonS3
    {
        val credentials = readCredentials("AWS_SYNC")
        return AmazonS3ClientBuilder
                .standard()
                .withRegion("eu-west-2")
                .withCredentials(AWSStaticCredentialsProvider(credentials))
                .build()
    }
}
