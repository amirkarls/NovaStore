package com.novastore.app.data.fdroid.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface FdroidDao {

    @Query("SELECT COUNT(*) FROM fdroid_apps")
    suspend fun count(): Int

    @Query("SELECT * FROM fdroid_apps ORDER BY name LIMIT :limit OFFSET :offset")
    suspend fun getPage(limit: Int, offset: Int): List<FdroidEntity>

    @Query("SELECT * FROM fdroid_apps WHERE name LIKE '%' || :query || '%' OR summary LIKE '%' || :query || '%' OR author LIKE '%' || :query || '%' OR packageName LIKE '%' || :query || '%' ORDER BY name LIMIT :limit OFFSET :offset")
    suspend fun searchPage(query: String, limit: Int, offset: Int): List<FdroidEntity>

    @Query("SELECT COUNT(*) FROM fdroid_apps WHERE name LIKE '%' || :query || '%' OR summary LIKE '%' || :query || '%' OR author LIKE '%' || :query || '%' OR packageName LIKE '%' || :query || '%'")
    suspend fun searchCount(query: String): Int

    @Query("SELECT * FROM fdroid_apps WHERE packageName = :pkg LIMIT 1")
    suspend fun getByPackage(pkg: String): FdroidEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(apps: List<FdroidEntity>)

    @Query("DELETE FROM fdroid_apps")
    suspend fun clear()
}
