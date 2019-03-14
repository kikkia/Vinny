# Config file layout

Config files are used for the environment setup for Vinny. In the absense of these, then environment variables are used. This means everything from storing the Tokens you use to connect to services to database information.

Vinny uses a config file: `config.conf` stored at `./res/config/`. The setup looks like:  
```
***Key***
Value

***Key 2***
Value 2
```

To get one of these values in the code you will use the config.java class. As an example to get the Discord API token would look like:
```java
Config config = Config.getInstance();
String discordToken = config.getToken("Discord");
```
  
## config keys (Case Sensitive)
#### Required
- "DISCORD_TOKEN": Discord API token
- "REDDIT_TOKEN": Reddit Token (Required only for reddit commands)
- "REDDIT_CLIENT_ID": Client id needed for the reddit api
- "OWNER_ID": The id of the owner of the bot. Some commands are locked down to owner only.
- "DB_URI": URI to the DB you would like to use
- "DB_USERNAME": Username for the DB
- "DB_PASSWORD": Password for the DB
- "DB_SCHEMA": Schema to use on the DB
- "NUM_SHARDS": Number of shards to use the bot with. Shards split bot into x instances. Best practice is about 1 shard per 1k servers. 

#### Not Required
- "SILENT_DEPLOY": If enabled, no commands will be loaded, but all background events will run (Default: false)
- "PREFIX": Default prefix for the bot. (Default: @mention)
- "DATA_LOADER": If enabled, a background process will be started to rescan discord and rebuild/verify the database. (Default: false)
- "GUILD_PREFS_CACHE_MAX_ITEMS": Sets the max number of guilds that are cached at any given time. (Default: 500)
- "GUILD_PREFS_OBJECT_LIFETIME": How long (seconds) a guild in the cache will sit unaccessed before being cleaned up. (Default: 600)
- "GUILD_PREFS_CACHE_CLEANUP_INTERVAL": How long (seconds) between cleanup intervals. (Default: 120)
   
### TIP: Using the config is easier for use with an IDE. Whereas using environment variabled is easier when using Docker  
## Example `config.conf`:
```
***DISCORD_TOKEN***
<Token from discord>

***BOT_ID***
284199417047810049

***BOTLIST_API***
<Token>

***OWNER_ID***
124988914472583168

***SILENT_DEPLOY***
false

***DB_URI***
192.168.1.13:3306

***DB_USERNAME***
root

***DB_PASSWORD***
password1234

***DB_SCHEMA***
<name of a schema>

***NUM_SHARDS***
1

***REDDIT_TOKEN***
<Token from reddit>

***REDDIT_CLIENT_ID***
<ClientID from reddit>

```


### Example docker-compose.yml
```
version: '2'
services:
  vinny:
    build: .
    restart: always
    network_mode: host
    environment:
            - BOT_ID=<Bot ID here>
            - DB_PASSWORD=<DATABASE PASSWORD>
            - DB_SCHEMA=<DEFAULT DATABASE SCHEMA>
            - DB_URI=<DATABASE URI>
            - DB_USERNAME=<DATABASE USERNAME>
            - DISCORD_TOKEN=<Discord bot token>
            - NUM_SHARDS=<Number of shards (reccomended 1 per 1000 servers)>
            - OWNER_ID=<Owner ID>
            - DATA_LOADER=<True or false>
```
