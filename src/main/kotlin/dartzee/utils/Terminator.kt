package dartzee.utils

import kotlin.system.exitProcess

interface ITerminator
{
    fun terminate(code: Int)
}

class Terminator: ITerminator
{
    override fun terminate(code: Int)
    {
        exitProcess(code)
    }
}