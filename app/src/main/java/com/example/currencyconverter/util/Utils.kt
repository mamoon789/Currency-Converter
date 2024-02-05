package com.example.currencyconverter.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.example.currencyconverter.model.Currency
import com.example.currencyconverter.model.LatestRates
import com.example.currencyconverter.model.Rate

object Utils
{
    fun checkInternetConnection(context: Context): Boolean
    {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if (capabilities != null)
        {
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
            {
                Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
                return true
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI))
            {
                Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
                return true
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))
            {
                Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
                return true
            }
        }
        return false
    }

    fun calculateTimeDifferenceInMinutes(timestamp: Long): Long
    {
        return (System.currentTimeMillis() / 1000 - timestamp) / 60
    }

    fun transformLatestRatesToAddCurrency(latestRates: LatestRates, currencyList: List<Currency>): LatestRates
    {
        return latestRates.apply {
            rates = rates.map { rate ->
                val currency = currencyList.find { rate.key == it.key }
                Rate(rate.key, rate.value, currency?.value ?: "")
            }
        }
    }

    fun transformLatestRatesToBaseCurrency(latestRates: LatestRates, currency: String): LatestRates
    {
        return latestRates.apply {
            rates.find { it.key == currency }!!.apply {
                base = currency
                rates = rates.map {
                    Rate(it.key, it.value / value, it.name)
                }
            }
        }
    }
}