package com.example.currencyconverter.vm

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.example.currencyconverter.model.Currency
import com.example.currencyconverter.model.LatestRates
import com.example.currencyconverter.model.Rate
import com.example.currencyconverter.repo.Repository
import com.example.currencyconverter.repo.Resource
import com.example.currencyconverter.vm.Event
import com.example.currencyconverter.vm.ViewModel
import com.nhaarman.mockitokotlin2.doReturn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations

class ViewModalTest
{
    private val testDispatcher = StandardTestDispatcher()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    lateinit var repository: Repository

    @Mock
    lateinit var savedStateHandle: SavedStateHandle

    @Mock
    lateinit var eventAmount: Event.UpdateAmount

    @Mock
    lateinit var eventCurrency: Event.UpdateCurrency

    @Mock
    lateinit var eventCurrencyList: Event.UpdateCurrencyList

    @Mock
    lateinit var eventRateList: Event.UpdateRateList

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setup()
    {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)

        doReturn(MutableStateFlow(emptyList<String>()).asStateFlow())
            .`when`(savedStateHandle).getStateFlow("currencyList", emptyList<String>())
    }

    @Test
    fun testOnEvent_CurrencyList_Successful() = runTest {
        val currencyList = listOf(
            Currency("USD", "US Dollar"),
            Currency("PKR", "Pakistani Rupee")
        )
        Mockito.`when`(repository.uiState)
            .thenReturn(MutableStateFlow(Resource.Success(currencyList)).asStateFlow())

        val viewModel = ViewModel(savedStateHandle, repository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val resource = awaitItem()
            Assert.assertTrue(resource is Resource.Success<*>)
            Assert.assertTrue((resource as Resource.Success<*>).checkInstance<List<Currency>>())
            Assert.assertTrue((resource.data as List<*>).size == 2)
            cancel()
        }
    }

    @Test
    fun testOnEvent_LatestRates_Successful() = runTest {
        val rates = listOf(
            Rate("USD", 1.0, ""),
            Rate("PKR", 278.0, "")
        )
        val latestRates = LatestRates("USD", rates, 999000)
        Mockito.`when`(repository.uiState)
            .thenReturn(MutableStateFlow(Resource.Success(latestRates)).asStateFlow())

        val viewModel = ViewModel(savedStateHandle, repository)
        viewModel.onEvent(Event.GetLatestRates())
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val resource = awaitItem()
            Assert.assertTrue(resource is Resource.Success<*>)
            Assert.assertTrue((resource as Resource.Success<*>).checkInstance<LatestRates>())

            val _latestRates = (resource.data as LatestRates)
            Assert.assertTrue(_latestRates.base == "USD")
            Assert.assertTrue(_latestRates.rates.size == 2)
            cancel()
        }
    }

    @Test
    fun testOnEvent_LatestRates_UnSuccessful() = runTest {
        Mockito.`when`(repository.uiState)
            .thenReturn(MutableStateFlow(Resource.Error("error", "something wrong")).asStateFlow())

        val viewModel = ViewModel(savedStateHandle, repository)
        viewModel.onEvent(Event.GetLatestRates())
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val resource = awaitItem()
            Assert.assertTrue(resource is Resource.Error)
            cancel()
        }
    }

    @Test
    fun testOnEvent_UpdateAmount_Successful() = runTest {
        val savedStateHandle = SavedStateHandle()
        savedStateHandle["amount"] = ""

        Mockito.`when`(eventAmount.amount).thenReturn("1")

        val viewModel = ViewModel(savedStateHandle, repository)

        launch {
            delay(100)
            viewModel.onEvent(eventAmount)
        }

        viewModel.amount.test {
            var amount = awaitItem()
            Assert.assertTrue(amount == "")
            amount = awaitItem()
            Assert.assertTrue(amount == "1")
            cancel()
        }
    }

    @Test
    fun testOnEvent_UpdateCurrency_Successful() = runTest {
        val savedStateHandle = SavedStateHandle()
        savedStateHandle["currency"] = ""

        Mockito.`when`(eventCurrency.currency).thenReturn("USD")

        val viewModel = ViewModel(savedStateHandle, repository)

        launch {
            delay(100)
            viewModel.onEvent(eventCurrency)
        }

        viewModel.currency.test {
            var currency = awaitItem()
            Assert.assertTrue(currency == "")
            currency = awaitItem()
            Assert.assertTrue(currency == "USD")
            cancel()
        }
    }

    @Test
    fun testOnEvent_UpdateCurrencyList_Successful() = runTest {
        val savedStateHandle = SavedStateHandle()
        savedStateHandle["currencyList"] = emptyList<String>()

        Mockito.`when`(eventCurrencyList.currencyList).thenReturn(listOf("USD", "PKR"))

        val viewModel = ViewModel(savedStateHandle, repository)

        launch {
            delay(100)
            viewModel.onEvent(eventCurrencyList)
        }

        viewModel.currencyList.test {
            var currencyList = awaitItem()
            Assert.assertTrue(currencyList.isEmpty())
            currencyList = awaitItem()
            Assert.assertTrue(currencyList.size == 2)
            cancel()
        }
    }

    @Test
    fun testOnEvent_UpdateRateList_Successful() = runTest {
        val savedStateHandle = SavedStateHandle()
        savedStateHandle["rateList"] = emptyList<Rate>()

        Mockito.`when`(eventRateList.rateList).thenReturn(
            listOf(Rate("USD", 1.0,"US Dollar"), Rate("PKR", 278.0, "Pakistani Rupee"))
        )

        val viewModel = ViewModel(savedStateHandle, repository)

        launch {
            delay(100)
            viewModel.onEvent(eventRateList)
        }

        viewModel.rateList.test {
            var rateList = awaitItem()
            Assert.assertTrue(rateList.isEmpty())
            rateList = awaitItem()
            Assert.assertTrue(rateList.size == 2)
            cancel()
        }
    }
}