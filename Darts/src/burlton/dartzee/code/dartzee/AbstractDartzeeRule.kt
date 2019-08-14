package burlton.dartzee.code.dartzee

import burlton.core.code.util.XmlUtil
import burlton.dartzee.code.dartzee.dart.*
import burlton.dartzee.code.dartzee.total.*
import org.w3c.dom.Document
import org.w3c.dom.Element

abstract class AbstractDartzeeRule
{
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

    open fun getDescription() = toString()

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

fun getAllDartRules(): List<AbstractDartzeeRule>
{
    return mutableListOf(DartzeeDartRuleEven(),
            DartzeeDartRuleOdd(),
            DartzeeDartRuleInner(),
            DartzeeDartRuleOuter(),
            DartzeeDartRuleColour(),
            DartzeeDartRuleScore(),
            DartzeeDartRuleCustom())
}

fun getAllTotalRules(): List<AbstractDartzeeRule>
{
    return mutableListOf(DartzeeTotalRuleLessThan(),
            DartzeeTotalRuleGreaterThan(),
            DartzeeTotalRuleEqualTo(),
            DartzeeTotalRuleEven(),
            DartzeeTotalRuleOdd(),
            DartzeeTotalRulePrime())
}
fun parseDartRule(xmlStr: String) = parseRule(xmlStr, getAllDartRules())
fun parseTotalRule(xmlStr: String) = parseRule(xmlStr, getAllTotalRules())
fun parseRule(xmlStr: String, ruleTemplates: List<AbstractDartzeeRule>): AbstractDartzeeRule?
{
    if (xmlStr.isEmpty())
    {
        return null
    }

    val xmlDoc = XmlUtil.getDocumentFromXmlString(xmlStr)
    xmlDoc ?: return null

    val rootElement = xmlDoc.documentElement

    val rule = ruleTemplates.find{it.getRuleIdentifier() == rootElement.tagName}
    rule ?: return null

    rule.populate(rootElement)

    return rule
}