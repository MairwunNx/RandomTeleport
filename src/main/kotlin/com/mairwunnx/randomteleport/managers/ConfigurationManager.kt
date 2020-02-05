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

    @UseExperimental(UnstableDefault::class)
    private val jsonInstance = Json(
        JsonConfiguration(
            strictMode = false, allowStructuredMapKeys = true, prettyPrint = true
        )
    )

    fun load() {
        logger.info("Loading random teleport configuration")
        if (!File(configurationPath).exists()) {
            logger.warn("Random teleport config not exist! creating it now!")
            File(configurationDir).mkdirs()
            val defaultConfig = jsonInstance.stringify(
                ConfigurationModel.serializer(), configuration
            )
            File(configurationPath).writeText(defaultConfig)
        }
        configuration = jsonInstance.parse(
            ConfigurationModel.serializer(), File(configurationPath).readText()
        )
    }

    fun save() {
        logger.info("Saving random teleport configuration")
        File(configurationDir).mkdirs()
        val configurationRaw = jsonInstance.stringify(
            ConfigurationModel.serializer(), configuration
        )
        File(configurationPath).writeText(configurationRaw)
    }

    fun get() = configuration
}
