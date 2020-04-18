package dartzee.helper

import dartzee.utils.ITerminator

class TerminationException(val code: Int): Exception()

class FakeTerminator: ITerminator
{
    override fun terminate(code: Int)
    {
        throw TerminationException(code)
    }
}