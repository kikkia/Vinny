package com.bot.exceptions

class ChannelTypeNotSupportedException(outputId: String, vararg args: Any): UserVisibleException(outputId, args)