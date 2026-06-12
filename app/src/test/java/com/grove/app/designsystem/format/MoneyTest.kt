package com.grove.app.designsystem.format

import org.junit.Assert.assertEquals
import org.junit.Test

class MoneyTest {
    @Test
    fun toMinorRoundsTwoDecimalCurrencies() {
        assertEquals(1_235, Money.toMinor(12.345, "USD"))
    }

    @Test
    fun toMinorRoundsZeroDecimalCurrencies() {
        assertEquals(12, Money.toMinor(12.4, "JPY"))
        assertEquals(13, Money.toMinor(12.5, "JPY"))
    }

    @Test
    fun fromMinorUsesCurrencyExponent() {
        assertEquals(12.34, Money.fromMinor(1_234, "USD"), 0.0)
        assertEquals(1_234.0, Money.fromMinor(1_234, "JPY"), 0.0)
    }
}
