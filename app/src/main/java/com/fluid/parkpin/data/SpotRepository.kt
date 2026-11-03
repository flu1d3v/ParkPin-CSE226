package com.fluid.parkpin.data

import kotlinx.coroutines.flow.Flow

/** The only way the UI reaches the database. */
class SpotRepository(private val dao: SpotDao) {
    val spots: Flow<List<ParkingSpot>> = dao.observeAll()

    suspend fun save(spot: ParkingSpot): Long = dao.insert(spot)
    suspend fun rename(id: Long, label: String) = dao.setLabel(id, label)
    suspend fun setPhoto(id: Long, path: String?, tags: String?) = dao.setPhoto(id, path, tags)
    suspend fun delete(spot: ParkingSpot) = dao.delete(spot)
}
