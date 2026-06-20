package com.example.data

import kotlinx.coroutines.flow.Flow

class DocumentRepository(private val documentDao: DocumentDao) {
    val allDocuments: Flow<List<DocumentEntity>> = documentDao.getAllDocuments()

    fun getDocumentsByType(type: String): Flow<List<DocumentEntity>> {
        return documentDao.getDocumentsByType(type)
    }

    fun getDocumentById(id: Int): Flow<DocumentEntity?> {
        return documentDao.getDocumentById(id)
    }

    suspend fun getDocumentByIdSuspended(id: Int): DocumentEntity? {
        return documentDao.getDocumentByIdSuspended(id)
    }

    suspend fun insertDocument(document: DocumentEntity): Long {
        return documentDao.insertDocument(document)
    }

    suspend fun updateDocument(document: DocumentEntity) {
        documentDao.updateDocument(document)
    }

    suspend fun deleteDocument(document: DocumentEntity) {
        documentDao.deleteDocument(document)
    }

    suspend fun deleteDocumentById(id: Int) {
        documentDao.deleteDocumentById(id)
    }
}
