package com.example.core.di

import android.content.Context
import com.example.data.AppDatabase
import com.example.data.CommunicationLogDao
import com.example.data.FamilyMemoryDao
import com.example.data.QuickTemplateDao
import com.example.data.RelativeDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    fun provideRelativeDao(database: AppDatabase): RelativeDao {
        return database.relativeDao()
    }

    @Provides
    fun provideCommunicationLogDao(database: AppDatabase): CommunicationLogDao {
        return database.communicationLogDao()
    }

    @Provides
    fun provideQuickTemplateDao(database: AppDatabase): QuickTemplateDao {
        return database.quickTemplateDao()
    }

    @Provides
    fun provideFamilyMemoryDao(database: AppDatabase): FamilyMemoryDao {
        return database.familyMemoryDao()
    }
}
