package com.tribalfs.stargazers.data.local

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.tribalfs.stargazers.data.StargazersRepo
import com.tribalfs.stargazers.data.model.Stargazer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
                    "stargazer_database"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            fetchInitialStargazers(context)
                        }
                    })
                    .build().also { INSTANCE = it }
            }


        private fun fetchInitialStargazers(context: Context) {
            StargazersRepo.getInstance(context).apply {
                CoroutineScope(Dispatchers.IO).launch {
                    refreshStargazers()
                }
            }
        }
    }
}