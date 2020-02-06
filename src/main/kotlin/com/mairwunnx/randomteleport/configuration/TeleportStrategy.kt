package com.mairwunnx.randomteleport.configuration

import kotlinx.serialization.Serializable

@Serializable
enum class TeleportStrategy {
    USUALLY_TELEPORT,
    SET_AND_UPDATE,
    SET_POSITION
}
