.PHONY: run
run: ## Compile and run the module
        git pull origin master
        mvn package
        java -Dcom.sun.management.jmxmote -Dcom.sun.management.jmxremote.port=9010 -Dcom.sun.management.jmxremote.local.only=false -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -cp target/discord-bot-1.0-SNAPSHOT-jar-with-dependencies.jar com.bot.DiscordBot

