package db;

import com.bot.db.PlaylistDAO;
import com.bot.models.AudioTrack;
import com.bot.models.InternalGuild;
import com.bot.models.InternalGuildMembership;
import com.bot.models.Playlist;
import com.bot.utils.CommandCategories;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool;
import org.flywaydb.core.Flyway;
import org.junit.Before;
import org.junit.BeforeClass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PlaylistIT {

    private static Connection connection;
    private static Flyway flyway;
    private static PlaylistDAO playlistDAO;
    private static final Logger LOGGER = Logger.getLogger(GuildIT.class.getName());

    private List<InternalGuildMembership> memberships = Arrays.asList(
            new InternalGuildMembership("1", "user-1", "101", true),
            new InternalGuildMembership("2", "user-2", "102", true),
            new InternalGuildMembership("3", "user-3", "101", true),
            new InternalGuildMembership("4", "user-4", "101", true),
            new InternalGuildMembership("3", "user-3", "102", false)
    );

    private List<InternalGuild> guilds = Arrays.asList(
            new InternalGuild("101", "guild-1", 100, "1", "2", "2", "1"),
            new InternalGuild("102", "guild-2", 100, "2", "2", "2", "3")
    );

    private List<AudioTrack> tracks = Arrays.asList(
            new AudioTrack("url-track-1", "title-1", 1),
            new AudioTrack("url-track-2", "title-2", 2),
            new AudioTrack("url-track-3", "title-3", 3)
    );

    private List<Playlist> userPlaylists = Arrays.asList(
            new Playlist(1, "1", "Playlist-user-1", tracks)
    );

    private List<Playlist> guildPlaylists = Arrays.asList(
            new Playlist(2, "101", "Playlist-Guild-101", tracks)
    );

    @BeforeClass
    public static void setUpConnections() throws SQLException, InterruptedException {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl("jdbc:mysql://localhost:1337/testdb");
        hikariConfig.setUsername("mysql");
        hikariConfig.setPassword("mysql");
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        hikariConfig.addDataSourceProperty("useServerPrepStmts", "true");

        // The integration tests start too fast (OH NOOOO) So we might need to wait for a couple seconds to ensure the db is up.
        HikariDataSource dataSource;
        int count = 0;
        int maxTries = 10;
        while(true) {
            int waitTime = ++count * 1000;
            try {
                Thread.sleep(waitTime);
                dataSource = new HikariDataSource(hikariConfig);
                break;
            } catch (HikariPool.PoolInitializationException e) {
                LOGGER.log(Level.WARNING, "Failed to connect to db, waiting for " + waitTime + " ms");
                if (count == maxTries) throw e;
            }
        }

        connection = dataSource.getConnection();

        flyway = new Flyway();
        flyway.setDataSource(dataSource);

        playlistDAO = new PlaylistDAO(connection);
    }

    @Before
    public void setUp() throws SQLException {
        resetdb();
        loadGuilds();
        loadUsers();
        loadPlaylists();
    }

    private void resetdb() {
        flyway.clean();
        flyway.migrate();
    }

    private void loadPlaylists() throws SQLException {
        String playlistInsertQuery = "INSERT INTO playlist (user_id, guild, name) VALUES(?,?,?) ON DUPLICATE KEY UPDATE name = name";
        String trackInsertQuery = "INSERT INTO track (url, title) VALUES (?, ?) ON DUPLICATE KEY UPDATE title = title";
        String playlistTrackQuery = "INSERT INTO playlist_track (track, playlist, position) VALUES (?, ?, ?)";

        PreparedStatement trackStatement = connection.prepareStatement(trackInsertQuery);
        for (AudioTrack t : tracks) {
            trackStatement.setString(1, t.getUrl());
            trackStatement.setString(2, t.getTitle());

            trackStatement.addBatch();
        }
        trackStatement.executeBatch();
        trackStatement.close();

        PreparedStatement playlistStatement = connection.prepareStatement(playlistInsertQuery);
        PreparedStatement playlistTrackStatement = connection.prepareStatement(playlistTrackQuery);
        for (Playlist p : userPlaylists) {
            playlistStatement.setString(1, p.getOwnerID());
            playlistStatement.setString(3, p.getName());

            for (AudioTrack a : p.getTracks()) {
            }

            playlistStatement.addBatch();
        }
        playlistStatement.executeBatch();
        playlistStatement.close();

        PreparedStatement guildPlaylistStatement = connection.prepareStatement(playlistInsertQuery);
        for (Playlist p : guildPlaylists) {
            guildPlaylistStatement.setString(2, p.getOwnerID());
            guildPlaylistStatement.setString(3, p.getName());

            guildPlaylistStatement.addBatch();
        }
        guildPlaylistStatement.executeBatch();
        guildPlaylistStatement.close();

    }

    private void loadGuilds() throws SQLException {
        String query = "INSERT INTO guild(id, name, default_volume, min_base_role_id, min_mod_role_id, min_voice_role_id, min_nsfw_role_id) VALUES(?,?,?,?,?,?,?)";
        PreparedStatement statement = connection.prepareStatement(query);

        for(InternalGuild g : guilds) {
            statement.setString(1, g.getId());
            statement.setString(2, g.getName());
            statement.setInt(3, g.getVolume());
            statement.setString(4, g.getRequiredPermission(CommandCategories.GENERAL));
            statement.setString(5, g.getRequiredPermission(CommandCategories.MOD));
            statement.setString(6, g.getRequiredPermission(CommandCategories.VOICE));
            statement.setString(7, g.getRequiredPermission(CommandCategories.NSFW));

            statement.addBatch();
        }

        statement.executeBatch();
        statement.close();
    }

    private void loadUsers() throws SQLException {
        String userQuery = "INSERT INTO users(id, name) VALUES(?,?) ON DUPLICATE KEY UPDATE name=name";
        String membershipQuery = "INSERT INTO guild_membership(guild, user_id, can_use_bot) VALUES(?,?,?)";
        PreparedStatement userStatement = connection.prepareStatement(userQuery);
        PreparedStatement membershipStatement = connection.prepareStatement(membershipQuery);

        for (InternalGuildMembership u : memberships) {
            userStatement.setString(1, u.getId());
            userStatement.setString(2, u.getName());

            userStatement.addBatch();

            membershipStatement.setString(1, u.getGuildId());
            membershipStatement.setString(2, u.getId());
            membershipStatement.setBoolean(3, u.canUseBot());

            membershipStatement.addBatch();
        }

        userStatement.executeBatch();
        userStatement.close();
        membershipStatement.executeBatch();
        membershipStatement.close();
    }
}
