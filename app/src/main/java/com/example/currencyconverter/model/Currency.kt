package com.example.currencyconverter.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Currency")
data class Currency(
    @PrimaryKey
    val key: String,
    val value: String,
)
