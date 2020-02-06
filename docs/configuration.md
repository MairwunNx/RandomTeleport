# Configuration

## Configuration location

```none
.minecraft/config/Random-Teleport.json
```

## Configuration description

It configuration file store settings for random teleport mod

## Default configuration

```json
{
    "canTeleportOnTrees": true,
    "defaultRadius": 4096,
    "defaultAttempts": 1,
    "teleportStrategy": "KEEP_LOADED",
    "teleportOnCenterBlock": true,
    "interactWithEssentials": true,
    "locationRollBackTimer": 10,
    "minRandomTeleportRadius": 30
}
```

**Important note 1**: for fabric default value for `teleportStrategy` is `USUALLY_TELEPORT`.

**Important note 2**: one of able values `teleportStrategy` for fabric `SET_POSITION` can break backward compatibility with forge configuration! (But it will be fixed in next version).

**Important note 3**: values `teleportStrategy`: `KEEP_LOADED` and `ATTEMPT_TELEPORT` not work in fabric, but if you set it in fabric configuration then mod just forward it to `USUALLY_TELEPORT` teleport strategy.

**For fabric note**: property `interactWithEssentials` not exist in fabric configuration.

## Small documentation :)

Sorry for the very bad English, maybe you can fix it in Pull request.

| Property                   | Type             | Description      |
|---                         |---               |---               |
|`canTeleportOnTrees`        |`Boolean`         |If value false then you will never not teleport on trees.|
|`defaultRadius`             |`Int`             |Random teleport radius in blocks (for axis x and z).|
|`defaultAttempts`           |`Int`             |Attempts to teleport randomly. Attempts usings when random teleport failed by reason "unsafely place".|
|`teleportStrategy`          |`TeleportStrategy`|Teleport strategy. Available values **FOR FORGE**: [KEEP_LOADED, SET_AND_UPDATE, USUALLY_TELEPORT, ATTEMPT_TELEPORT], Available values **FOR FABRIC**: [USUALLY_TELEPORT, SET_AND_UPDATE, SET_POSITION], difference in teleportation function implementations. I will describe it later. But briefly, like this. **For forge**: KEEP_LOADED - Keeps chunks loaded for a while for you. SET_AND_UPDATE - Just sets a new position for the player and updates the data. USUALLY_TELEPORT - the most common teleport, it just teleports. ATTEMPT_TELEPORT - based on the name, an attempt will be made to teleport, and most likely, if you find yourself in blocks, this can somehow fix the situation. You can try each one and see what each one does and how it affects performance, I’m sure there will be different performance indicators.|
|`teleportOnCenterBlock`     |`Boolean`         |If value true then you'll be teleported on center block.|
|`interactWithEssentials`    |`Boolean`         |Enables compatibility with Project Essentials modules. **NOT EXIST IN FABRIC CONFIGURATION!**|
|`locationRollBackTimer`     |`Int`             |Location rollback timer.|
|`minRandomTeleportRadius`   |`Int`             |Minimal random teleport radius in blocks.|

## If you have any questions or encounter a problem, be sure to open an [issue](https://github.com/MairwunNx/RandomTeleport/issues/new/choose)