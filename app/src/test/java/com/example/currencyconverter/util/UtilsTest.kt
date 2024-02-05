package com.example.currencyconverter.util

import com.example.currencyconverter.model.Currency
import com.example.currencyconverter.model.LatestRates
import com.example.currencyconverter.model.Rate
import org.junit.Assert.*

import org.junit.Test

class UtilsTest
{
    @Test
    fun testCalculateTimeDifferenceInMinutes_Successful(){
        val timestamp = System.currentTimeMillis() / 1000
        val result = Utils.calculateTimeDifferenceInMinutes(timestamp)
        assertTrue(result == 0L)
    }

    @Test
    fun testTransformLatestRatesToAddCurrency_Successful_CurrencyFound()
    {
        val latestRates = LatestRates("USD", listOf(Rate("USD", 1.0, "")), 101010)
        val currencyList = listOf(Currency("USD", "US Dollar"))
        Utils.transformLatestRatesToAddCurrency(latestRates, currencyList)
        assertTrue(latestRates.rates[0].name.isNotEmpty())
    }

    @Test
    fun testTransformLatestRatesToAddCurrency_UnSuccessful_CurrencyNotFound()
    {
        val latestRates = LatestRates("USD", listOf(Rate("USD", 1.0, "")), 101010)
        val currencyList = listOf(Currency("PKR", "Pakistani Rupee"))
        Utils.transformLatestRatesToAddCurrency(latestRates, currencyList)
        assertTrue(latestRates.rates[0].name.isEmpty())
    }

    @Test
    fun testTransformLatestRatesToAddCurrency_UnSuccessful_CurrencyFoundButEmpty()
    {
        val latestRates = LatestRates("USD", listOf(Rate("USD", 1.0, "")), 101010)
        val currencyList = listOf(Currency("USD", ""))
        Utils.transformLatestRatesToAddCurrency(latestRates, currencyList)
        assertTrue(latestRates.rates[0].name.isEmpty())
    }

    @Test()
    fun testTransformLatestRatesToAddCurrency_UnSuccessful_RatesEmpty()
    {
        val latestRates = LatestRates("USD", emptyList(), 101010)
        val currencyList = listOf(Currency("USD", "US Dollar"))
        Utils.transformLatestRatesToAddCurrency(latestRates, currencyList)
        assertTrue(latestRates.rates.isEmpty())
    }

    @Test
    fun testTransformLatestRatesToAddCurrency_UnSuccessful_CurrencyListEmpty()
    {
        val latestRates = LatestRates("USD", listOf(Rate("USD", 1.0, "")), 101010)
        Utils.transformLatestRatesToAddCurrency(latestRates, emptyList())
        assertTrue(latestRates.rates[0].name.isEmpty())
    }

    @Test
    fun testTransformLatestRatesToBaseCurrency_Successful_CurrencyFound()
    {
        val latestRates = LatestRates(
            "USD", listOf(
                Rate("USD", 1.0, ""),
                Rate("PKR", 278.0, "")
            ), 101010
        )
        Utils.transformLatestRatesToBaseCurrency(latestRates, "PKR")
        assertTrue(latestRates.base == "PKR")
    }

    @Test(expected = NullPointerException::class)
    fun testTransformLatestRatesToBaseCurrency_UnSuccessful_CurrencyNotFound()
    {
        val latestRates = LatestRates(
            "USD", listOf(
                Rate("USD", 1.0, ""),
                Rate("PKR", 278.0, "")
            ), 101010
        )
        Utils.transformLatestRatesToBaseCurrency(latestRates, "IND")
    }

    @Test(expected = NullPointerException::class)
    fun testTransformLatestRatesToBaseCurrency_UnSuccessful_CurrencyEmpty()
    {
        val latestRates = LatestRates(
            "USD", listOf(
                Rate("USD", 1.0, ""),
                Rate("PKR", 278.0, "")
            ), 101010
        )
        Utils.transformLatestRatesToBaseCurrency(latestRates, "")
    }

    @Test(expected = NullPointerException::class)
    fun testTransformLatestRatesToBaseCurrency_UnSuccessful_RatesEmpty()
    {
        val latestRates = LatestRates(
            "USD", emptyList(), 101010
        )
        Utils.transformLatestRatesToBaseCurrency(latestRates, "PKR")
    }
}