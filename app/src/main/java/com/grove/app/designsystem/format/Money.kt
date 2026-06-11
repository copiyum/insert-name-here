package com.grove.app.designsystem.format

import java.text.DecimalFormatSymbols
import java.util.Locale

object Money {
    private val FMT_SYM = DecimalFormatSymbols(Locale.US)

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
        val sym = Currencies.current(currencyCode).symbol
        return when (decimals) {
            0 -> "$sym${String.format(Locale.US, "%,d", amount.toLong())}"
            else -> "$sym${String.format(Locale.US, "%,.${decimals}f", amount)}"
        }
    }

    /** Long-minor-unit variant — use for amounts read from Room. */
    fun currencyLong(
        minor: Long,
        decimals: Int = 2,
        currencyCode: String = "USD",
    ): String {
        val exp = Currencies.minorUnitExponent(currencyCode)
        val display = minor.toDouble() / Math.pow(10.0, exp.toDouble())
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
}
