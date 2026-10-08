package com.example.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.database.entities.QrScanEntity
import com.example.data.database.entities.ScannedDocEntity
import com.example.data.database.entities.WatermarkPresetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QrScanDao {
    @Query("SELECT * FROM qr_scans ORDER BY timestamp DESC")
    fun getAllScans(): Flow<List<QrScanEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScan(scan: QrScanEntity): Long

    @Query("DELETE FROM qr_scans WHERE id = :id")
    suspend fun deleteScan(id: Long)

    @Query("DELETE FROM qr_scans")
    suspend fun clearAll()
}

@Dao
interface WatermarkPresetDao {
    @Query("SELECT * FROM watermark_presets ORDER BY id ASC")
    fun getAllPresets(): Flow<List<WatermarkPresetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreset(preset: WatermarkPresetEntity): Long

    @Query("DELETE FROM watermark_presets WHERE id = :id")
    suspend fun deletePreset(id: Long)
}

@Dao
interface ScannedDocDao {
    @Query("SELECT * FROM scanned_documents ORDER BY createdAt DESC")
    fun getAllDocs(): Flow<List<ScannedDocEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDoc(doc: ScannedDocEntity): Long

    @Query("DELETE FROM scanned_documents WHERE id = :id")
    suspend fun deleteDoc(id: Long)
}
