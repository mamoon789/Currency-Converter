package com.example.currencyconverter.ui.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalTextInputService
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import com.example.currencyconverter.ui.theme.CurrencyConverterTheme
import com.example.currencyconverter.ui.theme.White
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.example.currencyconverter.model.Currency
import com.example.currencyconverter.vm.Event
import com.example.currencyconverter.model.LatestRates
import com.example.currencyconverter.model.Rate
import com.example.currencyconverter.vm.ViewModel
import com.example.currencyconverter.worker.Worker
import com.example.currencyconverter.repo.Resource
import kotlinx.coroutines.delay

@AndroidEntryPoint
class Activity : ComponentActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        setContent {
            CurrencyConverterTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: ViewModel by viewModels()

                    val isLoading = remember { mutableStateOf(false) }
                    val errorMessage = remember { mutableStateOf("") }
                    val errorDescription = remember { mutableStateOf("") }

                    val amount = viewModel.amount.collectAsState()
                    val currency = viewModel.currency.collectAsState().value
                    val currencyList = viewModel.currencyList.collectAsState().value
                    val rateList = viewModel.rateList.collectAsState().value

                    LaunchedEffect(Unit) {
                        lifecycleScope.launch {
                            repeatOnLifecycle(Lifecycle.State.STARTED) {
                                viewModel.uiState.collect { resource ->
                                    when (resource)
                                    {
                                        is Resource.Loading ->
                                        {
                                            isLoading.value = true
                                        }

                                        is Resource.Error ->
                                        {
                                            isLoading.value = false
                                            errorMessage.value = resource.message
                                            errorDescription.value = resource.description

                                            if (resource.code == 101)
                                            {
                                                val workManager =
                                                    WorkManager.getInstance(this@Activity)
                                                val constraints = Constraints.Builder()
                                                    .setRequiredNetworkType(NetworkType.CONNECTED)
                                                    .build()
                                                val request =
                                                    OneTimeWorkRequest.Builder(
                                                        Worker::class.java
                                                    )
                                                        .setConstraints(constraints)
                                                        .build()
                                                workManager.enqueueUniqueWork(
                                                    "main_worker",
                                                    ExistingWorkPolicy.REPLACE,
                                                    request
                                                )
                                            }
                                        }

                                        is Resource.Success<*> ->
                                        {
                                            isLoading.value = false
                                            if (resource.checkInstance<LatestRates>())
                                            {
                                                (resource.data as LatestRates).apply {
                                                    if (amount.value.isEmpty() ||
                                                        amount.value.matches(Regex("\\."))
                                                    ) return@apply
                                                    viewModel.onEvent(
                                                        Event.UpdateRateList(
                                                            rates.map {
                                                                Rate(
                                                                    it.key,
                                                                    amount.value.toDouble() * it.value,
                                                                    it.name,
                                                                )
                                                            }
                                                        )
                                                    )
                                                }
                                            } else if (resource.checkInstance<List<Currency>>())
                                            {
                                                (resource.data as List<Currency>).apply {
                                                    viewModel.onEvent(
                                                        Event.UpdateCurrencyList(map { "${it.key} - ${it.value}" })
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }


                    LaunchedEffect(key1 = amount.value, key2 = currency) {
                        if (amount.value.isEmpty() || amount.value.matches(Regex("\\.")) || currency.isEmpty())
                        {
                            viewModel.onEvent(Event.UpdateRateList(emptyList()))
                            return@LaunchedEffect
                        }
                        delay(1000)
                        viewModel.onEvent(Event.GetLatestRates(currency))
                    }

                    App(amount.value, currency, currencyList, rateList) {
                        viewModel.onEvent(it)
                    }

                    if (isLoading.value) Loader()

                    if (errorMessage.value.isNotEmpty()) ErrorDialog(
                        errorMessage,
                        errorDescription
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(
    amount: String,
    currency: String,
    currencyList: List<String>,
    rateList: List<Rate>,
    event: (Event) -> Unit
)
{
    val expanded = remember { mutableStateOf(false) }
    Column(
        modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.End
    ) {
        TextField(
            value = amount,
            onValueChange = {
                if (it.matches(Regex("[0-9]*+.?+[0-9]*?")))
                    event(Event.UpdateAmount(it))
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(text = "Enter amount") },
            singleLine = true,
            maxLines = 1,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        )
        Spacer(modifier = Modifier.height(10.dp))
        ExposedDropdownMenuBox(expanded = expanded.value,
            onExpandedChange = { expanded.value = !expanded.value },
            content = {
                CompositionLocalProvider(LocalTextInputService provides null) {
                    TextField(
                        value = currency,
                        onValueChange = {},
                        placeholder = { Text(text = "Pick currency") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        readOnly = true,
                        singleLine = true,
                        maxLines = 1,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded.value)
                        },
                    )
                }
                ExposedDropdownMenu(expanded = expanded.value,
                    onDismissRequest = { expanded.value = false },
                    content = {
                        currencyList.map {
                            DropdownMenuItem(onClick = {
                                expanded.value = false
                                event(Event.UpdateCurrency(it))
                            }, text = { Text(text = it, maxLines = 1) })
                        }
                    })
            })
        Spacer(modifier = Modifier.height(20.dp))
        LazyVerticalGrid(columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(bottom = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            content = {
                rateList.map {
                    item {
                        Card(elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)) {
                            Text(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(top = 5.dp),
                                text = it.key,
                                minLines = 1,
                                maxLines = 1,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(start = 10.dp, end = 10.dp, top = 5.dp, bottom = 5.dp),
                                text = it.name,
                                fontSize = TextUnit(14f, TextUnitType.Sp),
                                minLines = 2,
                                maxLines = 2,
                                textAlign = TextAlign.Center,
                                overflow = TextOverflow.Ellipsis
                            )
                            val scroll = rememberScrollState(0)
                            Text(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(start = 10.dp, end = 10.dp, bottom = 5.dp)
                                    .horizontalScroll(scroll),
                                text = String.format("%.3f", it.value),
                                minLines = 1,
                                maxLines = 1,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            })
    }
}

@Composable
fun Loader()
{
    Box(contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .wrapContentSize()
                .clip(CircleShape)
                .background(White)
                .padding(10.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier
                    .width(64.dp)
                    .height(64.dp),
                color = MaterialTheme.colorScheme.secondary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        }
    }
}

@Composable
fun ErrorDialog(errorMessage: MutableState<String>, errorDescription: MutableState<String>)
{
    val dismissDialog = {
        errorMessage.value = ""
        errorDescription.value = ""
    }
    AlertDialog(onDismissRequest = dismissDialog,
        title = { Text(errorMessage.value) },
        text = { Text(errorDescription.value) },
        confirmButton = {
            TextButton(onClick = dismissDialog) {
                Text("Ok")
            }
        })
}

@Preview(showBackground = true)
@Composable
fun Preview()
{
    CurrencyConverterTheme {
        App(
            amount = "1",
            currency = "USD",
            currencyList = listOf("USD"),
            rateList = listOf(Rate("USD", 1.0, "US Dollar")),
            event = {}
        )
    }
}