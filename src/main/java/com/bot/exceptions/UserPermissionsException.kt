package com.bot.exceptions

class UserPermissionsException(outputId: String, vararg args: Any) : UserVisibleException(outputId, args)