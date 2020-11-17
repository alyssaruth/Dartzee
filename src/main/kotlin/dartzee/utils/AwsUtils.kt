package dartzee.utils

import com.amazonaws.auth.BasicAWSCredentials
import java.nio.charset.Charset
import java.util.*

object AwsUtils
{
    fun readCredentials(resourceName: String) =
        try
        {
            val awsCredentials = javaClass.getResource("/$resourceName").readText()
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
}
