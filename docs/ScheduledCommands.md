# Scheduled Commands
Scheduling commands allows you to setup Vinny to automatically run commands on a timer.

## Examples
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

## Time format
The format you tell Vinny is as follows:
`week:days:hours:minutes:seconds` -> `1:3:2:13:22` -> Run the command every 1 week, 3 days, 2 hours, 13 minutes and 22 seconds.

## Limits
Currently each user is allowed to have only 5 scheduled commands. To remove this limit you will need to donate to the project on [patreon](https://www.patreon.com/Kikkia)