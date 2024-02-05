package com.example.currencyconverter.di

import android.app.Application
import android.content.Context
import com.example.currencyconverter.api.Api
import com.example.currencyconverter.db.Database
import com.example.currencyconverter.repo.Repository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class RepositoryModule
{
    @Singleton
    @Provides
    fun provideAppContext(application: Application): Context
    {
        return application.applicationContext
    }

    @Singleton
    @Provides
    fun provideRepository(context: Context, api: Api, db: Database): Repository
    {
        return Repository(context, api, db)
    }
}