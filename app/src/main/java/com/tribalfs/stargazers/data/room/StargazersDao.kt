package com.tribalfs.stargazers.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tribalfs.stargazers.data.model.Stargazer
import kotlinx.coroutines.flow.Flow

@Dao
interface StargazersDao {
    @Query("SELECT * FROM stargazers")
    fun getAllStargazers(): Flow<List<Stargazer>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(stargazers: List<Stargazer>)

    @Query("DELETE FROM stargazers")
    suspend fun clear()

}