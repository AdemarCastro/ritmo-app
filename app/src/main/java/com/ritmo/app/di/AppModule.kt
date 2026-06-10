package com.ritmo.app.di

import android.content.Context
import androidx.room.Room
import com.ritmo.app.data.DateProvider
import com.ritmo.app.data.DefaultHabitRepository
import com.ritmo.app.data.HabitRepository
import com.ritmo.app.data.SystemDateProvider
import com.ritmo.app.data.local.HabitDao
import com.ritmo.app.data.local.RitmoDatabase
import com.ritmo.app.data.network.FakeHabitNetworkDataSource
import com.ritmo.app.data.network.HabitNetworkDataSource
import com.ritmo.app.data.preferences.DataStoreUserPreferencesRepository
import com.ritmo.app.data.preferences.UserPreferencesRepository
import com.ritmo.app.data.sync.SyncScheduler
import com.ritmo.app.data.sync.WorkManagerSyncScheduler
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class IoDispatcher

@Module
@InstallIn(SingletonComponent::class)
abstract class AppBindsModule {

    @Binds
    @Singleton
    abstract fun bindHabitRepository(repository: DefaultHabitRepository): HabitRepository

    @Binds
    @Singleton
    abstract fun bindNetworkDataSource(dataSource: FakeHabitNetworkDataSource): HabitNetworkDataSource

    @Binds
    @Singleton
    abstract fun bindSyncScheduler(scheduler: WorkManagerSyncScheduler): SyncScheduler

    @Binds
    @Singleton
    abstract fun bindDateProvider(provider: SystemDateProvider): DateProvider

    @Binds
    @Singleton
    abstract fun bindUserPreferencesRepository(
        repository: DataStoreUserPreferencesRepository,
    ): UserPreferencesRepository
}

@Module
@InstallIn(SingletonComponent::class)
object AppProvidesModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): RitmoDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            RitmoDatabase::class.java,
            "Ritmo.db",
        ).build()
    }

    @Provides
    fun provideHabitDao(database: RitmoDatabase): HabitDao = database.habitDao()

    @Provides
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
}
