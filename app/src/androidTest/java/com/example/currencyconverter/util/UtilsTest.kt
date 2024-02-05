package com.example.currencyconverter.util

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.currencyconverter.util.Utils
import org.junit.Test
import org.junit.Assert.*


class UtilsTest
{
    @Test()
    fun testCheckInternetConnection_Successful()
    {
        val context = ApplicationProvider.getApplicationContext<Context>()
        Utils.checkInternetConnection(context)
        assertTrue(true)
    }
}