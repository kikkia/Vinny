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
        String query = "SELECT id, command, guild, channel, author, interval_time, last_run FROM command_alias";
        try (Connection connection = write.getConnection()){
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                return getCommands(statement);
            }
        }
    }

    public List<ScheduledCommand> getAllScheduledCommandsForAuthor(String userId) throws SQLException {
        String query = "SELECT id, command, guild, channel, author, interval_time, last_run FROM command_alias WHERE author = ?";
        try (Connection connection = write.getConnection()){
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, userId);
                return getCommands(statement);
            }
        }
    }

    public List<ScheduledCommand> getAllScheduledCommandsForChannel(String channelId) throws SQLException {
        String query = "SELECT id, command, guild, channel, author, interval_time, last_run FROM command_alias WHERE channel = ?";
        try (Connection connection = write.getConnection()){
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, channelId);
                return getCommands(statement);
            }
        }
    }

    public List<ScheduledCommand> getAllScheduledCommandsForGuild(String guildId) throws SQLException {
        String query = "SELECT id, command, guild, channel, author, interval_time, last_run FROM command_alias WHERE guild = ?";
        try (Connection connection = write.getConnection()){
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, guildId);
                return getCommands(statement);
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
