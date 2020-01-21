package dartzee.test.core.util

import dartzee.core.util.*
import dartzee.test.helper.AbstractTest
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.shouldBe
import org.junit.Test

class TestXmlUtil: AbstractTest()
{
    @Test
    fun `Should convert an XML doc to a string`()
    {
        val doc = XmlUtil.factoryNewDocument()

        val rootElement = doc.createRootElement("Root")

        val childA = doc.createElement("A")
        rootElement.appendChild(childA)

        rootElement.setAttribute("Foo", "Bar")

        doc.toXmlString() shouldBe """<Root Foo="Bar"><A/></Root>"""
    }

    @Test
    fun `Should handle an empty document`()
    {
        val doc = XmlUtil.factoryNewDocument()
        doc.toXmlString() shouldBe ""
    }

    @Test
    fun `Should return null for an invalid string`()
    {
        val str = "bugg'rit"
        str.toXmlDoc() shouldBe null
    }

    @Test
    fun `Should convert a valid xml string back to a document`()
    {
        val str = """<Xml Foo="bar"/>"""
        val doc = str.toXmlDoc()!!

        val element = doc.documentElement
        element.tagName shouldBe "Xml"
        element.getAttribute("Foo") shouldBe "bar"
    }

    @Test
    fun `Should create a root element and append it to the doc`()
    {
        val doc = XmlUtil.factoryNewDocument()
        doc.createRootElement("Baz")

        val element = doc.documentElement
        element.tagName shouldBe "Baz"
    }

    @Test
    fun `Should handle setting non-string attributes`()
    {
        val doc = XmlUtil.factoryNewDocument()
        val root = doc.createRootElement("Root")

        root.setAttributeAny("Foo", 1)
        root.getAttribute("Foo") shouldBe "1"
    }

    @Test
    fun `Should return integer value if valid`()
    {
        val doc = XmlUtil.factoryNewDocument()
        val root = doc.createRootElement("Root")

        root.setAttributeAny("Foo", 1)
        root.getAttributeInt("Foo") shouldBe 1
    }

    @Test
    fun `Should return defaultValue if not present`()
    {
        val doc = XmlUtil.factoryNewDocument()
        val root = doc.createRootElement("Root")

        root.getAttributeInt("Foo", 12) shouldBe 12
    }

    @Test
    fun `Should return 0 if not present and no default value specified`()
    {
        val doc = XmlUtil.factoryNewDocument()
        val root = doc.createRootElement("Root")

        root.getAttributeInt("Foo") shouldBe 0
    }

    @Test
    fun `Should return double value if valid`()
    {
        val doc = XmlUtil.factoryNewDocument()
        val root = doc.createRootElement("Root")

        root.setAttributeAny("Foo", 1.5)
        root.getAttributeDouble("Foo") shouldBe 1.5
    }

    @Test
    fun `Should return 0 if double attrib not present`()
    {
        val doc = XmlUtil.factoryNewDocument()
        val root = doc.createRootElement("Root")

        root.getAttributeDouble("Foo") shouldBe 0.0
    }

    @Test
    fun `Should read and write Integer hashmaps`()
    {
        val map = mutableMapOf<Int, Int>()
        map[0] = 10
        map[1] = 20
        map[5] = -5

        val doc = XmlUtil.factoryNewDocument()
        val root = doc.createRootElement("Root")

        root.writeHashMap(map, "SomeMap")
        val newMap = root.readIntegerHashMap("SomeMap")
        newMap.size shouldBe 3
        newMap[0] shouldBe 10
        newMap[1] shouldBe 20
        newMap[5] shouldBe -5
    }

    @Test
    fun `Should read and write lists`()
    {
        val list = listOf("Foo", "Bar", "Baz")

        val doc = XmlUtil.factoryNewDocument()
        val root = doc.createRootElement("Root")

        root.writeList(list, "SomeList")

        val newList = root.readList("SomeList")
        newList.shouldContainExactly("Foo", "Bar", "Baz")
    }
}