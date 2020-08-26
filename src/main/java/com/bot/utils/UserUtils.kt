package com.bot.utils

import com.bot.db.UserDAO
import com.bot.models.UserLevel
import java.sql.SQLException

class UserUtils {
    companion object {
        val logger = Logger(this::class.java.simpleName)

        @JvmStatic
        fun getUserLevel(id: String) : UserLevel {
            return try {
                val user = UserDAO.getInstance().getById(id)
                when {
                    user.donor -> UserLevel.DONOR
                    user.reviewer -> UserLevel.REVIEWER
                    else -> UserLevel.USER
                }
            } catch (e: SQLException) {
                logger.severe("Failed to get user from db when checking level", e)
                UserLevel.USER
            }
        }
    }
}