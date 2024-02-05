package com.example.currencyconverter.api

import com.example.currencyconverter.db.Database
import com.example.currencyconverter.model.Currency
import com.example.currencyconverter.model.LatestRates
import com.example.currencyconverter.model.Rate
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class DaoTest
{
    @get:Rule
    val hiltAndroidRule = HiltAndroidRule(this)

    @Inject
    lateinit var db: Database

    @Before
    fun setUp()
    {
        hiltAndroidRule.inject()
    }

    @Test
    fun insertCurrencyList_Successful_RecordFound() = runTest {
        val currencyList = listOf(Currency("USD", "US Dollar"))
        db.dao().insertCurrencyList(currencyList)
        assertTrue(db.dao().getCurrencyList().size == 1)
    }

    @Test
    fun insertLatestRates_Successful_RecordFound() = runTest {
        val latestRates = LatestRates("USD", listOf(Rate("USD", 1.0, "")), 10000)
        db.dao().insertLatestRates(latestRates)
        assertTrue(db.dao().getLatestRates("USD") != null)
    }

    @Test
    fun updateLatestRates_Successful_RecordFound() = runTest {
        val latestRates = LatestRates("USD", listOf(Rate("USD", 1.0, "")), 10000)
        db.dao().insertLatestRates(latestRates)
        latestRates.timestamp = 20000
        db.dao().updateLatestRates(latestRates)
        assertTrue(db.dao().getLatestRates("USD")!!.timestamp == 20000L)
    }

    @Test
    fun updateLatestRates_UnSuccessful_RecordNotFound() = runTest {
        val latestRates1 = LatestRates("USD", listOf(Rate("USD", 1.0, "")), 10000)
        db.dao().insertLatestRates(latestRates1)
        val latestRates2 = LatestRates("PKR", listOf(Rate("PKR", 1.0, "")), 10101)
        db.dao().updateLatestRates(latestRates2)
        assertTrue(db.dao().getLatestRates("PKR") == null)
    }

    @Test
    fun updateLatestRates_UnSuccessful_RecordNotFound_PrimaryKeyUpdate() = runTest {
        val latestRates = LatestRates("USD", listOf(Rate("USD", 1.0, "")), 10000)
        db.dao().insertLatestRates(latestRates)
        latestRates.base = "PKR"
        db.dao().updateLatestRates(latestRates)
        assertTrue(db.dao().getLatestRates("PKR") == null)
    }

    @After
    fun tearDown()
    {
        db.close()
    }
}