package com.bot.db;

import com.bot.db.mappers.BannedImageMapper;
import com.bot.models.BannedImage;
import com.bot.utils.Logger;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BannedImageDAO {

    private static final Logger LOGGER = new Logger(ScheduledCommandDAO.class.getName());

    private HikariDataSource write;
    private static BannedImageDAO instance;

    private BannedImageDAO() {
        if (instance == null) {
            initialize();
        }
    }

    public static BannedImageDAO getInstance() {
        if (instance == null) {
            instance = new BannedImageDAO();
        }
        return instance;
    }

    private void initialize() {
        write = ConnectionPool.getDataSource();
    }

    public void addBannedImage(BannedImage image) throws SQLException {
        String query = "INSERT INTO banned_images(guild, author, hash) VALUES (?,?,?)";

        try(Connection connection = write.getConnection()) {
            try(PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, image.getGuild());
                preparedStatement.setString(2, image.getAuthor());
                preparedStatement.setString(3, image.getHash());
                preparedStatement.execute();
            }
        }
    }

    public List<BannedImage> getAllInGuild(String guildId) {
        String query = "SELECT id, author, guild, hash FROM banned_images WHERE guild = ?";
        try (Connection connection = write.getConnection()){
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, guildId);
                return getImages(statement);
            }
        } catch (SQLException e) {
            LOGGER.severe("Failed to get all banned images in guild", e);
            return Collections.emptyList();
        }
    }

    public BannedImage getByHashAndGuild(String hash, String guild) throws SQLException {
        String query = "SELECT id, author, guild, hash FROM banned_images WHERE hash = ? AND guild = ?";
        try (Connection connection = write.getConnection()){
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, hash);
                statement.setString(2, guild);
                ResultSet set = statement.executeQuery();
                return BannedImageMapper.mapSetToBannedImage(set);
            }
        }
    }

    private List<BannedImage> getImages(PreparedStatement statement) throws SQLException {
        List<BannedImage> commands = new ArrayList<>();

        try(ResultSet set = statement.executeQuery()) {
            while (set.next()) {
                commands.add(BannedImageMapper.mapSetToBannedImage(set));
            }
        }
        return commands;
    }
}
