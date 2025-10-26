package dev.aulianenko.myfinances.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "account_values",
    foreignKeys = [
        ForeignKey(
            entity = Account::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["accountId"])]
)
data class AccountValue(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val accountId: String,
    val value: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val note: String? = null
)