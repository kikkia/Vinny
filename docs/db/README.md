## DB docs

### Tables
#### Guild
A representation of a guild, mainly used to store guild specific settings  

| Fields | Type | PK | FK | Description |
| ------ | ---- | :-: | :-: | ------- |
| GuildID | varchar | X |  | The ID of the guild |
| MinVoiceRoleID | varchar | | | The ID of the min role to use a Voice command|
| MinNsfwRoleID | varchar | | | The ID of the min role to use an Nsfw command|
| MinModRoleID | varchar | | | The ID of the min role to use a Mod command|
| MinBaseRoleID | varchar | | | The ID of the min role to use a command|
| Prefix | varchar | | | The prefix for Vinny within the guild|

#### TextChannel
A representation of a channel used to store channel specific settings 

| Fields | Type | PK | FK | Description |
| ------ | ---- | :-: | :-: | ------- |
| ID | varchar | X | | The ID of the Channel |
| GuildID | varchar | | X | The ID of the guild the text channel is from |
| Enabled | tinyint | | | Whether or not Vinny is enabled in this channel |
| EnableNsfw | tinyint | | | Whether or not nsfw commands are enabled in this channel |
| EnableVoice | tinyint | | | Whether or not Voice commands are enabled in this channel |

#### User
Used to represent users, currently only used for playlists but will be scaled out to other user specific things.   

| Fields | Type | PK | FK | Description | 
| ------ | ---- | :-: | :-: | ------- |
| UserID | varchar | X | | The ID of the User |

#### GuildMembership
A join table between the Guilds and the users, allows us to track how many users are accross multiple servers  

| Fields | Type | PK | FK | Description |
| ------ | ---- | :-: | :-: | ------- |
| UserID | varchar |  | X | The ID of the User |
| GuildID | varchar |  | X | The ID of the guild |

#### Playlist 
A representation of a playlist, tied to user. Could also be tied to a guild???  

| Fields | Type | PK | FK | Description |
| ------ | ---- | :-: | :-: | ------- |
| UserID | varchar |  | X | The ID of the User |
| Songs | varchar | | | The urls of the songs split with , |
