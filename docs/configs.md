# Config setup
Vinny now uses yaml config files. Your yaml config should be at `src/main/resources/config.yaml` or located at the root of the classpath and named `config.yaml`.

## Example config file
discordConfig:
  token: <DISCORD_BOT_TOKEN>
  botId: <DISCORD_BOT_CLIENT_ID>
  ownerId: <DISCORD_USER_ID_OF_OWNER>
databaseConfig:
  address: <DB_ADDRESS>
  username: <DB_USERNAME>
  password: <DB_PASSWORD>
  schema: <DB_SCHEMA>
shardingConfig:
  total: 1 
  localStart: 0 # Start index of shard to start for this process
  localEnd: 0 # End index of shard to start for this process
voiceConfig:
  nodes:
    - address: <LAVALINK_WEBSOCKET_ADDRESS>
      password: <LL_PASSWORD>
      region: <NODE_REGION> # NA,EU,etc
      name: <NODE_NAME>
  defaultSearchProvider: "" # default provider for search example: "scsearch:" soundcloud search
rssConfig:
  enable: "true" # Listen to NATS for rss events from Vinny-RSS service
  natsAddress: "nats://www.xxx.yyy.zzz:4222" # nats server address
  natsPassword: ""
  natsSubject: "" # NATS subject to listen to
thirdPartyConfig:
  p90Token: "" # token for p90 gif/webm api
  twitchClientId: ""
  pixivUser: ""
  pixivPass: ""
  sauceProxy: ""
  sauceToken: ""
  datadogHostname: "localhost"
  redditClientId: ""
  redditClientToken: ""
botConfig:
  enableScheduledCommands: "true"
  enableLoggingChannels: "true" # Enable logging errors and logs to discord webhooks defined below
  hostIdentifier: ""
  dataLoader: "false" # On bot start runs a job to sync database state with what discord state is, with lots of servers can be demanding
  silentDeploy: "false" # Deploy vinny silently (Only responds to owner commands)
  guildPrefsCacheTTL: 1200 # How long guild preferences live in cache in seconds
  onlineEmoji: "<:online:561655473179459614>" # Emoji for online status
  idleEmoji: "<:idle:561655480963956758>" # Emoji for idle status
  dndEmoji: "<:dnd:561655462991233054>" # Emoji for dnd status
  offlineEmoji: "<:offline:561655488006062098>" # Emoji for offline status
  debugWebhooks: [""] # List of webhook urls for debug logs
  infoWebhooks: [""] # List of webhook urls for info logs
  warningWebhooks: [""] # List of webhook urls for warning logs
  errorWebhooks: [""] # List of webhook urls for error logs
