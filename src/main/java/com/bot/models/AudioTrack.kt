package com.bot.models

class AudioTrack(url: String?, title: String?, position: Int) {

    var url: String? = null
    var title: String? = null
    var position: Int = 0

    init {
        if (url == null) {
            throw IllegalArgumentException("Track must have a url")
        }
        if (title == null) {
            throw IllegalArgumentException("Track must have a title")
        }

        this.title = title
        this.url = url
        this.position = position
    }
}
