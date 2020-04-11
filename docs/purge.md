# Purge
The purge command is a very powerful tool to help you clean up channels. The command needs to take in at least one of these arguments to work: 
- `# between 2-1000` - How many messages to delete (Default 10)
- `bot`- Include this word in the message to delete messages only from bots (Note: behavior changes when including prefix)
- `prefix="whatever"` - When using the `bot` argument you can use this prefix argument to remove any human messages as well that start with this prefix.
- `"example"` - Incuding something in quotes will remove messages if they contain the thing in quotes (Note: use multiple of these to remove any message containing ONE or MORE of the provided quotes)
- `@user` - Only messages from this user will be removed (Can include multiple users)
- `userID` - Same as @user but this way they dont get a ping. (You can get the user id by right clicking on the user and selecting `copy Id`)

### Example usages:
- `~purge 100` - Remove the last 100 messages
- `~purge @Kikkia @TestUser 30` - Remove the last 30 messages from Kikkia or TestUser
- `~purge bot prefix="!" 100` - Removes the last 100 messages from bots and messages starting with ! (Bot commands for example)
- `~purge "test"` - Removes the last 10 messages containing `test`
- `~purge @Rythm prefix="!" 1000` - Removes last 1000 messages from Rythm and all Rythm commands using !

Aliases: `prune`  
NOTE: Messages over 2 weeks old cannot be purged.
