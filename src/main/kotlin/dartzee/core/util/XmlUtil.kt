package dartzee.core.util

import dartzee.logging.CODE_PARSE_ERROR
import dartzee.utils.InjectedThings.logger
import java.io.StringReader
import java.io.StringWriter
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.xml.sax.InputSource

// Cache one instance of the factories - I've seen from thread stacks that constructing it each time
// is really slow
private val factory = DocumentBuilderFactory.newInstance()
private val tf = TransformerFactory.newInstance()

object XmlUtil {
    fun factoryNewDocument(): Document {
        val builder = factory.newDocumentBuilder()
        return builder.newDocument()
    }
}

/** Extension fns */
fun Element.getAttributeInt(attributeName: String, defaultValue: Int = 0): Int {
    val attribute = getAttribute(attributeName)
    return if (attribute == "") defaultValue else attribute.toInt()
}

fun Element.setAttributeAny(key: String, value: Any) = setAttribute(key, "$value")

fun Document.createRootElement(name: String): Element = createElement(name).also { appendChild(it) }

fun Document.toXmlString(): String =
    try {
        val transformer = tf.newTransformer()
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes")
        val writer = StringWriter()
        transformer.transform(DOMSource(this), StreamResult(writer))

        val sb = writer.buffer
        sb.toString().replace("[\n\r]".toRegex(), "")
    } catch (t: Throwable) {
        logger.error(CODE_PARSE_ERROR, "Failed to convert xml doc to string", t)
        ""
    }

@Suppress("SwallowedException")
fun String.toXmlDoc() =
    try {
        val builder = factory.newDocumentBuilder()
        builder.setErrorHandler(null)
        builder.parse(InputSource(StringReader(this)))
    } catch (t: Throwable) {
        null
    }
