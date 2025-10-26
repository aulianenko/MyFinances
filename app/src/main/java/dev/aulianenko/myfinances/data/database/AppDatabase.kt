package dev.aulianenko.myfinances.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import dev.aulianenko.myfinances.data.dao.AccountDao
import dev.aulianenko.myfinances.data.dao.AccountValueDao
import dev.aulianenko.myfinances.data.entity.Account
import dev.aulianenko.myfinances.data.entity.AccountValue

@Database(
    entities = [Account::class, AccountValue::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun accountValueDao(): AccountValueDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "myfinances_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}