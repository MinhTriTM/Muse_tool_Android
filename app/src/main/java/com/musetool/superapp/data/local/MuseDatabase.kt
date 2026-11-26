package com.musetool.superapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UsageLogEntity::class,
        OtaReleaseEntity::class,
        PromptTemplateEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class MuseDatabase : RoomDatabase() {
    abstract fun museDao(): MuseDao

    companion object {
        @Volatile
        private var INSTANCE: MuseDatabase? = null

        fun getInstance(context: Context): MuseDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MuseDatabase::class.java,
                    "muse_tool_superapp.db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
