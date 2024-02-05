package com.example.currencyconverter.vm

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.currencyconverter.vm.Event.*
import com.example.currencyconverter.model.Rate
import com.example.currencyconverter.repo.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val repository: Repository
) : ViewModel()
{
    val uiState = repository.uiState
    val amount = savedStateHandle.getStateFlow("amount", "")
    val currency = savedStateHandle.getStateFlow("currency", "")
    val currencyList = savedStateHandle.getStateFlow("currencyList", emptyList<String>())
    val rateList = savedStateHandle.getStateFlow("rateList", emptyList<Rate>())

    init
    {
        if (currencyList.value.isEmpty())
        {
            onEvent(GetCurrencyList())
        }
    }

    fun onEvent(event: Event)
    {
        when (event)
        {
            is GetCurrencyList -> viewModelScope.launch {
                repository.getCurrencyList()
            }

            is GetLatestRates -> viewModelScope.launch {
                repository.getLatestRates(event.currency.slice(0..2))
            }

            is UpdateAmount -> savedStateHandle["amount"] = event.amount
            is UpdateCurrency -> savedStateHandle["currency"] = event.currency
            is UpdateCurrencyList -> savedStateHandle["currencyList"] = event.currencyList
            is UpdateRateList -> savedStateHandle["rateList"] = event.rateList
        }
    }
}

sealed class Event
{
    class GetCurrencyList() : Event()
    class GetLatestRates(val currency: String = "USD") : Event()
    class UpdateAmount(val amount: String) : Event()
    class UpdateCurrency(val currency: String) : Event()
    class UpdateCurrencyList(val currencyList: List<String>) : Event()
    class UpdateRateList(val rateList: List<Rate>) : Event()
}
