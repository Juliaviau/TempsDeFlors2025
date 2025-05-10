package com.example.tempsdeflors

import android.content.Context
import androidx.room.Room

object DatabaseManager {

    private var database: AppDatabase? = null
    private val puntsVisitats = mutableListOf<PuntsEntity>()


    fun init(context: Context) {
        if (database == null) {
            database = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "punts-db"
            ).build()
        }
    }

    fun getDatabase(): AppDatabase {
        return database
            ?: throw IllegalStateException("DatabaseManager not initialized. Call init(context) first.")
    }

    fun getPuntsDao(): PuntsDao? {
        return getDatabase().puntsDao()
    }
}
