package com.idunnololz.summitForLemmy.server

import dagger.Module
import dagger.Provides
import io.ktor.server.application.*

@Module
class ApplicationModule(val application: Application) {
    @Provides
    fun provideApplication() = application
}