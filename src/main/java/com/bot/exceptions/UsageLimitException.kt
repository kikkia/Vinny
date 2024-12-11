package com.bot.exceptions

class UsageLimitException(outputId: String, vararg args: Any) : UserVisibleException(outputId, args)