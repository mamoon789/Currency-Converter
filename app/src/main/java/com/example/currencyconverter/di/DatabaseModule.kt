package com.example.currencyconverter.di

import android.content.Context
import androidx.room.Room
import com.example.currencyconverter.db.Database
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class DatabaseModule
{
    @Singleton
    @Provides
    fun provideDatabase(context: Context): Database
    {
        return Room.databaseBuilder(context, Database::class.java, "database").build()
    }
}