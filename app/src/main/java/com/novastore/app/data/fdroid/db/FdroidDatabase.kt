package com.novastore.app.data.fdroid.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [FdroidEntity::class], version = 1, exportSchema = false)
abstract class FdroidDatabase : RoomDatabase() {
    abstract fun fdroidDao(): FdroidDao

    companion object {
        @Volatile
        private var INSTANCE: FdroidDatabase? = null

        fun get(context: Context): FdroidDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    FdroidDatabase::class.java,
                    "fdroid_database.db"
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
        }
    }
}
