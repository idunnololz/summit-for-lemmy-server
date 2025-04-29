package com.idunnololz.summitForLemmy.server.network

import dagger.Module
import dagger.Provides
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import javax.inject.Singleton
import kotlinx.serialization.json.Json

@Module
interface NetworkModule {
  companion object {
    @Singleton
    @Provides
    fun provideHttpClient() = HttpClient(CIO) {
      install(ContentNegotiation) {
        json(
          Json {
            prettyPrint = true
            isLenient = true
          },
        )
      }
    }
  }
}
