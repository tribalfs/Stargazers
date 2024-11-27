package com.tribalfs.stargazers.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import androidx.room.AutoMigration
import com.tribalfs.stargazers.data.model.Stargazer

@Database(entities = [Stargazer::class],
    version = 2,
    autoMigrations = [
        AutoMigration(from = 1, to = 2)
    ])
abstract class StargazersDB : RoomDatabase() {
    abstract fun stargazerDao(): StargazersDao

    companion object {
        @Volatile
        private var INSTANCE: StargazersDB? = null

        fun getDatabase(context: Context): StargazersDB =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    StargazersDB::class.java,
                    "stargazer_database").build().also { INSTANCE = it }
            }
    }
}