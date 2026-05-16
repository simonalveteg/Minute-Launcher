package com.alveteg.simon.minutelauncher

import android.content.Context
import androidx.room.Room
import com.alveteg.simon.minutelauncher.data.ApplicationRepository
import com.alveteg.simon.minutelauncher.data.LauncherDao
import com.alveteg.simon.minutelauncher.data.LauncherDatabase
import com.alveteg.simon.minutelauncher.data.LauncherRepository
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
  fun provideDatabase(
    @ApplicationContext appContext: Context,
  ): LauncherDatabase {
    return Room.databaseBuilder(
      appContext,
      LauncherDatabase::class.java,
      "launcher-database"
    )
      .addMigrations(
        LauncherDatabase.MIGRATION_1_2,
        LauncherDatabase.MIGRATION_2_3
      )
      .build()
  }

  @Provides
  @Singleton
  fun provideLauncherDao(database: LauncherDatabase): LauncherDao {
    return database.launcherDao()
  }


  @Provides
  @Singleton
  fun provideRepository(
    dao: LauncherDao,
  ): LauncherRepository {
    return LauncherRepository(dao)
  }

  @Provides
  @Singleton
  fun provideApplicationRepository(context: Context): ApplicationRepository {
    return ApplicationRepository(context)
  }

  @Provides
  fun provideContext(@ApplicationContext appContext: Context): Context {
    return appContext
  }
}