package dartzee.dartzee

import dartzee.core.util.XmlUtil
import dartzee.core.util.createRootElement
import dartzee.core.util.toXmlDoc
import dartzee.core.util.toXmlString
import dartzee.dartzee.aggregate.AbstractDartzeeAggregateRule
import dartzee.dartzee.aggregate.DartzeeAggregateRuleCluster
import dartzee.dartzee.aggregate.DartzeeAggregateRuleDecreasing
import dartzee.dartzee.aggregate.DartzeeAggregateRuleDistinctScores
import dartzee.dartzee.aggregate.DartzeeAggregateRuleIncreasing
import dartzee.dartzee.aggregate.DartzeeAggregateRuleRepeats
import dartzee.dartzee.aggregate.DartzeeAggregateRuleSpread
import dartzee.dartzee.aggregate.DartzeeTotalRuleEqualTo
import dartzee.dartzee.aggregate.DartzeeTotalRuleEven
import dartzee.dartzee.aggregate.DartzeeTotalRuleGreaterThan
import dartzee.dartzee.aggregate.DartzeeTotalRuleLessThan
import dartzee.dartzee.aggregate.DartzeeTotalRuleMultipleOf
import dartzee.dartzee.aggregate.DartzeeTotalRuleOdd
import dartzee.dartzee.aggregate.DartzeeTotalRulePrime
import dartzee.dartzee.dart.AbstractDartzeeDartRule
import dartzee.dartzee.dart.DartzeeDartRuleAny
import dartzee.dartzee.dart.DartzeeDartRuleColour
import dartzee.dartzee.dart.DartzeeDartRuleCustom
import dartzee.dartzee.dart.DartzeeDartRuleEven
import dartzee.dartzee.dart.DartzeeDartRuleInner
import dartzee.dartzee.dart.DartzeeDartRuleMiss
import dartzee.dartzee.dart.DartzeeDartRuleOdd
import dartzee.dartzee.dart.DartzeeDartRuleOuter
import dartzee.dartzee.dart.DartzeeDartRuleScore
import org.w3c.dom.Document
import org.w3c.dom.Element

abstract class AbstractDartzeeRule
{
    abstract fun getRuleIdentifier(): String

    open fun writeXmlAttributes(doc: Document, rootElement: Element) {}

    fun populate(xmlStr: String)
    {
        val xmlDoc = xmlStr.toXmlDoc()
        xmlDoc ?: return

        populate(xmlDoc.documentElement)
    }

    open fun populate(rootElement: Element) {}

    open fun validate(): String = ""

    override fun toString() = getRuleIdentifier()

    open fun getDescription() = toString()

    open fun randomise() {}

    fun toDbString(): String
    {
        val xmlDoc = XmlUtil.factoryNewDocument()

        val rootElement = xmlDoc.createRootElement(getRuleIdentifier())
        writeXmlAttributes(xmlDoc, rootElement)

        return xmlDoc.toXmlString()
    }
}

fun getAllDartRules(): List<AbstractDartzeeDartRule>
{
    return listOf(DartzeeDartRuleAny(),
            DartzeeDartRuleEven(),
            DartzeeDartRuleOdd(),
            DartzeeDartRuleInner(),
            DartzeeDartRuleOuter(),
            DartzeeDartRuleColour(),
            DartzeeDartRuleScore(),
            DartzeeDartRuleCustom(),
            DartzeeDartRuleMiss())
}
fun getAllAggregateRules(): List<AbstractDartzeeAggregateRule>
{
    return listOf(
        DartzeeTotalRuleLessThan(),
        DartzeeTotalRuleGreaterThan(),
        DartzeeTotalRuleEqualTo(),
        DartzeeTotalRuleEven(),
        DartzeeTotalRuleOdd(),
        DartzeeTotalRuleMultipleOf(),
        DartzeeTotalRulePrime(),
        DartzeeAggregateRuleIncreasing(),
        DartzeeAggregateRuleDecreasing(),
        DartzeeAggregateRuleSpread(),
        DartzeeAggregateRuleCluster(),
        DartzeeAggregateRuleDistinctScores(),
        DartzeeAggregateRuleRepeats())
}
fun parseDartRule(xmlStr: String) = parseRule(xmlStr, getAllDartRules())
fun parseAggregateRule(xmlStr: String) = parseRule(xmlStr, getAllAggregateRules())
fun <K: AbstractDartzeeRule> parseRule(xmlStr: String, ruleTemplates: List<K>): K?
{
    if (xmlStr.isEmpty())
    {
        return null
    }

    val xmlDoc = xmlStr.toXmlDoc()
    xmlDoc ?: return null

    val rootElement = xmlDoc.documentElement

    val rule = ruleTemplates.find{ it.getRuleIdentifier() == rootElement.tagName }
    rule ?: return null

    rule.populate(rootElement)

    return rule
}