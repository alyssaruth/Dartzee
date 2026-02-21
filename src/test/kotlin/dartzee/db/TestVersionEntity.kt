package dartzee.db

class TestVersionEntity : AbstractEntityTest<VersionEntity>() {
    override fun factoryDao() = VersionEntity()
}
