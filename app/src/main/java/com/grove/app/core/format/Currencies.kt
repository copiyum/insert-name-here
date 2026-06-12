package com.grove.app.core.format

data class Currency(
    val code: String,
    val symbol: String,
    val name: String,
    val example: String,
)

object Currencies {
    val list =
        listOf(
            Currency("INR", "₹", "Indian Rupee", "₹1,234.56"),
            Currency("USD", "$", "US Dollar", "$1,234.56"),
            Currency("EUR", "€", "Euro", "€1 234,56"),
            Currency("GBP", "£", "British Pound", "£1,234.56"),
            Currency("JPY", "¥", "Japanese Yen", "¥123,456"),
            Currency("CAD", "C$", "Canadian Dollar", "C$1,234.56"),
            Currency("AUD", "A$", "Australian Dollar", "A$1,234.56"),
            Currency("CHF", "CHF", "Swiss Franc", "CHF 1'234.56"),
            Currency("CNY", "¥", "Chinese Yuan", "¥1,234.56"),
            Currency("KRW", "₩", "South Korean Won", "₩123,456"),
            Currency("SGD", "S$", "Singapore Dollar", "S$1,234.56"),
            Currency("HKD", "HK$", "Hong Kong Dollar", "HK$1,234.56"),
            Currency("BRL", "R$", "Brazilian Real", "R$ 1.234,56"),
            Currency("MXN", "MX$", "Mexican Peso", "MX$1,234.56"),
            Currency("ZAR", "R", "South African Rand", "R 1,234.56"),
        )

    private val map = list.associateBy { it.code }

    fun current(code: String): Currency = map[code] ?: list[0]


    fun minorUnitExponent(code: String): Int =
        when (code) {
            "JPY", "KRW" -> 0
            else -> 2
        }
}
