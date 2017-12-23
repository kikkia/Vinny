# Vinny-Redux
[![Discord](https://img.shields.io/badge/Discord-Support-blue.svg)](https://discord.gg/XMwyzxZ)

A complete rewrite and overhaul of VinnyBot.

## Usage of code
Vinny is completly open-source under the MIT License. Feel free to use and modify any code as you see fit. Just make sure to mention where you got it from. ;)

## Contributing
If you want to help by suggesting a feature or update to Vinny the best way is to reach out to me. Either on Vinny's support sever or by making an issue on this repo.

1. Fork it!
2. Create your feature branch: `git checkout -b my-new-feature`
3. Make your changes
4. Commit your changes: `git commit -m 'Add some feature'`
5. Push to the branch: `git push origin my-new-feature`
6. Submit a pull request :D

Running Vinny-Redux locally requires Java 8+, Maven, and an internet connection.
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

The only mandatory fields in here are Discord, OwnerID and BotID. Leaving the others out will possibly disable some commands and spit out some non-fatal errors.
