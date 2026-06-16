package ru.sapozhnikov.aiagent.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Room-сущность экземпляра долговременной (профильной) памяти. */
@Entity(tableName = "profile_memory")
internal data class ProfileMemoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val text: String,
    val createdAt: Long,
)
