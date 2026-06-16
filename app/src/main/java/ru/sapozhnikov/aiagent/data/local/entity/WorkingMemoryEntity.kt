package ru.sapozhnikov.aiagent.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Room-сущность экземпляра рабочей памяти. */
@Entity(tableName = "working_memory")
internal data class WorkingMemoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val text: String,
    val createdAt: Long,
)
