package com.bot.exceptions.newstyle

class UserPermissionsException(outputId: String, vararg args: Any) : UserVisibleException(outputId, args)