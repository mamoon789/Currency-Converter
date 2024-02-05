package com.example.currencyconverter.api

import com.example.currencyconverter.model.Currency
import com.example.currencyconverter.model.LatestRates
import retrofit2.Response
import retrofit2.http.GET

interface Api
{
    @GET("latest.json?app_id=3a32d73fccd64b1b939607e2e5c574e3")
    suspend fun getLatestRates(): Response<LatestRates>

    @GET("currencies.json")
    suspend fun getCurrencyList(): Response<List<Currency>>

}