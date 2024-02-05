package com.example.currencyconverter.di

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.currencyconverter.api.Api
import com.example.currencyconverter.db.Database
import com.example.currencyconverter.repo.Repository
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

@TestInstallIn(components = [SingletonComponent::class], replaces = [RepositoryModule::class])
@Module
class TestRepositoryModule
{
    @Singleton
    @Provides
    fun provideAppContext(): Context
    {
        return ApplicationProvider.getApplicationContext()
    }

    @Singleton
    @Provides
    fun provideRepository(context: Context, api: Api, db: Database): Repository{
        return Repository(context,api,db)
    }
}