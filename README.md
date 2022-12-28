# MoreMcmeta Plugin Template

This is an example mod with Gradle set up to develop a MoreMcmeta plugin (or several plugins bundled into one mod).

To use the template with a different Minecraft version, simply change the properties in [gradle.properties](gradle.properties). The template is set up to populate the mod metadata with the Minecraft version automatically. You likely want to create a new branch for each Minecraft version you want to support.

Note that this template uses auto-published `SNAPSHOT` versions of a branch in the MoreMcmeta repository. This is desirable for developing default plugins since these snapshots exclude default plugins. However, if you are not developing a default plugin, you should use a stable release version and set the minimum MoreMcmeta version to at least 4.0.0 for your Minecraft verison.

## License
This template is licensed under [CC0](https://creativecommons.org/share-your-work/public-domain/cc0/). Remember to replace the license file in this repository if you don't want to license your mod under CC0.