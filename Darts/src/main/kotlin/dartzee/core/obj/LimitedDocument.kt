package dartzee.core.obj

import javax.swing.text.AttributeSet
import javax.swing.text.PlainDocument

class LimitedDocument(private val limit: Int) : PlainDocument()
{
    override fun insertString(offset: Int, str: String?, attr: AttributeSet?)
    {
        str ?: return

        if (length + str.length > limit)
        {
            return
        }

        super.insertString(offset, str, attr)
    }
}