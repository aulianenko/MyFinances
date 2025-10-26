package dev.aulianenko.myfinances.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "accounts")
data class Account(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val currency: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)