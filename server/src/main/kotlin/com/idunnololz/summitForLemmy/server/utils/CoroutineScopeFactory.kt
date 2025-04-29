package com.idunnololz.summitForLemmy.server.utils

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

@Singleton
class CoroutineScopeFactory
@Inject
constructor() {
  fun create() = CoroutineScope(SupervisorJob() + Dispatchers.Default)
}
