package com.example.core.di

import com.example.data.CommunicationLogDao
import com.example.data.FamilyMemoryDao
import com.example.data.QuickTemplateDao
import com.example.data.RelativeDao
import com.example.data.RelativeRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideRelativeRepository(
        relativeDao: RelativeDao,
        communicationLogDao: CommunicationLogDao,
        quickTemplateDao: QuickTemplateDao,
        familyMemoryDao: FamilyMemoryDao
    ): RelativeRepository {
        return RelativeRepository(
            relativeDao = relativeDao,
            communicationLogDao = communicationLogDao,
            quickTemplateDao = quickTemplateDao,
            familyMemoryDao = familyMemoryDao
        )
    }
}
