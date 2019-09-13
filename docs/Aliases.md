# Aliases
### What is an alias?
Simply put, an alias is a way for you to invoke Vinny commands however you want to.

### How they work
There are 3 types of aliases (Guild, Channel, and user) Currently only Guild Aliases are implemented but the other 2 are coming soon.
An alias is a mapping from some trigger (A message) to a command. For example, you can set it up where saying `give me a meme` and Vinny
would run the command `~rr dank memes`. The possibilities are endless.

### Can I give the command custom input?
Yes, when setting the command that you want it to run. You can put %% in the command where you want everything typed after the trigger to go.


#### Hierarchy
When all types are implemented they will have a heirarchy. If you have 2 aliases, lets say one is a user, and the other is a guild. The guild will
override the user alias. The hierarchy is
1. Channel
2. Guild
3. User

#### Permissions
The way these work under the hood is just as if you sent the command. So it takes your permissions to evaluate when running the command.

#### Making an alias
To make an alias for the guild you use the command `~addgalias`. Vinny will then walk you through the steps to setup an alias.
