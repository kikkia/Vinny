package com.bot.exceptions

class UserNotInVoiceChannelException(message: String = "You are not in a voice channel") : Exception(message)