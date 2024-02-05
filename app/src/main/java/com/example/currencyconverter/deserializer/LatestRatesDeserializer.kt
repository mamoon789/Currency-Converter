package com.example.currencyconverter.deserializer

import com.example.currencyconverter.model.LatestRates
import com.example.currencyconverter.model.Rate
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

class LatestRatesDeserializer : JsonDeserializer<LatestRates>
{
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): LatestRates
    {
        if (json == null || context == null)
        {
            throw Exception("Error")
        }
        val obj = json.asJsonObject
        val base = context.deserialize<String?>(obj.get("base"), String::class.java)
        val timestamp = context.deserialize<Long?>(obj.get("timestamp"), Long::class.java)

        // create List<Rate> from the rates JsonObject
        val ratesSet = obj.get("rates").asJsonObject.entrySet()
        val ratesList = ratesSet.map {
            Rate(it.key, it.value.asDouble, "")
        }

        return LatestRates(base, ratesList, timestamp)
    }
}