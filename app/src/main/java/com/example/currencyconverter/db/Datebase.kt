package com.example.currencyconverter.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.currencyconverter.model.Currency
import com.example.currencyconverter.model.Rate
import com.example.currencyconverter.model.LatestRates
import com.example.currencyconverter.api.Dao
import com.google.gson.Gson

@Database(entities = [LatestRates::class, Currency::class], version = 1)
@TypeConverters(Converter::class)
abstract class Database : RoomDatabase()
{
    abstract fun dao(): Dao
}

class Converter()
{
    @TypeConverter
    fun fromListToString(rateList: List<Rate>): String
    {
        return Gson().toJson(rateList)
    }

    @TypeConverter
    fun fromStringToList(rateList: String): List<Rate>
    {
        return Gson().fromJson(rateList, Array<Rate>::class.java).asList()
    }
}