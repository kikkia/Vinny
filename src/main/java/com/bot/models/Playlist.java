package com.bot.models;

import java.util.List;
import java.util.logging.Logger;

public class Playlist {
	private static final Logger LOGGER = Logger.getLogger(Playlist.class.getName());

	private int id;
	private String ownerID;
	private String Name;
	private List<AudioTrack> tracks;

	public Playlist(int id, String ownerID, String name, List<AudioTrack> tracks) {
		if (id < 0) {
			throw new IllegalArgumentException("ID must be set");
		}
		if (ownerID == null){
			throw new IllegalArgumentException("ownerID must be set");
		}
		if (name == null) {
			throw new IllegalArgumentException("name must be set");
		}
		if (tracks == null) {
			throw new IllegalArgumentException("tracks must be set");
		}

		setId(id);
		setName(name);
		setOwnerID(ownerID);
		setTracks(tracks);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getOwnerID() {
		return ownerID;
	}

	public void setOwnerID(String ownerID) {
		this.ownerID = ownerID;
	}

	public String getName() {
		return Name;
	}

	public void setName(String name) {
		Name = name;
	}

	public List<AudioTrack> getTracks() {
		return tracks;
	}

	public void setTracks(List<AudioTrack> tracks) {
		this.tracks = tracks;
	}

	public void addTrack(AudioTrack track) {
		for (int i = 0; i < tracks.size(); i++) {
			// if track.position is less than a given index then track belongs earlier in the order so insert it at the current index.
			if (tracks.get(i).getPosition() > track.getPosition()) {
				tracks.add(i, track);
				return;
			}
		}
		// track has biggest value, add to end
		tracks.add(track);
	}

}
