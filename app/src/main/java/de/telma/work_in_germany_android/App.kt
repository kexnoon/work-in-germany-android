package de.telma.work_in_germany_android

import android.app.Application
import org.koin.core.context.startKoin

class App: Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            modules(
                listOf(
                    uiModule,
                    dataModule,
                    domainModule,
                    storageModule,
                    networkModule
                )
            )
        }
    }
}