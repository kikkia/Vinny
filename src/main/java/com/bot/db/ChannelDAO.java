package com.bot.db;

import com.bot.models.InternalTextChannel;
import com.bot.models.InternalVoiceChannel;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.VoiceChannel;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChannelDAO {
    private static final Logger LOGGER = Logger.getLogger(ChannelDAO.class.getName());

    private Connection read;
    private Connection write;
    private static ChannelDAO instance;

    private ChannelDAO() {
        try {
            initialize();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static ChannelDAO getInstance() {
        if (instance == null) {
            instance = new ChannelDAO();
        }
        return instance;
    }

    private void initialize() throws SQLException {
        DataSource dataSource = ConnectionPool.getDataSource();
        DataSource readDataSource = ReadConnectionPool.getDataSource();

        this.read = readDataSource.getConnection();
        this.write = dataSource.getConnection();
    }

    public void addVoiceChannel(VoiceChannel voiceChannel) {
        String query = "INSERT INTO voice_channel(id, guild, name) VALUES (?,?,?) ON DUPLICATE KEY UPDATE name=VALUES(name)";
        PreparedStatement preparedStatement = null;

        try {
            preparedStatement = write.prepareStatement(query);
            preparedStatement.setString(1, voiceChannel.getId());
            preparedStatement.setString(2, voiceChannel.getGuild().getId());
            preparedStatement.setString(3, voiceChannel.getName());
            preparedStatement.execute();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to add voice channel to the db " +e.getSQLState());
        } finally {
            close(preparedStatement, null);
        }
    }

    public void addTextChannel(TextChannel textChannel) {
        String query = "INSERT INTO text_channel(id, guild, name) VALUES (?,?,?) ON DUPLICATE KEY UPDATE name=VALUES(name)";
        PreparedStatement preparedStatement = null;

        try {
            preparedStatement = write.prepareStatement(query);
            preparedStatement.setString(1, textChannel.getId());
            preparedStatement.setString(2, textChannel.getGuild().getId());
            preparedStatement.setString(3, textChannel.getName());
            preparedStatement.execute();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to add text channel to the db: " + e.getSQLState());
        } finally {
            close(preparedStatement, null);
        }
    }

    public void removeVoiceChannel(VoiceChannel channel) {
        String query = "DELETE FROM voice_channel WHERE id = ?";
        PreparedStatement preparedStatement = null;

        try {
            preparedStatement = write.prepareStatement(query);
            preparedStatement.setString(1, channel.getId());
            preparedStatement.execute();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to remove a voice channel from db: " +e.getSQLState());
        } finally {
            close(preparedStatement, null);
        }
    }

    public void removeTextChannel(TextChannel channel) {
        String query = "DELETE FROM text_channel WHERE id = ?";
        PreparedStatement preparedStatement = null;

        try {
            preparedStatement = write.prepareStatement(query);
            preparedStatement.setString(1, channel.getId());
            preparedStatement.execute();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to remove a text channel from db: " +e.getSQLState());
        } finally {
            close(preparedStatement, null);
        }
    }

    public boolean setVoiceChannelEnabled(Guild guild, String id, boolean enabled) {
        return false;
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
                boolean voice_enabled = set.getBoolean("voice_enabled");
                boolean nsfw_enabled = set.getBoolean("nsfw_enabled");
                boolean announcement = set.getBoolean("announcement");
                boolean command_enabled = set.getBoolean("commands_enabled");
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
                boolean voice_enabled = set.getBoolean("voice_enabled");
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
