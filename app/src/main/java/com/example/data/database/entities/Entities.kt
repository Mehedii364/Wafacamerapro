package com.example.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "qr_scans")
data class QrScanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val content: String,
    val format: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false
)

@Entity(tableName = "watermark_presets")
data class WatermarkPresetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val text: String,
    val position: String = "BOTTOM_RIGHT", // TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT, CENTER
    val textColor: Long = 0xFFFFFFFF,
    val textSizeSp: Float = 16f,
    val opacity: Float = 0.85f,
    val includeTimestamp: Boolean = true,
    val isDefault: Boolean = false
)

@Entity(tableName = "scanned_documents")
data class ScannedDocEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val pageCount: Int,
    val fileUri: String,
    val createdAt: Long = System.currentTimeMillis()
)
