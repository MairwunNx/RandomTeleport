package com.mairwunnx.randomteleport.configuration

import kotlinx.serialization.Serializable

@Serializable
data class ConfigurationModel(
    var canTeleportOnTrees: Boolean = true,
    var defaultRadius: Int = 4096,
    var defaultAttempts: Int = 1,
    var teleportStrategy: TeleportStrategy = TeleportStrategy.USUALLY_TELEPORT,
    var teleportOnCenterBlock: Boolean = true,
    var locationRollBackTimer: Int = 10,
    var minRandomTeleportRadius: Int = 30,
    var opLevelForRollBack: Int = 1,
    var opLevelForRandomTeleport: Int = 1
)
