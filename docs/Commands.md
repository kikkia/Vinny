# Table of contents
1. [About Commands](#About) 
2. [How to use commands](#How-to)
3. [Command Categories](#Categories) 
    * [Voice Commands](#Voice)
        * [Controls](#Controls)
        * [Custom Playlists](#Playlists)
    * [Reddit Commands](#Reddit)
    * [General Commands](#General)
    * [Nsfw Commands](#Nsfw)
    * [Meme Commands](#Meme)
    * [Mod Commands](#Mod)
    * [Alias Commands](#Aliases)
    * [Scheduling Commands](#Scheduling)
    * [Subscriptions](#Subscriptions)
3. [Role Permissions](#Permissions)

# About 
Vinny has many commands that can be run on either a server or in DMs. 

# How-to
Commands are run by typing a message in a channel that Vinny can see and post in. These messages comprise of two parts, the prefix (Default: `~` and `@Vinny`) and the command (For example `play`). You can set custom prefixes for any server.  
Some commands take in arguments. These arguments effect what the command does. For example the `~play` command can take in either a link to audio, a search. (Example `~play rickroll` or `~play <link to audio>`)  
Commands can also have `aliases`, which are just other names for the command you can use to invoke it. You can also schedule commands to run at
an interval. Using the scheduling feature, you can get all your Vinny automatically.

# Categories
Commands are organized by different categories. Most categories can be given a required minimum role needed to use them. 

---

## Voice
Voice commands are commands used to control aspects of Vinny's voice chat capabilities. Such as playing music, shuffling tracks, changing volume, etc.  
All voice commands require that you are in a voice channel. Some require Vinny to be playing audio. 
  
### Controls
Commands that allow you to play audio and modify the audio stream. 

### `play`
The play command is used to queue up a track for Vinny to play in a voice channel. You can use this command to play audio from many different sources (video, Twitch, Soundcloud, etc).  
  
* `~play Griz My friends and I` - Vinny will search for `Griz My friends and I` and queue up the first track it finds. 
* `~play <Link to audio>` - Vinny will play the track from the url.

### `search`
The search command is used to search for a song. Unlike searching with the play command Vinny will give you the choice to choose between 5 different tracks.  
* `~search Xylo afterlife` - Returns a list of 5 results for the search and allows you to add a reaction to pick one. 

### `nowplaying`
*Aliases*: `np`, `playing`  

Gives information about the current playing track.    
* `~nowplaying`
  
### `pause`
Pauses the currently playing audio. Does not clear the queue. 

### `resume`
Resumes the audio queue if it is currently paused.

### `skip`
Skips to the next track in the queue. (Only if there is a next track to skip to.)

### `remove`
Removes tracks from the current queue by either a given (position in queue, url, track name)
* `~remove 4` - Removes the 4th next track in the queue.
* `~remove track title` - Removes all tracks in the queue with the given title (Case insensitive)
* `~remove url` - Removes all tracks in the queue with the given url

### `repeat`
Sets the current queue to loop through all tracks, or the next track
* `~repeat` - Repeats the next track in the loop (Run command again or `repeat all` to turn it off)
* `~repeat all` - Loops over the whole queue continuously. (When track ends it gets added to the end.)

### `shuffle`
Shuffles all of the tracks in the current queue

### `volume`
Sets the volume of the voice output. With no arguments it displays the current volume. Volume can range between 1 and 200. 
* `~volume 50` - Sets the volume to 50%.

### `dvolume`
Sets the defualt volume for the server. Whenever Vinny starts playing audio, he will be set to the default volume. 
* `~dvolume 50` - Sets the default volume to 50%

### `lockvolume` or `lvol`
Locks or unlocks the volume for the playing stream. (Requires mod perms)

### `list`
*Aliases:* `playlist`, `lists`, `queue`  
List all of the current tracks in the queue. 

### `clearqueue`
Clears all tracks from the queue without stopping the now playing track

---
### Playlists
Make and load custom playlists for a server or for yourself. Playlists are used to save and load a list of tracks instantly. Playlists can either be for a guild, which allows any user with permission to load them, or for yourself. Which allows you to load them and save them on any server you and Vinny are on. 

### `myplaylists`
Lists all of your playlists  

### `loadmyplaylist`
Loads the specified playlist into the track queue. Takes either the id or name of a playlist.  
* `~loadmyplaylist playlistName` or `~loadmyplaylist 5`

### `savemyplaylist`
Saves the current track queue as a playlist that you can use on any server. You must specify a name to name the playlist.
* `~savemyplaylist test` - Saves a playlist of the current tracks with the name test. 
  
### `gplaylists`
Lists all of the playlists available on the server/guild

### `loadgplaylist`
Loads the specified playlist into the track queue. Takes either the id or name of a playlist.  
* `~loadgplaylist playlistName` or `~loadgplaylist 5`

### `savegplaylist`
Saves the current track queue as a playlist that any user can use on this server. You must specify a name to name the playlist.
* `~savegplaylist test` - Saves a playlist of the current tracks with the name test. 

### `removeplaylist`
Removes a guild or user playlist by either name or id
* `~removeplaylist playlistName` or `~removeplaylist 5`

## Reddit
Reddit commands can get new, hot and top posts from any subreddit. (NOTE: NSFW posts/subreddits require the channel to have nsfw enabled.)

### `rr`
Gives a random hot post from a given subreddit.
* `~rr gaming` - Gives a random hot post from r/gaming.

### `tr`
Gives a random top all time post from a given subreddit.
* `~tr gaming` - Gives a random top post from r/gaming.

### `nr`
Gives a random new post from a given subreddit.
* `~nr gaming` - Gives a random new post from r/gaming.

## General
General commands are commands that donlt fit in any other category.

### `info`
Information about Vinny

### `say`  
Vinny will repeat whatever you said
* `~say hey whats up?` - Vinny will say `hey whats up?`

### `vote`
Support Vinny by upvoting on bot lists.
 
### `invite`
Sends a link to invite the bot to your server.

### `review`
Leave Vinny a review, your feedback will help the bot grow and improve!

### `support`
Gives a link to the support server.
  
### `stats`
Gives a link to detailed vinny statistics.
  
### `ping`
Gets the ping from Vinny to discord.
  
### `roll`
Gives a random numbers from N-K (default 0-10)
* `~roll 10-40` - gives a random number between 10 and 40.
  
### `user`
Gives details about a user. NOTE: At the moment you cannot mention a user (to avoid ping spam). You must user a users ID (Right click on a user and select "copy id"). If left blank you get the info for youself.
* `~user 124988914472583168` - Gives the info for Kikkia#3782.
  
### `perms`
Gives the permissions for a user (Both discord perms and Vinny perms.) Same as the `user` command, you must user an ID, or leave it blank for yourself.
  
### `sinfo`
*Aliases*: `serverinfo`, `guildinfo`, `ginfo`  
Get info of the current server. 

### `games`
Lists all games being played on the server and the users playing them.

### `pixiv`
Get a random new image from pixiv, or a random image for a search term. 
* `~pixiv megumin` - Gives a pixiv post with that search term
* `~pixiv` - Gives a random new post from pixiv

## Nsfw
Nsfw commands are just that. They need the channel to be have nsfw enabled (both in discord and with the [`enablensfw` command](###`enablensfw`))

### `r34`
*Aliases*: `rule34`  
Gives rule 34 for given tags. (Tip: When doing a multiple word tag for example: `star wars`. Replace spaces with underscores. `star_wars`)
* `~r34 konosuba`

### `e621`
Searches e621 for given tags Cannot be scheduled for now.
* `~e621 something` - idk what you furries look at..
* `~e621 something another_thing` - 2 tag example, you can do up to 4 tags

### `4chan`
*Aliases*: `random4chan`, `r4chan`, `random4c`, `r4c`  
Returns a random thread from a given 4chan board.
`~4chan g` - Gives a thread from /g/.

## Meme
Meme commands are commands that are just truly shitposting. 

### `comment` 
Generates a message based on what a user or channel has posted to discord using a custom adaptation of a markov chain algortihm. The comments are better the more messages that Vinny can gather from the user or channel. 
* `~comment @kikkia` - Generates a comment for Kikkia.  
* `~comment #general` - Generates a comment from the #general channel.  

### `sauce`
*Aliases*: `source`
Gets the source for a linked or attached image. Great for finding where that meme originated from or where that pic you found came from.
* `~sauce <image link>` - Gets the source of the linked image
* `~sauce <image attached>` - Gets the source of the attached image

### `p90`
Gets a webm from p90.zone. You can leave it blank to get a random webm, or add in a word after to search. Searching requires nsfw to be enabled.
* `~p90 dank` - Gets a dank webm from P90

### `ascii`
Generates an ascii representation of a given message.
* `ascii ayy lmao` - Outputs ascii art of `ayy lmao`

### `kickroulette`
Want to try your luck? Get an invite back to the guild just in case. This command will allow you to play russian roulette. Lost the game, get kicked from the server. If you rejoin in the next 24 hrs you'll get your roles back.
* `~memeroulette`

### `memekick`
Want to kick someone, but want them back? This command kicks a user and sends them an invite to get back, it also will give them their roles back. (user needs kick_members permission)
* `~memekick @user_to_kick` - Meme kicks the user, works with multiple users.

### `shitpost`
nuff said.

### `clap`
Clap emojifies any input or the last message sent

### `copypasta`
Gives a copypasta

## Mod
Mod commands allow you to change Vinny's settings on the server among other thing. Any one that changes a settings falls under the ModRole permissions category.

### `settings`
Lists all of the required minimum roles for each category. 

### `baserole` 
Sets the minimum role required to use any command. (Mention a role, leave blank for @everyone)
* `~baserole @bot-role` - Sets the given role (if any) or above as required for any command. 

### `modrole` 
Sets the minimum role required to use commands that change Vinny settings. (Mention a role, leave blank for @everyone)
* `~modrole @mods` - Sets the given role (if any) or above as required for any command that changes Vinny settings. 

### `nsfwrole` 
Sets the minimum role required to use any nsfw commands. (Mention a role, leave blank for @everyone)
* `~nsfwrole @nsfw` - Sets the given role (if any) or above as required for nsfw commands. 

### `voicerole` 
Sets the minimum role required to use voice commands. (Mention a role, leave blank for @everyone)
* `~voicerole @dj` - Sets the given role (if any) or above as required for voice commands. 

### `enablensfw`
Enables nsfw commands on the channel it is run on.

### `disablensfw`
Disables nsfw commands on the channel it is run on.

### `addprefix`
Adds one or more prefixes to the custom prefixes for the server.
* `~addprefix <One or more custom prefixes, separated by spaces>` - Adds one or more custom prefixes to the server.

### `removeprefix`
Removes one or more prefixes from the servers custom prefixes. 
* `~removeprefix <One or more custom prefixes, separated by spaces>` - Removes one or more custom prefixes from the current custom prefixes.

### `prefixes`
Lists all custom prefixes on the server.

### `purge`
Bulk removes messages from a channel, for more help see [this page.](https://github.com/kikkia/Vinny/blob/master/docs/purge.md)


## Aliases
Aliases are ways to use vinny commands with whatever trigger you want as opposed to using the normal command prefixes.

### `addgalias`
Adds an alias for the guild that anyone can invoke. This command will walk you through the process.

### `aliases`
Shows all aliases you have access to.

### `removegalias`
Removes an alias from the guild. Example: `given your guild has an alias with the trigger lol`
* `~removegalias lol`

## Scheduling
You can schedule Vinny to run commands every X amount of time using these commands

### `schedule`
Setup a Scheduled command. Vinny will walk you trough setting them up when you use this command.

### `unschedule`
This command is used to remove scheduled commands. You must be able to run Mod commands, or be the author of the scheduled command.
* `~unschedule <id>`

### `scheduled`
This command can be used to get all scheduled commands on either the server, the channel or for yourself.
* `~scheduled me` - Gets all of your scheduled commands.
* `~scheduled g` - Gets all scheduled commands in the guild.
* `~scheduled c` - Gets all scheduled commands in the channel.

## Subscriptions 
Vinny allows you to setup subscriptions to various other places like twitter, reddit, 4chan, etc.
[You can find more info here](https://github.com/kikkia/Vinny/blob/master/docs/Subscriptions.md)

### `subscribetwitter`
This command is used to setup a subscription to a twitter user.

### `subscribeyt`
Use this command to subscribe to new video notifications for a youtube channel.

### `subscribechan`
This command is used to setup a subscription to a 4chan board.

### `subscribereddit`
This command is used to setup a subscription to a subreddit.

### `subscribetwitch`
This command is used to get notifications when someone goes live on twitch.

### `subscriptions`
This command gets all of the subscriptions for a channel, or for you.
* `~subscriptions me` - Get all subscriptions you have made
* `~subscriptions c` - Get all subscriptions for this channel

### `unsubscribe`
Removes a subscription of the given ID. You can find this ID from the `subscriptions` command
* `~unsubscribe <id>`

---
## Permissions
The way that Vinny does permissions on who can use what commands is by role hierarchy. You can see your servers role hierarchy in the roles settings screen. The higher the role on the list, the higher it is on the hierarchy. The role set as the minimum for a command category is just that, the minimum role a user needs to run that command. If they have a role higher on the list than the current minimum, they can also run the command. 
