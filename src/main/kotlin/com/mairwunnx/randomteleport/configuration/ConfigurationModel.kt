package com.mairwunnx.randomteleport.configuration

import kotlinx.serialization.Serializable

@Serializable
data class ConfigurationModel(
    var canTeleportOnTrees: Boolean = true,
    var defaultRadius: Int = 4096,
    var defaultAttempts: Int = 1,
    var enableInCommandSettings: Boolean = false,
    var teleportStrategy: TeleportStrategy = TeleportStrategy.KEEP_LOADED,
    var teleportOnCenterBlock: Boolean = true,
    var interactWithEssentials: Boolean = true,
    var locationRollBackTimer: Int = 10,
    var maxCommandTeleportAttempts: Int = 10,
    var maxCommandTeleportRadius: Int = 10000,
    var minCommandTeleportRadius: Int = 30,
    var minRandomTeleportRadius: Int = 30
)
