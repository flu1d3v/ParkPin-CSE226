package com.fluid.parkpin.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SpotDao {
    @Query("SELECT * FROM parking_spots ORDER BY savedAt DESC")
    fun observeAll(): Flow<List<ParkingSpot>>

    @Insert
    suspend fun insert(spot: ParkingSpot): Long

    @Query("UPDATE parking_spots SET label = :label WHERE id = :id")
    suspend fun setLabel(id: Long, label: String)

    @Query("UPDATE parking_spots SET photoPath = :path, tags = :tags WHERE id = :id")
    suspend fun setPhoto(id: Long, path: String?, tags: String?)

    @Delete
    suspend fun delete(spot: ParkingSpot)
}
