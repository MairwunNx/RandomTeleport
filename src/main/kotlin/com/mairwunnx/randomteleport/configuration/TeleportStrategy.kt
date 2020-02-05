package com.mairwunnx.randomteleport.configuration

import kotlinx.serialization.Serializable

@Serializable
enum class TeleportStrategy {
    KEEP_LOADED,
    SET_AND_UPDATE,
    USUALLY_TELEPORT,
    ATTEMPT_TELEPORT
}
