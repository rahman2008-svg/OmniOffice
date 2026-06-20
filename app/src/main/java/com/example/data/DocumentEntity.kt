package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "documents")
data class DocumentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val type: String, // "WORD", "SHEET", "SLIDE", "PDF"
    val content: String, // Rich-text string, or serialized spreadsheet cells/slide decks/annotations
    val isStarred: Boolean = false,
    val templateName: String? = null,
    val lastModified: Long = System.currentTimeMillis(),
    val fileSize: String = "12 KB"
)
