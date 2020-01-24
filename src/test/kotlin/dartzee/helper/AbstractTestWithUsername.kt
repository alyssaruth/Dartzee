package dartzee.helper

import dartzee.core.util.CoreRegistry.INSTANCE_STRING_USER_NAME
import dartzee.core.util.CoreRegistry.instance

abstract class AbstractTestWithUsername: AbstractTest()
{
    private val originalName = instance.get(INSTANCE_STRING_USER_NAME, "")

    override fun beforeEachTest()
    {
        super.beforeEachTest()
        instance.put(INSTANCE_STRING_USER_NAME, "TestUser")
    }

    override fun afterEachTest()
    {
        super.afterEachTest()
        instance.put(INSTANCE_STRING_USER_NAME, originalName)
    }
}