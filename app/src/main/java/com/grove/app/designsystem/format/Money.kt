package com.grove.app.designsystem.format

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Currency as JavaCurrency
import java.util.Locale

object Money {
    fun toMinor(
        amount: Double,
        currencyCode: String,
    ): Long {
        val exp = Currencies.minorUnitExponent(currencyCode)
        return BigDecimal
            .valueOf(amount)
            .movePointRight(exp)
            .setScale(0, RoundingMode.HALF_UP)
            .longValueExact()
    }

    fun fromMinor(
        minor: Long,
        currencyCode: String,
    ): Double {
        val exp = Currencies.minorUnitExponent(currencyCode)
        return BigDecimal.valueOf(minor, exp).toDouble()
    }

    fun currency(
        amount: Double,
        decimals: Int = 2,
        currencyCode: String = "INR",
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
        currencyCode: String = "INR",
    ): String {
        val exp = Currencies.minorUnitExponent(currencyCode)
        val display = BigDecimal.valueOf(minor, exp).toDouble()
        val actualDecimals = if (decimals == 2) exp else decimals
        return currency(display, actualDecimals, currencyCode)
    }

    fun parseMinor(
        input: String,
        currencyCode: String,
    ): Long =
        normalizedInput(input, currencyCode)
            .takeIf { it.isNotBlank() && it != "." }
            ?.let { normalized ->
                val exp = Currencies.minorUnitExponent(currencyCode)
                BigDecimal(normalized)
                    .movePointRight(exp)
                    .setScale(0, RoundingMode.HALF_UP)
                    .longValueExact()
            } ?: 0L

    fun minorInputText(
        minor: Long,
        currencyCode: String,
    ): String {
        val exp = Currencies.minorUnitExponent(currencyCode)
        val decimal = BigDecimal.valueOf(minor, exp).setScale(exp, RoundingMode.UNNECESSARY)
        return if (exp == 0) decimal.toBigIntegerExact().toString() else decimal.toPlainString()
    }

    fun normalizedInput(
        value: String,
        currencyCode: String,
    ): String = normalizedInput(value, Currencies.minorUnitExponent(currencyCode))

    fun normalizedInput(
        value: String,
        minorExponent: Int,
    ): String {
        val cleaned = value.filter { it.isDigit() || it == '.' }
        val firstDot = cleaned.indexOf('.')
        if (firstDot < 0 || minorExponent == 0) return cleaned.replace(".", "")
        val whole = cleaned.take(firstDot).ifBlank { "0" }
        val fractional = cleaned.drop(firstDot + 1).replace(".", "").take(minorExponent)
        return "$whole.$fractional"
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
