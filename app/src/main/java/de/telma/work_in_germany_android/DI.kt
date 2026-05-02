package de.telma.work_in_germany_android

import de.telma.work_in_germany_android.data.Repository
import de.telma.work_in_germany_android.data.RepositoryImpl
import de.telma.work_in_germany_android.domain.*
import de.telma.work_in_germany_android.network.RemoteDataSource
import de.telma.work_in_germany_android.network.RemoteDataSourceImpl
import de.telma.work_in_germany_android.storage.LocalDataSource
import de.telma.work_in_germany_android.storage.LocalDataSourceImpl
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
    single<LocalDataSource> { LocalDataSourceImpl() }
}

val networkModule = module {
    single<RemoteDataSource> { RemoteDataSourceImpl() }
}