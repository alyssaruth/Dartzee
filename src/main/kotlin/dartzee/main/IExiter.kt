package dartzee.main

import kotlin.system.exitProcess

interface IExiter {
    fun exit(status: Int)
}

class DefaultExiter : IExiter {
    override fun exit(status: Int) {
        exitProcess(status)
    }
}
