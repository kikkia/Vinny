package com.bot.exceptions.newstyle

class ChannelTypeNotSupportedException(outputId: String, vararg args: Any): UserVisibleException(outputId, args)