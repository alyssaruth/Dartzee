package burlton.dartzee.code.dartzee.dart

import burlton.dartzee.code.dartzee.IDartzeeRuleConfigurable
import javax.swing.JPanel

abstract class AbstractDartzeeDartRuleConfigurable: AbstractDartzeeDartRule(), IDartzeeRuleConfigurable
{
    override var configPanel = JPanel()
}