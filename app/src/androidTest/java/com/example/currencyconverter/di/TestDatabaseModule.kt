package com.example.currencyconverter.di

import android.content.Context
import androidx.room.Room
import com.example.currencyconverter.db.Database
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

@TestInstallIn(components = [SingletonComponent::class], replaces = [DatabaseModule::class])
@Module
class TestDatabaseModule
{
    @Singleton
    @Provides
    fun provideTestDatabase(context: Context): Database
    {
        return Room.inMemoryDatabaseBuilder(
            context,
            Database::class.java
        ).allowMainThreadQueries().build()
    }
}