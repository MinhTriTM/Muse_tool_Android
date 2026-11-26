package com.musetool.superapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MuseDao {
    @Query("SELECT * FROM usage_logs ORDER BY timestamp DESC")
    fun observeAllUsageLogs(): Flow<List<UsageLogEntity>>

    @Query("SELECT COUNT(*) FROM usage_logs")
    suspend fun getUsageLogCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsageLog(log: UsageLogEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsageLogs(logs: List<UsageLogEntity>)

    @Query("DELETE FROM usage_logs WHERE id = :id")
    suspend fun deleteUsageLogById(id: Long)

    @Query("DELETE FROM usage_logs")
    suspend fun clearAllUsageLogs()

    @Query("SELECT * FROM ota_releases ORDER BY versionCode DESC")
    fun observeOtaReleases(): Flow<List<OtaReleaseEntity>>

    @Query("SELECT COUNT(*) FROM ota_releases")
    suspend fun getOtaReleaseCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOtaReleases(releases: List<OtaReleaseEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOtaRelease(release: OtaReleaseEntity)

    @Query("UPDATE ota_releases SET isInstalled = CASE WHEN versionName = :versionName THEN 1 ELSE isInstalled END, installedTimestamp = CASE WHEN versionName = :versionName THEN :installedAt ELSE installedTimestamp END")
    suspend fun markOtaVersionInstalled(versionName: String, installedAt: Long)

    @Query("SELECT * FROM prompt_templates ORDER BY isPinned DESC, id ASC")
    fun observePromptTemplates(): Flow<List<PromptTemplateEntity>>

    @Query("SELECT COUNT(*) FROM prompt_templates")
    suspend fun getPromptTemplateCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPromptTemplates(templates: List<PromptTemplateEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPromptTemplate(template: PromptTemplateEntity)

    @Update
    suspend fun updatePromptTemplate(template: PromptTemplateEntity)

    @Query("DELETE FROM prompt_templates WHERE id = :id")
    suspend fun deletePromptTemplate(id: Long)
}
