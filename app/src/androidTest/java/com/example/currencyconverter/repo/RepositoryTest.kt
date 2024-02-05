package com.example.currencyconverter.repo

import com.example.currencyconverter.db.Database
import com.example.currencyconverter.model.Currency
import com.example.currencyconverter.model.LatestRates
import com.example.currencyconverter.model.Rate
import com.example.currencyconverter.repo.Repository
import com.example.currencyconverter.repo.Resource
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class RepositoryTest
{
    @get:Rule
    var hiltAndroidRule = HiltAndroidRule(this)

    @Inject
    lateinit var repository: Repository

    @Inject
    lateinit var db: Database

    @Inject
    lateinit var mockWebServer: MockWebServer

    @Before
    fun setup()
    {
        hiltAndroidRule.inject()
    }

    @Test
    fun testGetCurrencyList_Successful_Api() = runTest {
        val mockResponse = MockResponse()
        val body = "{\"USD\": \"US Dollar\", \"PKR\": \"Pakistani Rupee\"}"
        mockResponse.setResponseCode(200)
        mockResponse.setBody(body)
        mockWebServer.enqueue(mockResponse)

        repository.getCurrencyList()
        mockWebServer.takeRequest()

        val resource = repository.uiState.value
        assertTrue(resource is Resource.Success<*>)
        assertTrue((resource as Resource.Success<*>).checkInstance<List<Currency>>())
        assertTrue((resource.data as List<Currency>).size == 2)
    }

    @Test
    fun testGetCurrencyList_Successful_DB_RecordFound() = runTest {
        val currencyList = listOf(Currency("USD", "US Dollar"), Currency("PKR", "Pakistani Rupee"))
        db.dao().insertCurrencyList(currencyList)

        repository.getCurrencyList()

        val resource = repository.uiState.value
        assertTrue(resource is Resource.Success<*>)
        assertTrue((resource as Resource.Success<*>).checkInstance<List<Currency>>())
        assertTrue((resource.data as List<Currency>).size == 2)
    }


    @Test
    fun testGetLatestRates_Successful_Api() = runTest {
        val mockResponse = MockResponse()
        val body = "{\"timestamp\": 11212211, \"base\": \"USD\", \"rates\": {\"USD\": 1, \"PKR\": 278}}"
        mockResponse.setResponseCode(200)
        mockResponse.setBody(body)
        mockWebServer.enqueue(mockResponse)

        repository.getLatestRates("PKR")
        mockWebServer.takeRequest()

        val resource = repository.uiState.value
        assertTrue(resource is Resource.Success<*>)
        assertTrue((resource as Resource.Success<*>).checkInstance<LatestRates>())

        val latestRates = (resource.data as LatestRates)
        assertTrue(latestRates.base == "PKR")
        assertTrue(latestRates.rates.size == 2)
    }

    @Test
    fun testGetLatestRates_UnSuccessful_Api_ChangedBaseCurrency() = runTest {
        val mockResponse = MockResponse()
        val body = "{\"error\": true, \"status\": 403, \"message\": \"not_allowed\"," +
                "\"description\": \"Changing the API `base` currency is available for Developer, " +
                "Enterprise and Unlimited plan clients. Please upgrade, or contact " +
                "support@openexchangerates.org with any questions.\"}"
        mockResponse.setResponseCode(403)
        mockResponse.setBody(body)
        mockWebServer.enqueue(mockResponse)

        repository.getLatestRates("USD")
        mockWebServer.takeRequest()

        val resource = repository.uiState.value
        assertTrue(resource is Resource.Error)
    }

    @Test
    fun testGetLatestRates_Successful_DB_RecordFound() = runTest {
        val rates = listOf(Rate("USD", 1.0, ""), Rate("PKR", 278.0, ""))
        db.dao().insertLatestRates(LatestRates("USD", rates, 990909090900))

        repository.getLatestRates("PKR")

        val resource = repository.uiState.value
        assertTrue(resource is Resource.Success<*>)
        assertTrue((resource as Resource.Success<*>).checkInstance<LatestRates>())

        val latestRates = (resource.data as LatestRates)
        assertTrue(latestRates.base == "PKR")
        assertTrue(latestRates.rates.size == 2)
    }

    @After
    fun tearDown()
    {
        db.close()
    }
}