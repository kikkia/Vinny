package com.bot.utils

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException

object DbHelpers {

    fun close(preparedStatement: PreparedStatement?, resultSet: ResultSet?, connection: Connection?) {
        try {
            connection?.close()
            preparedStatement?.close()
            resultSet?.close()
        } catch (e: SQLException) {
            e.printStackTrace()
        }

    }
}
