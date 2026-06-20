package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {
    @Query("SELECT * FROM documents ORDER BY lastModified DESC")
    fun getAllDocuments(): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE type = :type ORDER BY lastModified DESC")
    fun getDocumentsByType(type: String): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE id = :id")
    fun getDocumentById(id: Int): Flow<DocumentEntity?>

    @Query("SELECT * FROM documents WHERE id = :id")
    suspend fun getDocumentByIdSuspended(id: Int): DocumentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: DocumentEntity): Long

    @Update
    suspend fun updateDocument(document: DocumentEntity)

    @Delete
    suspend fun deleteDocument(document: DocumentEntity)

    @Query("DELETE FROM documents WHERE id = :id")
    suspend fun deleteDocumentById(id: Int)
}
