package com.example.currencyconverter.repo

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.currencyconverter.model.LatestRates
import com.example.currencyconverter.model.Error
import com.example.currencyconverter.util.Utils
import com.example.currencyconverter.api.Api
import com.example.currencyconverter.db.Database
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject

class Repository @Inject constructor(
    private val context: Context, private val api: Api, private val db: Database
)
{
    private val _uiState: MutableStateFlow<Resource> = MutableStateFlow(Resource.Success(null))
    val uiState = _uiState.asStateFlow()

    suspend fun getCurrencyList()
    {
        _uiState.emit(Resource.Loading())

        val isInternetAvailable = Utils.checkInternetConnection(context)
        val currencyList = db.dao().getCurrencyList()

        _uiState.emit(
            if (currencyList.isNotEmpty())
            {
                Resource.Success(currencyList)
            } else if (isInternetAvailable)
            {
                safeApiCall(
                    triggerApiCall = { api.getCurrencyList() },
                    triggerDbEvent = { db.dao().insertCurrencyList(it) },
                )
            } else
            {
                Resource.Error("Internet not available", "Connect to stable internet connection", 101)
            }
        )
    }

    suspend fun getLatestRates(currency: String)
    {
        _uiState.emit(Resource.Loading())

        val isInternetAvailable = Utils.checkInternetConnection(context)
        val latestRates = db.dao().getLatestRates("USD")

        _uiState.emit(
            if (isInternetAvailable && latestRates != null)
            {
                // Api not updating timestamp. So updating manually
                val timeDifference = Utils.calculateTimeDifferenceInMinutes(latestRates.timestamp)
                if (timeDifference > 30)
                {
                    safeApiCall(
                        triggerApiCall = { api.getLatestRates() },
                        triggerDbEvent = {
                            val currencyList = db.dao().getCurrencyList()
                            val updatedLatestRates = Utils.transformLatestRatesToAddCurrency(it, currencyList)
                            updatedLatestRates.timestamp = System.currentTimeMillis()
                            db.dao().updateLatestRates(updatedLatestRates)
                        },
                        triggerTransformResponse = {
                            Utils.transformLatestRatesToBaseCurrency(it, currency)
                        }
                    )
                } else
                {
                    Resource.Success(Utils.transformLatestRatesToBaseCurrency(latestRates, currency))
                }
            } else if (isInternetAvailable)
            {
                safeApiCall(
                    triggerApiCall = { api.getLatestRates() },
                    triggerDbEvent = {
                        val currencyList = db.dao().getCurrencyList()
                        val updatedLatestRates = Utils.transformLatestRatesToAddCurrency(it, currencyList)
                        db.dao().insertLatestRates(updatedLatestRates)
                    },
                    triggerTransformResponse = {
                        Utils.transformLatestRatesToBaseCurrency(it, currency)
                    }
                )
            } else if (latestRates != null)
            {
                Resource.Success(Utils.transformLatestRatesToBaseCurrency(latestRates, currency))
            } else
            {
                Resource.Error("Internet not available", "Connect to stable internet connection")
            }
        )
    }

    private suspend fun <T> safeApiCall(
        triggerApiCall: suspend () -> Response<T>,
        triggerDbEvent: suspend (T) -> Unit,
        triggerTransformResponse: (suspend (T) -> LatestRates)? = null
    ): Resource
    {
        return try
        {
            val response = triggerApiCall()
            val data = response.body()
            if (response.isSuccessful && data != null)
            {
                triggerDbEvent(data)
                Resource.Success(if (triggerTransformResponse != null) triggerTransformResponse(data) else data)
            } else
            {
                val errorBody = response.errorBody()?.string() ?: ""
                val error = Gson().fromJson(errorBody, Error::class.java)
                Resource.Error(
                    error?.message ?: "Something is wrong!",
                    error?.description ?: "${response.code()} ${response.message()}"
                )
            }
        } catch (e: IOException)
        {
            Resource.Error("Something is wrong!", e.message ?: "Check your internet connection")
        } catch (e: Exception)
        {
            Resource.Error("Something is wrong!", e.message ?: "Try later")
        }
    }
}

sealed class Resource
{
    class Success<T>(var data: T) : Resource()
    {
        inline fun <reified T> checkInstance(): Boolean
        {
            return this.data is T
        }
    }

    class Error(var message: String, var description: String, var code: Int? = 0) : Resource()
    class Loading : Resource()
}