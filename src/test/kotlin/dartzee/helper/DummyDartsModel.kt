import dartzee.ai.AbstractDartsModel
import dartzee.screen.Dartboard
import org.w3c.dom.Element
import java.awt.Point

class DummyDartsModel: AbstractDartsModel()
{
    var foo = ""

    override fun getModelName() = "Test"
    override fun getType() = 100
    override fun writeXmlSpecific(rootElement: Element) { rootElement.setAttribute("Foo", foo)}
    override fun readXmlSpecific(root: Element) { foo = root.getAttribute("Foo")}
    override fun throwDartAtPoint(pt: Point, dartboard: Dartboard) = pt
    override fun getProbabilityWithinRadius(radius: Double) = 1.0
}