package com.example.currencyconverter.api

import com.example.currencyconverter.model.Currency
import com.example.currencyconverter.model.LatestRates
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
class ApiTest
{
    @get:Rule
    val hiltAndroidRule = HiltAndroidRule(this)

    @Inject
    lateinit var mockWebServer: MockWebServer

    @Inject
    lateinit var api: Api

    @Before
    fun setup()
    {
        hiltAndroidRule.inject()
    }

    @Test
    fun testGetCurrencies_Successful() = runTest {
        val mockResponse = MockResponse()
        val body = "{\"USD\": \"US Dollar\", \"PKR\": \"Pakistani Rupee\"}"
        mockResponse.setResponseCode(200)
        mockResponse.setBody(body)
        mockWebServer.enqueue(mockResponse)

        val response = api.getCurrencyList()
        mockWebServer.takeRequest()

        assertTrue(response.isSuccessful)
        assertTrue(response.body()!!.isNotEmpty())
        assertTrue(response.body() is List<Currency>)
        assertTrue(response.body()!!.size == 2)
    }

    @Test
    fun testGetLatestRates_Successful() = runTest {
        val mockResponse = MockResponse()
        val body = "{\"timestamp\": 11212211, \"base\": \"USD\", \"rates\": {\"USD\": 1, \"PKR\": 278}}"
        mockResponse.setResponseCode(200)
        mockResponse.setBody(body)
        mockWebServer.enqueue(mockResponse)

        val response = api.getLatestRates()
        mockWebServer.takeRequest()

        assertTrue(response.isSuccessful)
        assertTrue(response.body() != null)
        assertTrue(response.body() is LatestRates)
    }

    @Test
    fun testGetLatestRates_UnSuccessful_ChangedBaseCurrency() = runTest {
        val mockResponse = MockResponse()
        val body = "{\"error\": true, \"status\": 403, \"message\": \"not_allowed\"," +
                "\"description\": \"Changing the API `base` currency is available for Developer, " +
                "Enterprise and Unlimited plan clients. Please upgrade, or contact " +
                "support@openexchangerates.org with any questions.\"}"
        mockResponse.setResponseCode(403)
        mockResponse.setBody(body)
        mockWebServer.enqueue(mockResponse)

        val response = api.getLatestRates()
        mockWebServer.takeRequest()

        assertTrue(!response.isSuccessful)
        assertTrue(response.body() == null)
    }

    @After
    fun tearDown()
    {
        mockWebServer.shutdown()
    }
}