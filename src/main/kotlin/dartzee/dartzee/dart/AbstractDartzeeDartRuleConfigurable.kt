package dartzee.dartzee.dart

import dartzee.dartzee.IDartzeeRuleConfigurable
import javax.swing.JPanel

abstract class AbstractDartzeeDartRuleConfigurable :
    AbstractDartzeeDartRule(), IDartzeeRuleConfigurable {
    override var configPanel = JPanel()
}
