package utils

import java.util.logging.Logger

abstract class Task(val logger: Logger) {
    abstract fun check(): Boolean

    abstract fun exec()

    fun getName(): String {
        val name = this.javaClass.name
        if (!name.contains(".")) {
            return name
        }

        return name.split(".").last()
    }
}