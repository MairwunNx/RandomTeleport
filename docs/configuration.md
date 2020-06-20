# Configuration

Stores settings for this mod, located in `.<root>/config/random-teleport.json` and has a json file type.

## Default configuration

```json5
{
    // Hey! Important note: json not supports commentaries, remove this if you will copy this.
    "canTeleportOnTrees": true, // If value false then you will never not teleport on trees.
    "defaultRadius": 4096, // Random teleport radius in blocks (for axis x and z).
    "defaultAttempts": 1, // Attempts to teleport randomly. Attempts usings when random teleport failed by reason "unsafely place".
    /*
      Teleport strategy. 
        - Strategies for Forge: [KEEP_LOADED, SET_AND_UPDATE, USUALLY_TELEPORT, ATTEMPT_TELEPORT], 
        - Strategies for Fabric: [USUALLY_TELEPORT, SET_AND_UPDATE, SET_POSITION], 
        - Strategies for Fabric 1.15.2: [USUALLY_TELEPORT, SET_POSITION].

      Difference in teleportation function implementations. 
      I will describe it later. But briefly, like this. 
          For Forge: 
              - KEEP_LOADED - Keeps chunks loaded for a while for you. 
              - SET_AND_UPDATE - Just sets a new position for the player and updates the data. 
              - USUALLY_TELEPORT - the most common teleport, it just teleports. 
              - ATTEMPT_TELEPORT - based on the name, an attempt will be made to teleport, and most likely, if you find yourself in blocks, this can somehow fix the situation. 

      You can try each one and see what each one does and how it affects performance, I’m sure there will be different performance indicators.
    */
    "teleportStrategy": "KEEP_LOADED",
    "teleportOnCenterBlock": true, // If value of this property is true you'll teleported at block center.
    "locationRollBackTimer": 10, // Location rollback timer in seconds.
    "minRandomTeleportRadius": 30 // Minimal random teleport radius in blocks.
}
```

## Important notes: 

1. For fabric default value for `teleportStrategy` is `USUALLY_TELEPORT`.
2. One of able values `teleportStrategy` for fabric `SET_POSITION` can break backward compatibility with forge configuration! (But it will be fixed in next version).
3. Values `teleportStrategy`: `KEEP_LOADED` and `ATTEMPT_TELEPORT` not work in fabric, but if you set it in fabric configuration then mod just forward it to `USUALLY_TELEPORT` teleport strategy.
4. For fabric 1.15.2 note: value `teleportStrategy`: `SET_AND_UPDATE` not work in fabric, but if you set it in fabric configuration then mod just forward it to `USUALLY_TELEPORT` teleport strategy.

## If you have any questions or encounter a problem, be sure to open an [issue](https://github.com/MairwunNx/RandomTeleport/issues/new/choose)
