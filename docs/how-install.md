# Installation instructions

**If you have forge** then for start the modification, you need installed Forge, it is desirable that the version matches the supported versions. You can download Forge from the [link](https://files.minecraftforge.net/maven/net/minecraftforge/forge/index_1.14.4.html).

**If you have fabric** then for start the modification, you need installed Fabric, it is desirable that the version matches the supported versions. You can download Fabric from the [link](https://fabricmc.net/use/) (click on **Vanilla**, select latest version and download .exe or .jar as executable file).

Move the downloaded mod to the `mods` folder (installation example below).

**If you have forge** then you can install useful dependencies (without a mod, game can start, but I can recommend using it)

**If you have fabric** then you NEED install mandatory dependencies (without a mod, game can't start)

**Downloads for Forge**: [Cooldown](https://github.com/ProjectEssentials/ProjectEssentials-Cooldown) · [Permissions](https://github.com/ProjectEssentials/ProjectEssentials-Permissions)

**Downloads for Fabric**: [Fabric Language Kotlin](https://www.curseforge.com/minecraft/mc-mods/fabric-language-kotlin/files) · [Fabric Api](https://www.curseforge.com/minecraft/mc-mods/fabric-api/files)

```none
const val MV = valueOf(your_minecraft_version)

.
├── assets
├── config
├── libraries
├── mods (that's how it should be)
│   ├── Project Essentials Cooldown-1.MV.X-1.X.X.jar (recommended, for forge only)
│   ├── Project Essentials Permissions-1.MV.X-1.X.X.jar (recommended, for forge only)
│   ├── fabric-language-kotlin-1.3.X+build.X.jar (mandatory, for fabric only)
│   ├── fabric-api-0.4.X+build.XXX-1.MV.jar (mandatory, for fabric only)
│   └── Random-Teleport-1.MV.X-1.X.X.jar
└── ...
```

**Only for forge:** Now try to start the game, go to the `mods` tab, if this modification is displayed in the `mods` tab, then the mod has been successfully installed

**Only for fabric:** For checking successfully installing mod, try execute any command, e.g `/rtp`, if you get any responce except `Command not found` then mod successfully installed.

## If you have any questions or encounter a problem, be sure to open an [issue](https://github.com/MairwunNx/RandomTeleport/issues/new/choose)