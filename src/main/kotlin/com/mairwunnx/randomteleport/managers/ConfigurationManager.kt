package com.mairwunnx.randomteleport.managers

import com.mairwunnx.randomteleport.configuration.ConfigurationModel
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import org.apache.logging.log4j.LogManager
import java.io.File

object ConfigurationManager {
    private val configurationDir = File(".").absolutePath + File.separator + "config"
    private val configurationPath = configurationDir + File.separator + "random-teleport.json"

    private var configuration = ConfigurationModel()
    private val logger = LogManager.getLogger()

    @OptIn(UnstableDefault::class)
    private val jsonInstance = Json(
        JsonConfiguration(
            encodeDefaults = true,
            ignoreUnknownKeys = true,
            isLenient = false,
            serializeSpecialFloatingPointValues = false,
            allowStructuredMapKeys = true,
            prettyPrint = true,
            unquotedPrint = false,
            useArrayPolymorphism = false
        )
    )

    fun load() {
        logger.info("Loading random teleport configuration").also {
            if (File(configurationPath).exists()) {
                configuration = jsonInstance.parse(
                    ConfigurationModel.serializer(), File(configurationPath).readText()
                ); return
            }
        }.also { logger.warn("Random teleport config not exist! Will used default!") }
    }

    fun save() {
        logger.info("Saving random teleport configuration")
        val configurationRaw = jsonInstance.stringify(
            ConfigurationModel.serializer(), configuration
        )
        File(configurationPath).apply { mkdirs().also { writeText(configurationRaw) } }
    }

    fun get() = configuration
}
