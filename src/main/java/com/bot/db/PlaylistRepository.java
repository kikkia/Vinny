package com.bot.db;

import com.bot.models.AudioTrack;
import com.bot.models.Playlist;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlaylistRepository {

	private Connection read;
	private Connection write;


	public void initialize() throws SQLException {
		ConnectionPool connectionPool = ConnectionPool.getInstance();
		ReadConnectionPool readConnectionPool = ReadConnectionPool.getInstance();

		DataSource dataSource = connectionPool.getDataSource();
		DataSource readDataSource = readConnectionPool.getDataSource();

		this.read = readDataSource.getConnection();
		this.write = dataSource.getConnection();

	}

	public List<Playlist> getPlaylistsForUser(String userId) {
		String query = "Select p.id, p.name, pt.position, t.url, t.title FROM playlist p LEFT JOIN playlist_track pt ON p.id = pt.playlist LEFT JOIN track t ON t.id = pt.track WHERE p.user_id = " + userId;
		Map<Integer, Playlist> playlists = null;
		PreparedStatement statement = null;
		ResultSet set = null;

		try {
			statement = read.prepareStatement(query);
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
					Playlist newPlaylist = new Playlist(playlistId, "" + userId, playlistName, tracks);
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

		if (playlists.values().isEmpty()) {
			return null;
		}

		return new ArrayList<>(playlists.values());

	}
//
//	public List<Playlist> getPlaylistsForGuild(String guildId) {
//
//	}
//
//	public Playlist getPlaylistForUserByName(String userId, String playlistName) {
//
//	}
//
//	public Playlist getPlaylistForUserById(String userId, int playlistId) {
//
//	}
//
//	public Playlist getPlaylistForGuildByName(String userId, String playlistName) {
//
//	}
//
//	public Playlist getPlaylistForGuildById(String userId, int playlistId) {
//
//	}
//
//	public boolean createPlaylistForUser(String userId, String name, List<QueuedAudioTrack> tracks) {
//
//	}
//
//	public boolean createPlaylistForGuild(String guildId, String name, List<QueuedAudioTrack> tracks) {
//
//	}
//
//	public boolean addTrackToPlaylistForUser(String userId, Playlist currentPlaylist, QueuedAudioTrack track, int index) {
//
//	}
//
//	public boolean addTrackToPlaylistForUserById(String userId, int playlistId, QueuedAudioTrack track, int index) {
//
//	}
//
//	public boolean addTrackToPlaylistForUserByName(String userId, String name, QueuedAudioTrack track, int index) {
//
//	}
//
//	public boolean addTrackToPlaylistForGuild(String guildId, Playlist currentPlaylist, QueuedAudioTrack track, int index) {
//
//	}
//
//	public boolean addTrackToPlaylistForGuildById(String guildId, int playlistId, QueuedAudioTrack track, int index) {
//
//	}
//
//	public boolean addTrackToPlaylistForGuildByName(String guildId, String name, QueuedAudioTrack track, int index) {
//
//
//	}
//
//	public boolean removeTrackFromUserPlaylistById(String userId, int playlistId, int trackIndexToRemove) {
//
//	}
//
//	public boolean removeTrackFromUserPlaylistByName(String userId, String playlistName, int trackIndexToRemove) {
//
//	}
//
//	public boolean removeTrackFromGuildPlaylistById(String guildId, int playlistId, int trackIndexToRemove) {
//
//	}
//
//	public boolean removeTrackFromGuildPlaylistByName(String guildId, String playlistName, int trackIndexToRemove) {
//
//	}

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
