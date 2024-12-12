package com.bot.exceptions.newstyle

open class UserVisibleException(val outputId: String, vararg args: Any) : RuntimeException(outputId)