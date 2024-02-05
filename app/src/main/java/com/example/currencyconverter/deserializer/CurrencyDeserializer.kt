package com.example.currencyconverter.deserializer

import com.example.currencyconverter.model.Currency
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

class CurrencyDeserializer : JsonDeserializer<List<Currency>>
{
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): List<Currency>
    {
        if (json == null || context == null)
        {
            throw Exception("Error")
        }
        val obj = json.asJsonObject.entrySet()
        return obj.map {
            Currency(it.key, it.value.asString)
        }
    }
}