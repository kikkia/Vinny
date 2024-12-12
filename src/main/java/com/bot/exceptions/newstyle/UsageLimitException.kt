package com.bot.exceptions.newstyle

class UsageLimitException(outputId: String, vararg args: Any) : UserVisibleException(outputId, args)