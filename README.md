# Vinny-Redux
[![Discord](https://img.shields.io/badge/Discord-Support-blue.svg)](https://discord.gg/XMwyzxZ)
![Build](https://travis-ci.org/JessWalters/Vinny-Redux.svg?branch=master)

### Click [here](https://discordapp.com/oauth2/authorize?client_id=276855867796881408&scope=bot&permissions=523365751) to add Vinny to your server.  
  
A complete rewrite and overhaul of VinnyBot.

## Usage of code
Vinny is completly open-source under the MIT License. Feel free to use and modify any code as you see fit. Just make sure to mention where you got it from. ;)

## Contributing
If you want to help by suggesting a feature or update to Vinny the best way is to reach out to me. Either on Vinny's support sever or by making an issue on this repo.

I am in the process of writing some guides to help with the process. I will put them in where relevent.

1. Fork it!
2. Create your feature branch: `git checkout -b my-new-feature`
3. Make your changes [Create a command guide](docs/Creating_A_Command.md)
4. Commit your changes: `git commit -m 'Add some feature'`
5. Push to the branch: `git push origin my-new-feature`
6. Submit a pull request :D

### Dealing with configuration of the bot
[Config guide](docs/configs.md)

### Running locally with docker-compose.
1. Complete the docker-compose file with the appropriate denvironment variables. 
2. `docker-compose up vinny`

### Running Vinny-Redux locally without docker requires Java 8+ and Maven.
1. Clone repo
2. CD into the directory made by cloning. 
3. Create a file called tokens.txt at res/config/ 
4. Make the file look like:
```
***Discord***   
<Discord bot Oauth2 Token>  (You need to register a bot on https://discordapp.com/developers/applications/me)  
***Bot API***  
<bots.discord.pw Token>   
***Bot ID***  
<BotID>  (This also comes from https://discordapp.com/developers/applications/me)  
***Reddit***  
<Reddit client secret token> (Retrieved from registering a bot with reddits API)  
***OwnerID***  
<Discord ID for the owner of the bot> (Your discord ID)
```
Alternatively you can use ENV variables with the same field names (without *)

The only mandatory fields in here are Discord, OwnerID and BotID. Leaving the others out will possibly disable some commands and spit out some non-fatal errors.  
