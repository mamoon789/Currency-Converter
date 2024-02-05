package com.example.currencyconverter.api

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.currencyconverter.model.Currency
import com.example.currencyconverter.model.LatestRates
import com.example.currencyconverter.model.Rate

@Dao
interface Dao
{
    @Insert
    suspend fun insertCurrencyList(currencyList: List<Currency>)

    @Query("SELECT * FROM Currency")
    suspend fun getCurrencyList(): List<Currency>

    @Insert
    suspend fun insertLatestRates(latestRates: LatestRates)

    @Update
    suspend fun updateLatestRates(latestRates: LatestRates)

    @Query("SELECT * FROM LatestRates WHERE base=:base")
    suspend fun getLatestRates(base: String = "USD"): LatestRates?

    @Insert
    suspend fun insertRateList(rate: List<Rate>)

    @Update
    suspend fun updateRateList(rate: List<Rate>)

}