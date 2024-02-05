package com.example.currencyconverter.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "LatestRates")
data class LatestRates(
    @PrimaryKey
    var base: String,
    var rates: List<Rate>,
    var timestamp: Long,
)