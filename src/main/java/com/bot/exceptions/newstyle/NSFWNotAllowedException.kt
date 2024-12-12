package com.bot.exceptions.newstyle

class NSFWNotAllowedException(outputId: String, vararg args: Any): UserVisibleException(outputId, args) {
}