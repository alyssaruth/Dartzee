package burlton.core.code.util

import org.w3c.dom.Document
import org.w3c.dom.Element
import org.xml.sax.InputSource
import java.io.StringReader
import java.io.StringWriter
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

//Cache one instance of the factories - I've seen from thread stacks that constructing it each time is really slow
private val factory = DocumentBuilderFactory.newInstance()
private val tf = TransformerFactory.newInstance()

object XmlUtil
{
    fun factoryNewDocument(): Document
    {
        val builder = factory.newDocumentBuilder()
        return builder.newDocument()
    }

    fun getStringFromDocument(xmlDoc: Document): String
    {
        try
        {
            val transformer = tf.newTransformer()
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes")
            val writer = StringWriter()
            transformer.transform(DOMSource(xmlDoc), StreamResult(writer))

            val sb = writer.buffer
            return sb.toString().replace("\n|\r".toRegex(), "")
        }
        catch (t: Throwable)
        {
            Debug.stackTrace(t)
            return ""
        }

    }

    fun getDocumentFromXmlString(xmlStr: String) =
        try
        {
            val builder = factory.newDocumentBuilder()
            builder.setErrorHandler(null)
            builder.parse(InputSource(StringReader(xmlStr)))
        }
        catch (t: Throwable)
        {
            null
        }
}

/**
 * Extension fns
 */
fun Element.getAttributeInt(attributeName: String, defaultValue: Int = 0): Int
{
    val attribute = getAttribute(attributeName)
    return if (attribute == "") defaultValue else attribute.toInt()
}

fun Element.getAttributeDouble(attributeName: String): Double
{
    val attribute = getAttribute(attributeName)
    return if (attribute == "") 0.0 else attribute.toDouble()
}

fun Element.readIntegerHashMap(tagName: String, keyTag: String, valueTag: String): MutableMap<Int, Int>
{
    val hm = mutableMapOf<Int, Int>()

    val children = getElementsByTagName(tagName)
    val size = children.length
    for (i in 0 until size)
    {
        val child = children.item(i) as Element
        val key = child.getAttributeInt(keyTag)
        val value = child.getAttributeInt(valueTag)

        hm[key] = value
    }

    return hm
}

fun Element.writeHashMap(hm: Map<*, *>, tagName: String, keyTag: String, valueTag: String)
{
    hm.forEach { key, value ->
        val child = ownerDocument.createElement(tagName)
        child.setAttribute(keyTag, "$key")
        child.setAttribute(valueTag, "$value")
        appendChild(child)
    }
}

fun Element.setAttribute(key: String, value: Any) = setAttribute(key, "$value")

fun Document.createRootElement(name: String) = createElement(name).also { appendChild(it) }
