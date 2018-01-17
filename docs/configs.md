# Config file layout

Config files are used for the environment setup for Vinny. This means everything from storing the Tokens you use to connect to services to database information.

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
  
## config.conf keys (Case Sensitive)
- "DISCORD_TOKEN": Discord API token
- "REDDIT_TOKEN": Reddit Token
- "DB_URI": URI to the DB you would like to use
- "DB_USERNAME": Username for the DB
- "DB_PASSWORD": Password for the DB
- "DB_SCHEMA": Schema to use on the DB
- "USE_DB": Boolean whether or not to use the db. (True or False)
- "NUM_SHARDS": Number of shards to use the bot with. Shards split bot into x instances. Best practice is about 1 shard per 1k servers. 
- "USE_VOICE": True or False, whether to load the voice commands. 
TODO: ADD MORE

  
Example `config.conf`:
```
TODO: Example
```
