package com.grove.app.designsystem.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.grove.app.designsystem.format.Currencies
import com.grove.app.designsystem.format.Money

@Stable
class MoneyInputState(
    initialText: String,
    private val currencyCode: String,
) {
    var text by mutableStateOf(initialText)
        private set

    val amountMinor: Long
        get() = Money.parseMinor(text, currencyCode)

    val amountMajor: Double
        get() = Money.fromMinor(amountMinor, currencyCode)

    val hasAmount: Boolean
        get() = amountMinor > 0L

    fun updateText(value: String) {
        text = Money.normalizedInput(value, currencyCode)
    }

    fun appendDigit(digit: Char) {
        if (!digit.isDigit()) return
        val exponent = Currencies.minorUnitExponent(currencyCode)
        val fractional = text.substringAfter(".", "")
        if (text.length < 10 && (!text.contains(".") || fractional.length < exponent)) {
            text += digit
        }
    }

    fun appendDecimal() {
        if (Currencies.minorUnitExponent(currencyCode) > 0 && !text.contains(".")) {
            text += if (text.isEmpty()) "0." else "."
        }
    }

    fun backspace() {
        if (text.isNotEmpty()) text = text.dropLast(1)
    }
}

@Composable
fun rememberMoneyInputState(
    currencyCode: String,
    initialMinor: Long? = null,
    key: Any? = initialMinor,
): MoneyInputState =
    remember(currencyCode, key) {
        MoneyInputState(
            initialText = initialMinor?.let { Money.minorInputText(it, currencyCode) } ?: "",
            currencyCode = currencyCode,
        )
    }
