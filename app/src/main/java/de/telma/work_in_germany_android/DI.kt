package de.telma.work_in_germany_android

import de.telma.work_in_germany_android.data.Repository
import de.telma.work_in_germany_android.data.RepositoryImpl
import de.telma.work_in_germany_android.domain.*
import de.telma.work_in_germany_android.network.RemoteDataSource
import de.telma.work_in_germany_android.network.RemoteDataSourceImpl
import de.telma.work_in_germany_android.storage.LocalDataSource
import de.telma.work_in_germany_android.storage.LocalDataSourceImpl
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val uiModule = module {
}

val domainModule = module {
    factory<FetchAllJobsUseCase> { FetchAllJobsUseCase(get()) }
    factory<GetJobByIdUseCase> { GetJobByIdUseCase(get()) }
    factory<GetJobsByCategoryUseCase> { GetJobsByCategoryUseCase(get()) }
}

val dataModule = module {
    single<Repository> { RepositoryImpl(get(), get()) }
}

val storageModule = module {
    single {
        Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }
    }

    single<LocalDataSource> {
        LocalDataSourceImpl(
            rootDir = androidContext().filesDir,
            json = get()
        )
    }
}

val networkModule = module {
    single<HttpClient> {
        HttpClient(Android) {
            install(ContentNegotiation) {
                json(get(), ContentType.Text.Plain)
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 10_000L
            }
        }
    }
    single<RemoteDataSource> { RemoteDataSourceImpl(get()) }
}
