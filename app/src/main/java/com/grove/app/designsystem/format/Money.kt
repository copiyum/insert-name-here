package com.grove.app.designsystem.format

import java.text.NumberFormat
import java.util.Currency as JavaCurrency
import java.util.Locale

object Money {
    fun toMinor(
        amount: Double,
        currencyCode: String,
    ): Long {
        val exp = Currencies.minorUnitExponent(currencyCode)
        return Math.round(amount * Math.pow(10.0, exp.toDouble()))
    }

    fun fromMinor(
        minor: Long,
        currencyCode: String,
    ): Double {
        val exp = Currencies.minorUnitExponent(currencyCode)
        return minor.toDouble() / Math.pow(10.0, exp.toDouble())
    }

    fun format(
        amount: Double,
        currencyCode: String = "USD",
    ): String {
        val sym = Currencies.current(currencyCode).symbol
        return "$sym${String.format(Locale.US, "%.2f", amount)}"
    }

    fun currency(
        amount: Double,
        decimals: Int = 2,
        currencyCode: String = "USD",
    ): String {
        return runCatching {
            NumberFormat.getCurrencyInstance(localeFor(currencyCode)).apply {
                currency = JavaCurrency.getInstance(currencyCode)
                minimumFractionDigits = decimals
                maximumFractionDigits = decimals
            }.format(amount)
        }.getOrElse {
            val sym = Currencies.current(currencyCode).symbol
            when (decimals) {
                0 -> "$sym${String.format(Locale.US, "%,d", Math.round(amount))}"
                else -> "$sym${String.format(Locale.US, "%,.${decimals}f", amount)}"
            }
        }
    }


    fun currencyLong(
        minor: Long,
        decimals: Int = 2,
        currencyCode: String = "USD",
    ): String {
        val exp = Currencies.minorUnitExponent(currencyCode)
        val display = fromMinor(minor, currencyCode)
        val actualDecimals = if (decimals == 2) exp else decimals
        return currency(display, actualDecimals, currencyCode)
    }

    fun signed(
        amount: Double,
        currencyCode: String = "USD",
    ): String {
        val abs = kotlin.math.abs(amount)
        val base = currency(abs, 2, currencyCode)
        return if (amount < 0) "-$base" else "+$base"
    }

    fun short(
        amount: Double,
        currencyCode: String = "USD",
    ): String {
        val sym = Currencies.current(currencyCode).symbol
        return when {
            amount >= 1_000_000 -> "$sym${String.format(Locale.US, "%.1fM", amount / 1_000_000)}"
            amount >= 1_000 -> "$sym${String.format(Locale.US, "%.1fK", amount / 1_000)}"
            else -> "$sym${String.format(Locale.US, "%.0f", amount)}"
        }
    }

    private fun localeFor(currencyCode: String): Locale =
        when (currencyCode) {
            "INR" -> Locale("en", "IN")
            "EUR" -> Locale.FRANCE
            "GBP" -> Locale.UK
            "JPY" -> Locale.JAPAN
            "CAD" -> Locale.CANADA
            "AUD" -> Locale("en", "AU")
            "CHF" -> Locale("de", "CH")
            "CNY" -> Locale.CHINA
            "KRW" -> Locale.KOREA
            "SGD" -> Locale("en", "SG")
            "HKD" -> Locale("en", "HK")
            "BRL" -> Locale("pt", "BR")
            "MXN" -> Locale("es", "MX")
            "ZAR" -> Locale("en", "ZA")
            else -> Locale.US
        }
}
