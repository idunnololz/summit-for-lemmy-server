package com.idunnololz.summitForLemmy.server

import com.idunnololz.summitForLemmy.server.network.NetworkModule
import dagger.Component
import javax.inject.Singleton

@Component(
  modules = [
    NetworkModule::class,
    ApplicationModule::class,
  ],
)
@Singleton
interface Server {
  fun serverInitializer(): ServerInitializer
}
