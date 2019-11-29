package com.bot.db;

import com.bot.models.ScheduledCommand;
import com.bot.utils.Logger;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ScheduledCommandDAO {
    private static final Logger LOGGER = new Logger(ScheduledCommandDAO.class.getName());

    private HikariDataSource write;
    private static ScheduledCommandDAO instance;

    private ScheduledCommandDAO() {
        if (instance == null) {
            initialize();
        }
    }

    public static ScheduledCommandDAO getInstance() {
        if (instance == null) {
            instance = new ScheduledCommandDAO();
        }
        return instance;
    }

    private void initialize() {
        write = ConnectionPool.getDataSource();
    }

    public void addScheduledCommand(ScheduledCommand command) throws SQLException {
        String query = "INSERT INTO scheduled_command(command, guild, channel, author, interval_time, last_run) VALUES (?,?,?,?,?,?)";

        try(Connection connection = write.getConnection()) {
            try(PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, command.getCommand());
                preparedStatement.setString(2, command.getGuild());
                preparedStatement.setString(3, command.getChannel());
                preparedStatement.setString(4, command.getAuthor());
                preparedStatement.setLong(5, command.getInterval());
                preparedStatement.setLong(6, System.currentTimeMillis());
                preparedStatement.execute();
            }
        }
    }

    public void updateLastRun(int id) throws SQLException {
        String query = "UPDATE scheduled_command SET last_run=? WHERE id=?";
        try(Connection connection = write.getConnection()) {
            try(PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setLong(1, System.currentTimeMillis());
                preparedStatement.setInt(2, id);
                preparedStatement.execute();
            }
        }
    }

    public List<ScheduledCommand> getAllScheduledCommands() throws SQLException {
        String query = "SELECT id, command, guild, channel, author, interval_time, last_run FROM scheduled_command";
        try (Connection connection = write.getConnection()){
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                return getCommands(statement);
            }
        }
    }

    public List<ScheduledCommand> getAllScheduledCommandsForAuthor(String userId) throws SQLException {
        String query = "SELECT id, command, guild, channel, author, interval_time, last_run FROM scheduled_command WHERE author = ?";
        try (Connection connection = write.getConnection()){
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, userId);
                return getCommands(statement);
            }
        }
    }

    public List<ScheduledCommand> getAllScheduledCommandsForChannel(String channelId) throws SQLException {
        String query = "SELECT id, command, guild, channel, author, interval_time, last_run FROM scheduled_command WHERE channel = ?";
        try (Connection connection = write.getConnection()){
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, channelId);
                return getCommands(statement);
            }
        }
    }

    public List<ScheduledCommand> getAllScheduledCommandsForGuild(String guildId) throws SQLException {
        String query = "SELECT id, command, guild, channel, author, interval_time, last_run FROM scheduled_command WHERE guild = ?";
        try (Connection connection = write.getConnection()){
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, guildId);
                return getCommands(statement);
            }
        }
    }

    public int getCountOfScheduledForAuthor(String id) {
        try {
            return getAllScheduledCommandsForAuthor(id).size();
        } catch (SQLException e) {
            LOGGER.severe("Failed to get author scheduled command count", e);
            // Just let them if this fails
            return 0;
        }
    }

    public ScheduledCommand getScheduledCommandByID(int id) throws SQLException {
        String query = "SELECT id, command, guild, channel, author, interval_time, last_run FROM scheduled_command WHERE id = ?";
        try (Connection connection = write.getConnection()){
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, id);
                return getCommands(statement).get(0);
            }
        }
    }

    public void removeScheduledCommand(int id) throws SQLException {
        String query = "DELETE FROM scheduled_command WHERE id = ?";
        try (Connection connection = write.getConnection()){
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, id);
                statement.execute();
            }
        }
    }

    private List<ScheduledCommand> getCommands(PreparedStatement statement) throws SQLException {
        List<ScheduledCommand> commands = new ArrayList<>();

        try(ResultSet set = statement.executeQuery()) {
            while (set.next()) {
                commands.add(new ScheduledCommand(
                        set.getInt("id"),
                        set.getString("command"),
                        set.getString("guild"),
                        set.getString("channel"),
                        set.getString("author"),
                        set.getLong("interval_time"),
                        set.getLong("last_run")
                ));
            }
        }
        return commands;
    }
}
