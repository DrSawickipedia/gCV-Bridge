# gCV-Bridge [![Build Status](https://img.shields.io/github/workflow/status/N3FS/gCV-Bridge/Deploy%20to%20Bintray)](https://github.com/N3FS/gCV-Bridge/releases)
A Discord bridge plugin for [gChat-Velocity](https://github.com/N3FS/gChat).

- **Forked From** - <https://github.com/N3FS/gCV-Bridge/>
	- Don't bug the original developer with issues if you're using this fork, and make them [here](https://github.com/DrSawickipedia/gCV-Bridge/issues) instead
	- I make no promises that I'll actually fix them (especially not in a timely manner) since this was for my own personal servers, so follow the below instructions if you want to do it for yourself (feel free to still add a ticket in case I eventually get around to it)

## Usage
- [gChat-Velocity](https://github.com/N3FS/gChat) is still required on the proxy since this is an addon plugin for it
	- If you're using this fork then I may have made changes on [my own fork](https://github.com/DrSawickipedia/gChat) that are required
		- This can be added as a local dependency in gradle, and if you're experiencing problems then follow the below steps for adding a shaded version to the project
		- You'll likely need to build it yourself, but the process for that should be the same as here
	- Place in the proxy's `plugins` folder
		- this may seem obvious but i'm going to write extremely thorough documentation since i wanted to die after trying to get this to build for hours (i really need to learn gradle properly), so i'm gonna put *everything* i remember in here
		- also if you have no coding/java experience and don't wanna deal with building it yourself, then you can probably bug me with a 
	- Configure the plugin in `plugins/gcv-bridge/config.yml`
- A discord bot is necessary to actually communicate with discord
	- You don't need to actually code anything for that, just create an application at <https://discord.com/developers/applications>
	- After doing that, make sure to create a bot user for the application and put the token from that in `config.yml` on the proxy

## Build Notes:
If you're like me and don't have much Minecraft plugin development experience but still want to make tweaks to this + build it yourself, these are steps that I can confirm result in a `.jar` that can be dropped into a Velocity proxy's `plugins` folder and work:
- Use Java 11
	- Confirmed to work on Eclipse Adoptium's `jdk-11.0.15.10-hotspot`
- Create a shaded version of the jar when building using either:
	- `./gradlew.bat shadowJar`
	- IntelliJ's Gradle build configuration using the `shadowJar` option (make sure to import the repo as a Gradle project when adding to IntelliJ for the first time)
- Put built shaded versions of the following dependencies in `./src/main/resources/` (these are what I use, but newer versions may also work):
	- *note: i needed to build the first three dependencies myself using `shadowJar` as well*
	- `javacord-3.8.0.jar`
	- `discord-webhooks-0.8.2-all.jar`
	- `mcdiscordreserializer-4.4.0-SNAPSHOT.jar`
	- `placeholders-common-5.1-SNAPSHOT.jar`

**Extra Debugging Notes:**
- The plugin is unable to load due to missing classes/methods from some dependency:
	- Add the built `.jar` (if there are multiple options, pick the `-all` version) to `./src/main/resources/`
	- If you need to built it yourself, make sure to use `shadowJar` here as well
	- I really hope there is never a case where you have to do this recursively for a dependency as well, but java seems to be full of suffering so it's worth a check if the previous steps don't work
