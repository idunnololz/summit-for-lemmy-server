package com.idunnololz.summitForLemmy.server.network

import dagger.Module
import dagger.Provides
import javax.inject.Singleton
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

@Module
interface NetworkModule {

    companion object {
        @Singleton
        @Provides
        fun provideHttpClient() =
            HttpClient(CIO) {
                install(ContentNegotiation) {
                    json(Json {
                        prettyPrint = true
                        isLenient = true
                    })
                }
            }
    }
}