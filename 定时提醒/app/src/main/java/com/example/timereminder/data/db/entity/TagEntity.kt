package com.example.timereminder.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.timereminder.domain.model.Tag

@Entity(tableName = "tags")
data class TagEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,

    val color: Int = 0xFF448AFF.toInt(),

    val icon: String? = null
) {
    fun toDomainModel(): Tag {
        return Tag(
            id = id,
            name = name,
            color = color,
            icon = icon
        )
    }

    companion object {
        fun fromDomainModel(tag: Tag): TagEntity {
            return TagEntity(
                id = tag.id,
                name = tag.name,
                color = tag.color,
                icon = tag.icon
            )
        }
    }
}
