package com.bot.db;

import com.bot.models.AudioTrack;
import com.bot.models.Playlist;
import com.bot.voice.QueuedAudioTrack;

import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PlaylistDAO {
    private static final Logger LOGGER = Logger.getLogger(PlaylistDAO.class.getName());

    private Connection read;
	private Connection write;
	private static PlaylistDAO instance;


	private PlaylistDAO() {
		try {
			initialize();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static PlaylistDAO getInstance() {
		if (instance == null)
			instance = new PlaylistDAO();
		return instance;
	}

	private void initialize() throws SQLException {
		this.read = ReadConnectionPool.getDataSource().getConnection();
		this.write = ConnectionPool.getDataSource().getConnection();
	}

	public List<Playlist> getPlaylistsForUser(String userId) {
		String query = "Select p.id, p.name, pt.position, t.url, t.title FROM playlist p LEFT JOIN playlist_track pt ON p.id = pt.playlist LEFT JOIN track t ON t.id = pt.track WHERE p.user_id = ?";
		// UserMembership helper method to get playlists
		return getPlaylistsFromQuery(userId, query);
	}
	public List<Playlist> getPlaylistsForGuild(String guildId) {
		String query = "Select p.id, p.name, pt.position, t.url, t.title FROM playlist p LEFT JOIN playlist_track pt ON p.id = pt.playlist LEFT JOIN track t ON t.id = pt.track WHERE p.guild = ?";
		// UserMembership helper method to get playlists
		return getPlaylistsFromQuery(guildId, query);
	}

	public Playlist getPlaylistForUserByName(String userId, String playlistName) {
		String query = "Select p.id, p.name, pt.position, t.url, t.title FROM playlist p LEFT JOIN playlist_track pt ON p.id = pt.playlist LEFT JOIN track t ON t.id = pt.track WHERE p.user_id = ? AND p.name = ?";
		// Kind of a hack, since only one playlist should be returned by the search we just take the first.
		try {
			return getPlaylistFromQueryWithName(userId, query, playlistName);
		}
		// Catches if no results returned then the list will be null, nullpointer will be caught and passed up.
		catch (SQLException e) {
			return null;
		}
	}

	public Playlist getPlaylistForUserById(String userId, int playlistId) {
		String query = "Select p.id, p.name, pt.position, t.url, t.title FROM playlist p LEFT JOIN playlist_track pt ON p.id = pt.playlist LEFT JOIN track t ON t.id = pt.track WHERE p.user_id = ? AND p.id = " + playlistId;
		// Kind of a hack, since only one playlist should be returned by the search we just take the first.
		try {
			return getPlaylistsFromQuery(userId, query).get(0);
		}
		// Catches if no results returned then the list will be null, nullpointer will be caught and passed up.
		catch (NullPointerException e) {
			return null;
		}
	}

	public Playlist getPlaylistForGuildByName(String guildId, String playlistName) {
		String query = "Select p.id, p.name, pt.position, t.url, t.title FROM playlist p LEFT JOIN playlist_track pt ON p.id = pt.playlist LEFT JOIN track t ON t.id = pt.track WHERE p.guild = ? AND p.name = ?";
		// Kind of a hack, since only one playlist should be returned by the search we just take the first.
		try {
			return getPlaylistFromQueryWithName(guildId, query, playlistName);
		}
		// Catches if no results returned then the list will be null, nullpointer will be caught and passed up.
		catch (SQLException e) {
			return null;
		}
	}

	public Playlist getPlaylistForGuildById(String guildId, int playlistId) {
		String query = "Select p.id, p.name, pt.position, t.url, t.title FROM playlist p LEFT JOIN playlist_track pt ON p.id = pt.playlist LEFT JOIN track t ON t.id = pt.track WHERE p.guild = ? AND p.id = " + playlistId;
		// Kind of a hack, since only one playlist should be returned by the search we just take the first.
		try {
			return getPlaylistsFromQuery(guildId, query).get(0);
		}
		// Catches if no results returned then the list will be null, nullpointer will be caught and passed up.
		catch (NullPointerException e) {
			return null;
		}
	}

	public boolean createPlaylistForUser(String userId, String name, List<QueuedAudioTrack> tracks) {
		String playlistInsertQuery = "INSERT INTO playlist (user_id, name) VALUES(?,?) ON DUPLICATE KEY UPDATE name = name";
		String trackInsertQuery = "INSERT INTO track (url, title) VALUES (?, ?) ON DUPLICATE KEY UPDATE title = title";
		String playlistTrackQuery = "INSERT INTO playlist_track (track, playlist, position) VALUES (?, ?, ?)";
		return addPlaylist(userId, name, tracks, playlistInsertQuery, trackInsertQuery, playlistTrackQuery);
	}

	public boolean createPlaylistForGuild(String guildId, String name, List<QueuedAudioTrack> tracks) {
		String playlistInsertQuery = "INSERT INTO playlist (guild, name) VALUES(?,?) ON DUPLICATE KEY UPDATE name = name";
		String trackInsertQuery = "INSERT INTO track (url, title) VALUES (?, ?) ON DUPLICATE KEY UPDATE title = title";
		String playlistTrackQuery = "INSERT INTO playlist_track (track, playlist, position) VALUES (?, ?, ?)";
		return addPlaylist(guildId, name, tracks, playlistInsertQuery, trackInsertQuery, playlistTrackQuery);
	}

	public boolean addTrackToPlaylistForUserById(String userId, int playlistId, QueuedAudioTrack track) {
		String addTrackQuery = "INSERT INTO track (url, title) VALUES (?, ?) ON DUPLICATE KEY UPDATE title = title";
		String insertPlaylistTrackQuery = "INSERT INTO playlist_track (playlist, track, position) VALUES (?, ?, ?)";
		String getNumTracksQuery = "SELECT COUNT(*) FROM playlist_track WHERE playlist = " + playlistId + " AND p.user_id = " + userId;
		return addTrackToPlayList(userId, null, playlistId, track, addTrackQuery, insertPlaylistTrackQuery, getNumTracksQuery);
	}

	public boolean addTrackToPlaylistForUserByName(String userId, String name, QueuedAudioTrack track) {
		String addTrackQuery = "INSERT INTO track (url, title) VALUES (?, ?) ON DUPLICATE KEY UPDATE title = title";
		String insertPlaylistTrackQuery = "INSERT INTO playlist_track (playlist, track, position) VALUES (?, ?, ?)";
		String getNumTracksQuery = "SELECT COUNT(*) FROM playlist_track pt LEFT JOIN playlist p ON p.id = pt.playlist WHERE p.name = \"" + name + "\" AND p.user_id = " + userId;
		// TODO: Implement later
		return false;
	}

	public boolean addTrackToPlaylistForGuildById(String guildId, int playlistId, QueuedAudioTrack track) {
		String addTrackQuery = "INSERT INTO track (url, title) VALUES (?, ?) ON DUPLICATE KEY UPDATE title = title";
		String insertPlaylistTrackQuery = "INSERT INTO playlist_track (playlist, track, position) VALUES (?, ?, ?)";
		String getNumTracksQuery = "SELECT COUNT(*) FROM playlist_track WHERE playlist = " + playlistId + " AND p.guild_id = " + guildId;
		return addTrackToPlayList(guildId, null, playlistId, track, addTrackQuery, insertPlaylistTrackQuery, getNumTracksQuery);
	}

	public boolean addTrackToPlaylistForGuildByName(String guildId, String name, QueuedAudioTrack track) {
		String addTrackQuery = "INSERT INTO track (url, title) VALUES (?, ?) ON DUPLICATE KEY UPDATE title = title\"";
		String insertPlaylistTrackQuery = "INSERT INTO playlist_track (playlist, track, position) VALUES (?, ?, ?)";
		String getNumTracksQuery = "SELECT COUNT(*) FROM playlist_track pt LEFT JOIN playlist p ON p.id = pt.playlist WHERE p.name = \"" + name + "\" AND p.user_id = " + guildId;
		// TODO: Implement later
		return false;
	}

	public boolean removeTrackFromUserPlaylistById(String userId, int playlistId, int trackIndexToRemove) {
		// TODO
		return false;
	}

	public boolean removeTrackFromUserPlaylistByName(String userId, String playlistName, int trackIndexToRemove) {
		// TODO: Implement later
		return false;
	}

	public boolean removeTrackFromGuildPlaylistById(String guildId, int playlistId, int trackIndexToRemove) {
		// TODO
		return false;
	}

	public boolean removeTrackFromGuildPlaylistByName(String guildId, String playlistName, int trackIndexToRemove) {
		// TODO: Implement later
		return false;
	}

	private List<Playlist> getPlaylistsFromQuery(String ownerId, String query) {
		Map<Integer, Playlist> playlists = null;
		PreparedStatement statement = null;
		ResultSet set = null;

		try {
			statement = read.prepareStatement(query);
			statement.setString(1, ownerId);
			set = statement.executeQuery();
			playlists = new HashMap<>();
			while (set.next()) {
				int playlistId = set.getInt("id");
				int trackPosition = set.getInt("position");
				String playlistName = set.getString("name");
				String trackUrl = set.getString("url");
				String trackTitle = set.getString("title");

				Playlist playlist = playlists.get(playlistId);
				if (playlist == null) {
					// Since each row is an audio track add it to the first of the list
					List<AudioTrack> tracks = new ArrayList<>();
					tracks.add(new AudioTrack(trackUrl, trackTitle, trackPosition));
					// Create new Playlist to add to the map of received playlists
					Playlist newPlaylist = new Playlist(playlistId, ownerId, playlistName, tracks);
					playlists.put(playlistId, newPlaylist);
				}
				else {
					playlist.addTrack(new AudioTrack(trackUrl, trackTitle, trackPosition));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(statement, set);
		}

		return new ArrayList<>(playlists.values());
	}

	private Playlist getPlaylistFromQueryWithName(String ownerId, String query, String name) throws SQLException {
		PreparedStatement statement = null;
		ResultSet set = null;

		int playlistId = 0;
		String playlistName = null;
		List<AudioTrack> tracks = new ArrayList<>();

		try {
			statement = read.prepareStatement(query);
			statement.setString(1, ownerId);
			statement.setString(2, name);
			set = statement.executeQuery();

			while (set.next()) {
				playlistId = set.getInt("id");
				int trackPosition = set.getInt("position");
				playlistName = set.getString("name");
				String trackUrl = set.getString("url");
				String trackTitle = set.getString("title");


				tracks.add(new AudioTrack(trackUrl, trackTitle, trackPosition));
			}


		} finally {
			close(statement, set);
		}

		return new Playlist(playlistId, ownerId, playlistName, tracks);
	}

	private boolean addPlaylist(String ownerId, String name, List<QueuedAudioTrack> tracks, String playlistQuery, String trackQuery, String playlistTrackQuery) {
		PreparedStatement statement = null;
		ResultSet set = null;

		try {
			statement = write.prepareStatement(playlistQuery, Statement.RETURN_GENERATED_KEYS);
			statement.setString(1, ownerId);
			statement.setString(2, name);
			statement.execute();
			// Get the generated ID for the playlist since we need it for the join table
			set = statement.getGeneratedKeys();
			if (!set.next()) {
				LOGGER.log(Level.WARNING, "Failed to add playlist, result set had no next");
				return false;
			}
			int playlistId = set.getInt(1);

			for (int i = 0; i < tracks.size(); i++) {
				QueuedAudioTrack t = tracks.get(i);
				statement = write.prepareStatement(trackQuery, Statement.RETURN_GENERATED_KEYS);
				statement.setString(1, t.getTrack().getInfo().uri);
				statement.setString(2, t.getTrack().getInfo().title);
				statement.execute();
				set = statement.getGeneratedKeys();
				set.next();
				int trackId;
				// We need to check the value returned. If the track is present no generated key returned
				// then we can just look up the id
				try {
					trackId = set.getInt(1);
				} catch (SQLException e) {
					// if we hit an error writing the track then we can get the id another way
					statement = read.prepareStatement("SELECT * FROM track t WHERE t.url = \"" + t.getTrack().getInfo().uri + "\"");
					statement.execute();
					set = statement.getResultSet();
					set.next();
					trackId = set.getInt(1);
				}

				// Write the track -> playlist joins
				statement = write.prepareStatement(playlistTrackQuery);
				statement.setInt(1, trackId);
				statement.setInt(2, playlistId);
				statement.setInt(3, i);
				statement.execute();
			}
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		} finally {
			close(statement, set);
		}
	}

	private boolean addTrackToPlayList(String ownerId, String playlistName, int playlistId, QueuedAudioTrack track,
									   String addTrackQuery, String insertPlaylistQuery, String numTracksQuery) {
		PreparedStatement statement = null;
		ResultSet set = null;

		try {
			statement = write.prepareStatement(addTrackQuery, Statement.RETURN_GENERATED_KEYS);
			statement.setString(1, track.getTrack().getInfo().uri);
			statement.setString(2, track.getTrack().getInfo().title);
			statement.execute();
			set = statement.getGeneratedKeys();
			set.next();
			int trackId = set.getInt(1);

			statement = read.prepareStatement(numTracksQuery);
			statement.execute();
			set = statement.getResultSet();
			set.next();
			int trackPosition = set.getInt(1);

			statement = write.prepareStatement(insertPlaylistQuery);
			statement.setInt(1, trackId);
			statement.setInt(2, playlistId);
			statement.setInt(3, trackPosition + 1);
			statement.execute();

			return true;
		}
		catch (SQLException e) {
			e.printStackTrace();
			return false;
		} finally {
			close(statement, set);
		}
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

	// A basic method that verifies whether the repo can currently query the db. Used for monitoring.
	public boolean healthCheck() {
		PreparedStatement statement = null;
		ResultSet set = null;
		try {
			statement = read.prepareStatement("SELECT * FROM playlist");
			set = statement.executeQuery();
			set.first();
			return true;
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return false;
		} finally {
			close(statement, set);
		}
	}
}
