package com.idunnololz.summitForLemmy.server.utils

import com.idunnololz.summitForLemmy.server.Config
import org.apache.commons.logging.Log
import org.slf4j.Logger
import kotlin.math.log

class DumbLogger(
    val logger: Logger
) {
    fun info(message: String) {
        logger.info(message)
    }

    fun debug(message: String) {
        if (Config.IS_DEBUG) {
            logger.debug(message)
        }
    }
}

fun Logger.toDumbLogger() =
    DumbLogger(this)