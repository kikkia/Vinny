package db;

import com.bot.db.AliasDAO;
import com.bot.models.Alias;
import com.bot.models.InternalGuild;
import com.bot.models.InternalTextChannel;
import com.bot.utils.CommandCategories;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool;
import org.flywaydb.core.Flyway;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class AliasIT {

    private static Flyway flyway;
    private static final Logger LOGGER = Logger.getLogger(GuildIT.class.getName());
    private static AliasDAO aliasDAO;
    private static HikariDataSource dataSource;


    private List<InternalGuild> guilds = Arrays.asList(
            new InternalGuild("101", "guild-1", 100, "1", "2", "2", "1", "! v jk"),
            new InternalGuild("102", "guild-2", 100, "2", "2", "2", "3", null)
    );

    private List<InternalTextChannel> textChannels = Arrays.asList(
            new InternalTextChannel("1", "101", "general", false, false, true, true),
            new InternalTextChannel("3", "101", "test", false, false, true, true),
            new InternalTextChannel("4", "102", "general", false, false, true, true),
            new InternalTextChannel("5", "102", "afk", false, false, true, true)
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


        flyway = new Flyway();
        flyway.setDataSource(dataSource);

        aliasDAO = new AliasDAO(dataSource);
    }

    @Before
    public void setUp() throws SQLException {
        resetdb();
        loadGuilds();
        loadChannels();
    }

    private void resetdb() {
        flyway.clean();
        flyway.migrate();
    }

    private void loadGuilds() throws SQLException {
        String query = "INSERT INTO guild(id, name, default_volume, min_base_role_id, min_mod_role_id, min_voice_role_id, min_nsfw_role_id, prefixes) VALUES(?,?,?,?,?,?,?,?)";
        Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(query);

        for(InternalGuild g : guilds) {
            statement.setString(1, g.getId());
            statement.setString(2, g.getName());
            statement.setInt(3, g.getVolume());
            statement.setString(4, g.getRequiredPermission(CommandCategories.GENERAL));
            statement.setString(5, g.getRequiredPermission(CommandCategories.MODERATION));
            statement.setString(6, g.getRequiredPermission(CommandCategories.VOICE));
            statement.setString(7, g.getRequiredPermission(CommandCategories.NSFW));
            statement.setString(8, g.getPrefixes());

            statement.addBatch();
        }

        statement.executeBatch();
        statement.close();
        connection.close();
    }

    private void loadChannels() throws SQLException {
        String voiceChannelQuery = "INSERT INTO voice_channel(id, guild, name) VALUES (?,?,?) ON DUPLICATE KEY UPDATE name=VALUES(name)";
        String textChannelQuery = "INSERT INTO text_channel(id, guild, name) VALUES (?,?,?) ON DUPLICATE KEY UPDATE name=VALUES(name)";
        Connection connection = dataSource.getConnection();
        PreparedStatement textStatement = connection.prepareStatement(textChannelQuery);
        PreparedStatement voiceStatement = connection.prepareStatement(voiceChannelQuery);

        for (InternalTextChannel c : textChannels) {
            textStatement.setString(1, c.getId());
            textStatement.setString(2, c.getGuildId());
            textStatement.setString(3, c.getName());

            textStatement.addBatch();
        }

        textStatement.executeBatch();
        voiceStatement.executeBatch();
        textStatement.close();
        voiceStatement.close();
        connection.close();
    }

    private void assertAliasEquals(Alias expected, Alias actual) {
        assertEquals(expected.getAlias(), actual.getAlias());
        assertEquals(expected.getAuthor(), actual.getAuthor());
        assertEquals(expected.getCommand(), actual.getCommand());
        assertEquals(expected.getScopeId(), actual.getScopeId());
    }

    @Test
    private void testAddGetAndRemoveAliasForGuild() throws Exception {
        Alias alias1 = new Alias("alias1", "~rr animemes", "101", "me");
        Alias alias2 = new Alias("alias2", "~tr pcmasterrace", "101", "me");

        // Add aliases to guild
        aliasDAO.addGuildAlias(alias1);
        aliasDAO.addGuildAlias(alias2);

        // Get aliases
        Map<String, Alias> aliasMap = aliasDAO.getGuildAliases("101");
        assertEquals(2, aliasMap.size());

        assertAliasEquals(alias1, aliasMap.get("alias1"));
        assertAliasEquals(alias2, aliasMap.get("alias2"));

        // Remove one
        aliasDAO.removeGuildAlias(alias1, "101");

        // Check removal
        aliasMap = aliasDAO.getGuildAliases("101");
        assertEquals(1, aliasMap.size());
        assertNull(aliasMap.get("alias1"));
        assertAliasEquals(alias2, aliasMap.get("alias2"));
    }

    @Test
    private void testAddGetAndRemoveAliasForChannel() throws Exception {
        Alias alias1 = new Alias("alias1", "~rr animemes", "1", "me");
        Alias alias2 = new Alias("alias2", "~tr pcmasterrace", "1", "me");

        // Add aliases to guild
        aliasDAO.addChannelAlias(alias1);
        aliasDAO.addChannelAlias(alias2);

        // Get aliases
        Map<String, Alias> aliasMap = aliasDAO.getChannelAliases("1");
        assertEquals(2, aliasMap.size());

        assertAliasEquals(alias1, aliasMap.get("alias1"));
        assertAliasEquals(alias2, aliasMap.get("alias2"));

        // Remove one
        aliasDAO.removeChannelAlias(alias1, "1");

        // Check removal
        aliasMap = aliasDAO.getChannelAliases("1");
        assertEquals(1, aliasMap.size());
        assertNull(aliasMap.get("alias1"));
        assertAliasEquals(alias2, aliasMap.get("alias2"));
    }
}
