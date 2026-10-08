package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.database.dao.QrScanDao
import com.example.data.database.dao.ScannedDocDao
import com.example.data.database.dao.WatermarkPresetDao
import com.example.data.database.entities.QrScanEntity
import com.example.data.database.entities.ScannedDocEntity
import com.example.data.database.entities.WatermarkPresetEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [QrScanEntity::class, WatermarkPresetEntity::class, ScannedDocEntity::class],
    version = 1,
    exportSchema = false
)
abstract class WafaDatabase : RoomDatabase() {
    abstract fun qrScanDao(): QrScanDao
    abstract fun watermarkPresetDao(): WatermarkPresetDao
    abstract fun scannedDocDao(): ScannedDocDao

    companion object {
        @Volatile
        private var INSTANCE: WafaDatabase? = null

        fun getInstance(context: Context): WafaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WafaDatabase::class.java,
                    "wafa_camera_pro.db"
                ).addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            val presetDao = getInstance(context).watermarkPresetDao()
                            presetDao.insertPreset(
                                WatermarkPresetEntity(
                                    title = "Developer Credit",
                                    text = "Developed by Mehedi364",
                                    position = "BOTTOM_RIGHT",
                                    includeTimestamp = true,
                                    isDefault = true
                                )
                            )
                            presetDao.insertPreset(
                                WatermarkPresetEntity(
                                    title = "Wafa Pro Minimal",
                                    text = "Wafa Camera Pro",
                                    position = "BOTTOM_LEFT",
                                    includeTimestamp = false
                                )
                            )
                            presetDao.insertPreset(
                                WatermarkPresetEntity(
                                    title = "Timestamp Only",
                                    text = "",
                                    position = "BOTTOM_RIGHT",
                                    includeTimestamp = true
                                )
                            )
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
