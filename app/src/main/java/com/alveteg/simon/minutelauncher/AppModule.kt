package com.alveteg.simon.minutelauncher

import android.content.Context
import androidx.room.Room
import com.alveteg.simon.minutelauncher.data.LauncherDAO
import com.alveteg.simon.minutelauncher.data.LauncherDatabase
import com.alveteg.simon.minutelauncher.data.LauncherRepository
import com.alveteg.simon.minutelauncher.data.PackageRepository
import com.alveteg.simon.minutelauncher.data.UsageRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

  @Provides
  @Singleton
  fun provideDatabase(@ApplicationContext appContext: Context): LauncherDatabase {
    return Room.databaseBuilder(
      appContext,
      LauncherDatabase::class.java,
      "launcher-database"
    ).fallbackToDestructiveMigration().build()
  }

  @Provides
  @Singleton
  fun provideDao(database: LauncherDatabase): LauncherDAO {
    return database.launcherDao()
  }

  @Provides
  @Singleton
  fun provideRepository(dao: LauncherDAO): LauncherRepository {
    return LauncherRepository(dao)
  }

  @Provides
  @Singleton
  fun providePackageRepository(context: Context): PackageRepository {
    return PackageRepository(context)
  }

  @Provides
  @Singleton
  fun provideUsageRepository(context: Context): UsageRepository {
    return UsageRepository(context)
  }

  @Provides
  fun provideContext(@ApplicationContext appContext: Context): Context {
    return appContext
  }
}