<!-- Plugins -->
[spigot]: https://www.spigotmc.org/resources/bungeetablistplus.313/
[PlaceholderAPI]: https://www.spigotmc.org/resources/placeholderapi.6245/

<!-- GitHub URLs -->
[Wiki]: https://github.com/CodeCrafter47/BungeeTabListPlus/wiki
[issues]: https://github.com/CodeCrafter47/BungeeTabListPlus/issues

<!-- Other URLs -->
[Dev Builds]: https://ci.codecrafter47.de/job/BungeeTabListPlus/
[Discord]: https://discord.gg/qYX5AyJ

# BungeeTabListPlus ![Sonatype Nexus (Releases)](https://img.shields.io/nexus/r/codecrafter47.bungeetablistplus/bungeetablistplus-api-bungee?label=BungeeTablistPlus&nexusVersion=3&server=http%3A%2F%2Fnexus.codecrafter47.de)
BungeeTabListPlus is a tab list plugin for BungeeCord, which provides highly customisable features including but not limited to:

- Custom slots with configurable text, icon and ping.
- Global tab list.
- Different layout options: group players by server, display staff separately, use a tab list of fixed or dynamic size depending on your needs.
- Display a different tab lists depending on various conditions, e.g. for a specific server or players with a specific permission.
- Out-of-the-Box support for many popular vanish plugins.
- Tons of placeholders to use within your tab lists.
  - Placeholders from [PlaceholderAPI] are supported too!
  - You can even create your own placeholders!

# Installation
Download the [latest release][spigot] ([Dev Builds]) and install it on your BungeeCord Server. Optionally (but recommended), install `BungeeTabListPlus_BukkitBridge` (included in the download) on all your Bukkit/ Spigot/ Paper servers.

After that, head over to the [Wiki] to learn about how to configure the plugin to your needs.

# Maven 
> Latest stable version can be found on top of this page. 
```xml
<repository>
    <id>codecrafter47-repo</id>
    <url>https://nexus.codecrafter47.de/content/repositories/public/</url>
</repository>
```

```xml
<!-- Bungee API -->
<dependency>
    <groupId>codecrafter47.bungeetablistplus</groupId>
    <artifactId>bungeetablistplus-api-bungee</artifactId>
    <version>VERSION</version>
</dependency>

<!-- Bukkit API -->
<dependency>
    <groupId>codecrafter47.bungeetablistplus</groupId>
    <artifactId>bungeetablistplus-api-bukkit</artifactId>
    <version>VERSION</version>
</dependency>

<!-- Sponge API -->
<dependency>
    <groupId>codecrafter47.bungeetablistplus</groupId>
    <artifactId>bungeetablistplus-api-sponge</artifactId>
    <version>VERSION</version>
</dependency>
```

# Support
If you have issues configuring the plugin, do not hesitate to [Join the Discord][Discord] for support.  
If you found a bug, head over to the [issues] tab and open a new issue.

# Building the plugin

Clone this repository using
```shell script
git clone --recursive https://github.com/CodeCrafter47/BungeeTabListPlus.git
cd BungeeTabListPlus
```

If you forgot the `--recursive` above or want to update your clone to the latest version run
```shell script
git submodule update --init --recursive
```

Build the plugin using gradle:
```shell script
./gradlew shadowJar
```
