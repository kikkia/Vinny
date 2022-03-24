package com.bot.models

data class BannedImage(
        val id: Int,
        val author: String,
        val guild: String,
        val hash: String)