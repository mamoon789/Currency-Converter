package com.example.currencyconverter.worker

import android.annotation.SuppressLint
import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.currencyconverter.repo.Repository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class Worker @AssistedInject constructor(
    private val repository: Repository,
    @Assisted context: Context,
    @Assisted params: WorkerParameters
): CoroutineWorker(context, params)
{
    @SuppressLint("RestrictedApi")
    override suspend fun doWork(): Result
    {
        repository.getCurrencyList()
        return Result.Success()
    }
}