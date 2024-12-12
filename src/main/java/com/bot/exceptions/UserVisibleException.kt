package com.bot.exceptions

open class UserVisibleException(val outputId: String, vararg args: Any) : RuntimeException(outputId)