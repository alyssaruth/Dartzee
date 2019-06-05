package burlton.dartzee.code.utils

import burlton.core.code.util.AbstractClient
import burlton.desktopcore.code.util.AbstractDesktopClient

class DesktopDartsClient : AbstractDesktopClient()
{
    override fun init()
    {
        super.init()

        AbstractClient.derbyDbName = DartsDatabaseUtil.DATABASE_NAME
    }

    override fun checkForUpdates()
    {
        UpdateManager.checkForUpdates(DARTS_VERSION_NUMBER)
    }
}
