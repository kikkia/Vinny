package com.bot.exceptions

open class UserVisibleException(outputId: String, vararg args: Any) : RuntimeException(outputId)