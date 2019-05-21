package com.bot.models;

public class AudioTrack {

	private String url;
	private String title;
	private int position;

	public AudioTrack(String url, String title, int position) {
		if (url == null) {
			throw new IllegalArgumentException("Track must have a url");
		}
		if (title == null) {
			throw new IllegalArgumentException("Track must have a title");
		}

		setTitle(title);
		setUrl(url);
		setPosition(position);
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}
}
