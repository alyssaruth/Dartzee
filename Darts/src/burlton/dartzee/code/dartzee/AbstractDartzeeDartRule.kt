package burlton.dartzee.code.dartzee

import burlton.core.code.util.XmlUtil
import burlton.dartzee.code.`object`.DartboardSegmentKt
import org.w3c.dom.Document
import org.w3c.dom.Element

abstract class AbstractDartzeeDartRule
{
    abstract fun isValidSegment(segment: DartboardSegmentKt) : Boolean

    abstract fun getRuleIdentifier(): String

    open fun writeXmlAttributes(doc: Document, rootElement: Element) {}

    fun populate(xmlStr: String)
    {
        val xmlDoc = XmlUtil.getDocumentFromXmlString(xmlStr)
        xmlDoc ?: return

        populate(xmlDoc.documentElement)
    }

    open fun populate(rootElement: Element) {}

    open fun validate(): String = ""

    override fun toString() = getRuleIdentifier()

    fun toDbString(): String
    {
        val xmlDoc = XmlUtil.factoryNewDocument()
        xmlDoc ?: return "<NULL>"

        val rootElement = xmlDoc.createElement(getRuleIdentifier())
        writeXmlAttributes(xmlDoc, rootElement)

        xmlDoc.appendChild(rootElement)
        return XmlUtil.getStringFromDocument(xmlDoc)
    }
}

fun getAllDartRules(): MutableList<AbstractDartzeeDartRule>
{
    return mutableListOf(DartzeeDartRuleEven(),
                         DartzeeDartRuleOdd(),
                         DartzeeDartRuleInner(),
                         DartzeeDartRuleOuter(),
                         DartzeeDartRuleColour(),
                         DartzeeDartRuleScore(),
                         DartzeeDartRuleCustom())
}

fun parseDartzeeRule(xmlStr: String): AbstractDartzeeDartRule?
{
    if (xmlStr.isEmpty())
    {
        return null
    }

    val xmlDoc = XmlUtil.getDocumentFromXmlString(xmlStr)
    xmlDoc ?: return null

    val rootElement = xmlDoc.documentElement

    val rule = getAllDartRules().find{it.getRuleIdentifier() == rootElement.tagName}
    rule ?: return null

    rule.populate(rootElement)

    return rule
}