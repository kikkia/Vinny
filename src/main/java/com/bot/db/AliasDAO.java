package com.bot.db;

import com.bot.models.Alias;
import com.bot.utils.Logger;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class AliasDAO {

    private static final Logger LOGGER = new Logger(ChannelDAO.class.getName());

    private HikariDataSource write;
    private static AliasDAO instance;

    private final String GUILD_COLUMN_NAME = "guild";
    private final String CHANNEL_COLUMN_NAME = "channel";
    private final String USER_COLUMN_NAME = "user";

    private AliasDAO() {
        try {
            if (instance == null) {
                initialize();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            LOGGER.severe("Failed to initialize AliasDAO", e);
        }
    }

    public AliasDAO(HikariDataSource dataSource) {
        this.write = dataSource;
    }

    public static AliasDAO getInstance() {
        if (instance == null) {
            instance = new AliasDAO();
        }
        return instance;
    }

    private void initialize() throws SQLException {
        this.write = ConnectionPool.getDataSource();
    }

    public void addGuildAlias(Alias alias) throws SQLException {
        String guild_alias_insert_query = "INSERT INTO guild_aliases(guild, alias) VALUES (?,?)";

        try(Connection connection = write.getConnection()) {
            addAlias(connection, alias.getAlias(), alias.getCommand(), alias.getAuthor(), alias.getScopeId(), guild_alias_insert_query);
        }
    }

    public void addChannelAlias(Alias alias) throws SQLException {
        String channel_alias_insert_query = "INSERT INTO channel_aliases(channel, alias) VALUES (?,?)";

        try(Connection connection = write.getConnection()) {
            addAlias(connection, alias.getAlias(), alias.getCommand(), alias.getAuthor(), alias.getScopeId(), channel_alias_insert_query);
        }
    }

    public void addUserAlias(Alias alias) throws SQLException {
        String user_alias_insert_query = "INSERT INTO user_aliases(user, alias) VALUES (?,?)";

        try(Connection connection = write.getConnection()) {
            addAlias(connection, alias.getAlias(), alias.getCommand(), alias.getAuthor(), alias.getScopeId(), user_alias_insert_query);
        }
    }

    public Map<String, Alias> getGuildAliases(String guildId) throws SQLException {
        String selectQuery = "SELECT a.alias, a.command, a.author, ga.guild FROM command_aliases a " +
                "JOIN guild_aliases ga ON a.id = ga.alias WHERE ga.guild = ?";

        return getAliases(guildId, GUILD_COLUMN_NAME, selectQuery);
    }

    public Map<String, Alias> getChannelAliases(String channelId) throws SQLException {
        String selectQuery = "SELECT a.alias, a.command, a.author, ca.channel FROM command_aliases a " +
                "JOIN channel_aliases ca ON a.id = ca.alias WHERE ca.channel = ?";

        return getAliases(channelId, CHANNEL_COLUMN_NAME, selectQuery);
    }

    public Map<String, Alias> getUserAliases(String userId) throws SQLException {
        String selectQuery = "SELECT a.alias, a.command, a.author, ua.guild FROM command_aliases a " +
                "JOIN guild_aliases ua ON a.id = ga.alias WHERE ua.user = ?";

        return getAliases(userId, USER_COLUMN_NAME, selectQuery);
    }

    public void removeGuildAlias(Alias alias, String guildId) throws SQLException {
        String query = "DELETE FROM guild_aliases ga JOIN command_aliases ca ON ga.alias = ca.id " +
                "WHERE ca.alias = ? AND ga.guild = ?";

        try(Connection conn = write.getConnection()) {
            deleteAlias(conn, alias.getAlias(), guildId, query);
        }
    }

    public void removeChannelAlias(Alias alias, String channelId) throws SQLException {
        String query = "DELETE FROM channel_aliases ch JOIN command_aliases ca ON ch.alias = ca.id " +
                "WHERE ca.alias = ? AND ch.channel = ?";

        try(Connection conn = write.getConnection()) {
            deleteAlias(conn, alias.getAlias(), channelId, query);
        }
    }

    public void removeUserAlias(Alias alias, String userId) throws SQLException {
        String query = "DELETE FROM user_aliases ua JOIN command_aliases ca ON ua.alias = ca.id " +
                "WHERE ca.alias = ? AND ua.user = ?";

        try(Connection conn = write.getConnection()) {
            deleteAlias(conn, alias.getAlias(), userId, query);
        }
    }

    private Map<String, Alias> getAliases(String scopeId, String columnName, String selectQuery) throws SQLException {
        Map<String, Alias> aliases = new HashMap<>();

        try (Connection connection = write.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(selectQuery)) {
                statement.setString(1, scopeId);
                try (ResultSet set = statement.executeQuery()){
                    while (set.next()) {
                        aliases.put(set.getString("alias"),
                                new Alias(set.getString("alias"),
                                        set.getString("command"),
                                        set.getString(columnName),
                                        set.getString("author")));
                    }
                }
            }
        }
        return aliases;
    }

    private void addAlias(Connection connection, String alias, String command, String authorId, String scopeId, String joinInsertQuery) throws SQLException {
        String alias_insert_query = "INSERT INTO command_aliases(alias, command, author) VALUES (?,?,?)";
        int aliasId;
        try(PreparedStatement statement = connection.prepareStatement(alias_insert_query, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, alias);
            statement.setString(2, command);
            statement.setString(3, authorId);
            statement.execute();
            try (ResultSet set = statement.getGeneratedKeys()) {
                set.next();
                aliasId = set.getInt(1);
            }
        }

        try (PreparedStatement statement = connection.prepareStatement(joinInsertQuery)) {
            statement.setString(1, scopeId);
            statement.setInt(2, aliasId);
            statement.execute();
        }
    }

    private void deleteAlias(Connection conn, String alias, String scopeId, String deleteQuery) throws SQLException {
        try(PreparedStatement statement = conn.prepareStatement(deleteQuery)){
           statement.setString(1, alias);
           statement.setString(2, scopeId);
           statement.execute();
        }
    }
}
