package com.example.currencyconverter.di

import com.example.currencyconverter.model.Currency
import com.example.currencyconverter.deserializer.CurrencyDeserializer
import com.example.currencyconverter.model.LatestRates
import com.example.currencyconverter.deserializer.LatestRatesDeserializer
import com.example.currencyconverter.api.Api
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import okhttp3.mockwebserver.MockWebServer
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Type
import javax.inject.Singleton

@TestInstallIn(components = [SingletonComponent::class], replaces = [ApiModule::class])
@Module
class TestApiModule
{
    @Singleton
    @Provides
    fun provideMockWebServer(): MockWebServer{
        return MockWebServer()
    }

    @Singleton
    @Provides
    fun provideRetrofit(mockWebServer: MockWebServer): Retrofit
    {
        val typeOfT: Type =
            TypeToken.getParameterized(MutableList::class.java, Currency::class.java).type
        val gson = GsonBuilder()
            .registerTypeAdapter(typeOfT, CurrencyDeserializer())
            .registerTypeAdapter(LatestRates::class.java, LatestRatesDeserializer())
            .create()
        return Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Singleton
    @Provides
    fun provideApi(retrofit: Retrofit): Api
    {
        return retrofit.create(Api::class.java)
    }
}