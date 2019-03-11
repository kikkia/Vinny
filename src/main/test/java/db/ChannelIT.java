package db;

import com.bot.db.ChannelDAO;
import com.bot.models.*;
import com.bot.utils.CommandCategories;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.VoiceChannel;
import org.flywaydb.core.Flyway;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class ChannelIT {

    private static Flyway flyway;
    private static final Logger LOGGER = Logger.getLogger(GuildIT.class.getName());
    private static ChannelDAO channelDAO;
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

    private List<InternalVoiceChannel> voiceChannels = Arrays.asList(
            new InternalVoiceChannel("2", "101", "voice", true),
            new InternalVoiceChannel("6", "101", "afk-voice", true),
            new InternalVoiceChannel("7", "101", "the-other-voice", true),
            new InternalVoiceChannel("8", "102", "voice", true)
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

        channelDAO = new ChannelDAO(dataSource);
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

        for (InternalVoiceChannel c : voiceChannels) {
            voiceStatement.setString(1, c.getId());
            voiceStatement.setString(2, c.getGuildId());
            voiceStatement.setString(3, c.getName());

            voiceStatement.addBatch();
        }

        textStatement.executeBatch();
        voiceStatement.executeBatch();
        textStatement.close();
        voiceStatement.close();
        connection.close();
    }

    private void assertTextChannelEquals(InternalTextChannel expected, InternalTextChannel actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getGuildId(), actual.getGuildId());
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.isAnnouncmentChannel(), actual.isAnnouncmentChannel());
        assertEquals(expected.isCommandsEnabled(), actual.isCommandsEnabled());
        assertEquals(expected.isVoiceEnabled(), actual.isVoiceEnabled());
    }

    private void assertVoiceChannelEquals(InternalVoiceChannel expected, InternalVoiceChannel actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getGuildId(), actual.getGuildId());
        assertEquals(expected.isVoiceEnabled(), actual.isVoiceEnabled());
    }

    @Test
    public void testGetTextChannelById() throws SQLException {
        InternalTextChannel expected = textChannels.get(0);
        InternalTextChannel actual = channelDAO.getTextChannelForId(expected.getId());
        assertTextChannelEquals(expected, actual);
    }

    @Test
    public void testGetVoiceChannelById() throws SQLException {
        InternalVoiceChannel expected = voiceChannels.get(0);
        InternalVoiceChannel actual = channelDAO.getVoiceChannelForId(expected.getId());
        assertVoiceChannelEquals(expected, actual);
    }

    @Test
    public void testAddTextChannel() throws SQLException {
        InternalTextChannel expected = new InternalTextChannel("1000", "101", "channel-1", false, false, true, true);
        TextChannel textChannel = mock(TextChannel.class);
        Guild guild = mock(Guild.class);
        doReturn(guild).when(textChannel).getGuild();
        doReturn("101").when(guild).getId();
        doReturn("1000").when(textChannel).getId();
        doReturn("channel-1").when(textChannel).getName();

        channelDAO.addTextChannel(textChannel);

        InternalTextChannel actual = channelDAO.getTextChannelForId("1000");

        assertTextChannelEquals(expected, actual);
    }

    @Test
    public void testAddVoiceChannel() throws SQLException {
        InternalVoiceChannel expected = new InternalVoiceChannel("1001", "102", "new-channel", true);
        VoiceChannel voiceChannel = mock(VoiceChannel.class);
        Guild guild = mock(Guild.class);
        doReturn(guild).when(voiceChannel).getGuild();
        doReturn("102").when(guild).getId();
        doReturn("1001").when(voiceChannel).getId();
        doReturn("new-channel").when(voiceChannel).getName();

        channelDAO.addVoiceChannel(voiceChannel);

        InternalVoiceChannel actual = channelDAO.getVoiceChannelForId("1001");
        assertVoiceChannelEquals(expected, actual);
    }

    @Test
    public void testRemoveVoiceChannel() throws SQLException {
        VoiceChannel voiceChannel = mock(VoiceChannel.class);
        doReturn("6").when(voiceChannel).getId();

        channelDAO.removeVoiceChannel(voiceChannel);

        // Should be gone
        InternalVoiceChannel returned = channelDAO.getVoiceChannelForId("6");
        assertNull(returned);
    }

    @Test
    public void testRemoveTextChannel() throws SQLException {
        TextChannel textChannel = mock(TextChannel.class);
        doReturn("4").when(textChannel).getId();

        channelDAO.removeTextChannel(textChannel);

        InternalTextChannel returned = channelDAO.getTextChannelForId("4");
        assertNull(returned);
    }

    @Test
    public void testEnableAndDisableVoiceChannel() throws SQLException {
        Guild guild = mock(Guild.class);
        VoiceChannel channel = mock(VoiceChannel.class);

        doReturn(guild).when(channel).getGuild();
        doReturn("101").when(guild).getId();
        doReturn("2").when(channel).getId();
        doReturn("voice").when(channel).getName();

        InternalVoiceChannel returnedChannel = channelDAO.getVoiceChannelForId("2");
        assertTrue(returnedChannel.isVoiceEnabled());

        assertTrue(channelDAO.setVoiceChannelEnabled(channel, false));

        returnedChannel = channelDAO.getVoiceChannelForId("2");
        assertFalse(returnedChannel.isVoiceEnabled());

        assertTrue(channelDAO.setVoiceChannelEnabled(channel, true));

        returnedChannel = channelDAO.getVoiceChannelForId("2");
        assertTrue(returnedChannel.isVoiceEnabled());
    }

    @Test
    public void testGetVoiceChannelsForGuild() {
        List<InternalVoiceChannel> expectedChannels = voiceChannels.stream()
                .filter(k -> k.getGuildId().equals("101"))
                .collect(Collectors.toList());
        List<InternalVoiceChannel> returnedChannels = channelDAO.getVoiceChannelsForGuild("101");
        assertEquals(expectedChannels.size(), returnedChannels.size());

        assertTrue(returnedChannels.containsAll(expectedChannels));
    }

    @Test
    public void testGetTextChannelsForGuild() {
        List<InternalTextChannel> expectedChannels = textChannels.stream()
                .filter(c -> c.getGuildId().equals("102"))
                .collect(Collectors.toList());
        List<InternalTextChannel> returnedChannels = channelDAO.getTextChannelsForGuild("102");
        assertEquals(expectedChannels.size(), returnedChannels.size());

        assertTrue(returnedChannels.containsAll(expectedChannels));
    }

    @Test
    public void testGetVoiceChannelsForGuildDoesNotExist() {
        assertEquals(0, channelDAO.getVoiceChannelsForGuild("thisIsNotReal").size());
    }

    @Test
    public void testGetTextChannelsForGuildDoesNotExist() {
        assertEquals(0, channelDAO.getTextChannelsForGuild("thisIsNotReal").size());
    }
}
