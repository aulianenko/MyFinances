package dev.aulianenko.myfinances.domain.model

import java.util.Calendar

enum class TimePeriod(val displayName: String) {
    THREE_MONTHS("3 Months"),
    SIX_MONTHS("6 Months"),
    ONE_YEAR("1 Year"),
    MAX("All Time");

    fun getStartTimestamp(): Long {
        return when (this) {
            THREE_MONTHS -> {
                Calendar.getInstance().apply {
                    add(Calendar.MONTH, -3)
                }.timeInMillis
            }
            SIX_MONTHS -> {
                Calendar.getInstance().apply {
                    add(Calendar.MONTH, -6)
                }.timeInMillis
            }
            ONE_YEAR -> {
                Calendar.getInstance().apply {
                    add(Calendar.YEAR, -1)
                }.timeInMillis
            }
            MAX -> 0L
        }
    }

    fun getEndTimestamp(): Long = System.currentTimeMillis()
}
