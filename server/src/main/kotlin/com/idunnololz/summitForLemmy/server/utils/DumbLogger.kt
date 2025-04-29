package com.idunnololz.summitForLemmy.server.utils

import com.idunnololz.summitForLemmy.server.Config
import org.slf4j.Logger

class DumbLogger(
  val logger: Logger,
) {
  fun info(message: String) {
    logger.info(message)
  }

  fun debug(message: String) {
    if (Config.isDebug) {
      logger.debug(message)
    }
  }
}

fun Logger.toDumbLogger() = DumbLogger(this)
