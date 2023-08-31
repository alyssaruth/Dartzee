package dartzee.types

open class StringMicrotype(val value: String) {
    override fun equals(other: Any?) =
            other is StringMicrotype
                && this.javaClass == other.javaClass
                && this.value == other.value

    override fun hashCode(): Int = value.hashCode()
    override fun toString(): String = value
}