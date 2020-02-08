@file:Suppress("unused")

package com.mairwunnx.randomteleport.configuration

import kotlinx.serialization.Serializable

@Serializable
enum class TeleportStrategy {
    USUALLY_TELEPORT,
    SET_AND_UPDATE, // For compatibility with fabric 1.14.4 configuration, just redirect on `USALLY_TELEPORT`.
    SET_POSITION,
    KEEP_LOADED, // For compatibility with forge configuration, just redirect on `USALLY_TELEPORT`.
    ATTEMPT_TELEPORT // For compatibility with forge configuration, just redirect on `USALLY_TELEPORT`.
}
