package com.bot.db;

import com.bot.models.InternalTextChannel;
import com.bot.models.InternalVoiceChannel;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChannelRespository {
    private static final Logger LOGGER = Logger.getLogger(ChannelRespository.class.getName());

    private Connection read;
    private Connection write;
    private static ChannelRespository instance;

    private ChannelRespository() {
        try {
            initialize();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static ChannelRespository getInstance() {
        if (instance == null) {
            instance = new ChannelRespository();
        }
        return instance;
    }

    private void initialize() throws SQLException {
        DataSource dataSource = ConnectionPool.getDataSource();
        DataSource readDataSource = ReadConnectionPool.getDataSource();

        this.read = readDataSource.getConnection();
        this.write = dataSource.getConnection();
    }

    public List<InternalVoiceChannel> getVoiceChannelsForGuild(String guildId) {
        String query = "Select c.id, c.name, c.voice_enabled FROM voice_channel c WHERE c.guild = " + guildId;
        return getVoiceChannelsForQuery(guildId, query);
    }

    public List<InternalTextChannel> getTextChannelsForGuild(String guildId) {
        String query = "Select c.id, c.name, c.voice_enabled, c.announcement, c.nsfw_enabled, c.commands_enabled FROM text_channel c WHERE c.guild = " + guildId;
        return getTextChannelsForQuery(guildId, query);
    }

    private List<InternalTextChannel> getTextChannelsForQuery(String guildId, String query) {
        List<InternalTextChannel> channels = null;
        PreparedStatement statement = null;
        ResultSet set = null;

        try {
            statement = read.prepareStatement(query);
            set = statement.executeQuery();
            channels = new ArrayList<>();
            while (set.next()) {
                String name = set.getString("name");
                String id = set.getString("id");
                Boolean voice_enabled = set.getBoolean("voice_enabled");
                Boolean nsfw_enabled = set.getBoolean("nsfw_enabled");
                Boolean announcement = set.getBoolean("announcement");
                Boolean command_enabled = set.getBoolean("commands_enabled");
                channels.add(new InternalTextChannel(id,
                        guildId,
                        name,
                        announcement,
                        nsfw_enabled,
                        command_enabled,
                        voice_enabled));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
        } finally {
            close(statement, set);
        }

        return channels;
    }

    private List<InternalVoiceChannel> getVoiceChannelsForQuery(String guildId, String query) {
        List<InternalVoiceChannel> channels = null;
        PreparedStatement statement = null;
        ResultSet set = null;

        try {
            statement = read.prepareStatement(query);
            set = statement.executeQuery();
            channels = new ArrayList<>();
            while (set.next()) {
                String name = set.getString("name");
                String id = set.getString("id");
                Boolean voice_enabled = set.getBoolean("voice_enabled");
                channels.add(new InternalVoiceChannel(id, guildId, name, voice_enabled));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
        } finally {
            close(statement, set);
        }

        return channels;
    }

    private void close(PreparedStatement preparedStatement, ResultSet resultSet) {
        try {
            if (preparedStatement != null)
                preparedStatement.close();
            if (resultSet != null)
                resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
