package de.telma.work_in_germany_android

import de.telma.work_in_germany_android.data.Repository
import de.telma.work_in_germany_android.data.RepositoryImpl
import de.telma.work_in_germany_android.domain.FetchJobsUseCase
import org.koin.dsl.module

val uiModule = module {
}

val domainModule = module {
    single { FetchJobsUseCase(get()) }
}

val dataModule = module {
    single<Repository> { RepositoryImpl() }
}

val storageModule = module {
}

val networkModule = module {
}