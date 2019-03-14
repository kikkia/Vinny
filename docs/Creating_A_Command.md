# Creating a new command

Commands in Vinny use a semi-custom command framework to handle permissions, parsing and execution. 

## 1. Choosing a category
Each command is required to have a category. These categories help partition commands and help determine what roles and permissions are needed to execute them.

### Current categories and sub-categories
- General: If it fits no where else, it goes here.
   - Reddit: Commands that deal with reddit.
   - Meme: Commands that are just here for the fun.
- Voice: Commands that control voice functionality.
- NSFW: Commands that display things that are NSFW.
- Moderation: Commands that deal with changing settings for a guild.

All commands must extend a category template command. Each category has one that automatically assigns specific settings. 

### Example of what a new Voice command would look like:
```java
public class MyNewCommand extends GeneralCommand {

    public MyNewCommand() {
        // Init here
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        // Code that runs on when the command is called
    }
}
```  
  
## 2. Setting up the command
Each command need to have some unique properties set. The main ones are `name`, `help`, and `arguments`.
`name`: Is the part of the command that comes after the prefix.
`help`: The description for the command displayed when the help command is used.
`arguments`: (Optional) Shows in the help command, used to describe what arguments the command takes.
  
### Example of an initialized command
```java
public class MyNewCommand extends GeneralCommand {

    public MyNewCommand() {
        this.name = "uppercase";
        this.help = "Takes an input and upercases all of it.";
        this.arguments = "<thing to uppercase>";
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        // Code that runs on when the command is called
    }
}
```  
  
  
## 3. Executing the command
The most important part of any command is the execution. The execution happens in the `execute` method. Where we are given a CommandEvent. This command event can be used for many things. 
Here is a basic example of taking the arguments given, uppercasing them and sending them back.

#### Metrics
All commands need to have some metrics recorded. To do this just copy/paste this as the first line in the execute method.
```java
metricsManager.markCommand(this, commandEvent.getAuthor(), commandEvent.getGuild());
``` 

#### Permissions
All commands are locked down based on permissions. To check the permissions, its as easy as making sure this is right after the metrics:
```java
// Check the permissions to do the command
if (!CommandPermissions.canExecuteCommand(this, commandEvent))
    return;
```
All together
```java
public class MyNewCommand extends GeneralCommand {

    public MyNewCommand() {
        this.name = "uppercase";
        this.help = "Takes an input and upercases all of it.";
        this.arguments = "<thing to uppercase>";
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        metricsManager.markCommand(this, commandEvent.getAuthor(), commandEvent.getGuild());
        // Check the permissions to do the command
        if (!CommandPermissions.canExecuteCommand(this, commandEvent))
            return;
            
        commandEvent.reply(commandEvent.getArgs().toUpperCase);
    }
}
```  

## 4. Registering the command
All commands must be registered with the command client. This is done in the ShardingManager.java class. Find the area of other commands from the category and put your command on the end. 
```java
// General Commands
new InviteCommand(),
new ShardStatsCommand(),
new PingCommand(),
new GetSettingsCommand(),
new PrefixesCommand(),
new RollCommand(),
new MyNewCommand(),
```
