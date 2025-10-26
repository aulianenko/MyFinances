package dev.aulianenko.myfinances.domain

data class Currency(
    val code: String,
    val symbol: String,
    val name: String
)

object CurrencyProvider {
    val currencies = listOf(
        Currency("USD", "$", "US Dollar"),
        Currency("EUR", "€", "Euro"),
        Currency("GBP", "£", "British Pound"),
        Currency("JPY", "¥", "Japanese Yen"),
        Currency("CHF", "CHF", "Swiss Franc"),
        Currency("CAD", "C$", "Canadian Dollar"),
        Currency("AUD", "A$", "Australian Dollar"),
        Currency("CNY", "¥", "Chinese Yuan"),
        Currency("INR", "₹", "Indian Rupee"),
        Currency("BRL", "R$", "Brazilian Real"),
        Currency("RUB", "₽", "Russian Ruble"),
        Currency("KRW", "₩", "South Korean Won"),
        Currency("MXN", "$", "Mexican Peso"),
        Currency("SGD", "S$", "Singapore Dollar"),
        Currency("HKD", "HK$", "Hong Kong Dollar"),
        Currency("NOK", "kr", "Norwegian Krone"),
        Currency("SEK", "kr", "Swedish Krona"),
        Currency("DKK", "kr", "Danish Krone"),
        Currency("PLN", "zł", "Polish Zloty"),
        Currency("THB", "฿", "Thai Baht"),
        Currency("IDR", "Rp", "Indonesian Rupiah"),
        Currency("CZK", "Kč", "Czech Koruna"),
        Currency("ILS", "₪", "Israeli Shekel"),
        Currency("ZAR", "R", "South African Rand"),
        Currency("TRY", "₺", "Turkish Lira"),
        Currency("UAH", "₴", "Ukrainian Hryvnia")
    )

    fun getCurrencyByCode(code: String): Currency? = currencies.find { it.code == code }
}
