package com.bot.db.mappers;

import com.bot.models.BannedImage;

import java.sql.ResultSet;
import java.sql.SQLException;

public class BannedImageMapper {
    public static BannedImage mapSetToBannedImage(ResultSet set) throws SQLException {
        return new BannedImage(set.getInt("id"),
                set.getString("author"),
                set.getString("guild"),
                set.getString("hash"));
    }
}
