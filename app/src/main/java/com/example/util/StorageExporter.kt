package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import com.example.data.DocumentEntity
import com.example.data.SheetCell
import com.example.data.SheetSerializer
import com.example.data.SlideSerializer
import java.io.File

object StorageExporter {
    
    data class ExportResult(
        val isSuccess: Boolean,
        val filePath: String,
        val message: String,
        val fileUri: Uri? = null
    )

    fun exportDocument(context: Context, doc: DocumentEntity): ExportResult {
        try {
            val fileName = sanitizeFileName(doc.title, doc.type)
            val fileContent = getFormattedContent(doc)
            
            // App-specific external documents directory (always works without permissions)
            val appExternalDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) 
                ?: context.filesDir
            
            val appFile = File(appExternalDir, fileName)
            appFile.writeText(fileContent)

            // Try to write to the main public Download folder (primary user-facing memory location)
            val publicDownloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            var publicFilePath = ""
            var copySuccess = false
            
            if (publicDownloadDir != null) {
                if (!publicDownloadDir.exists()) {
                    publicDownloadDir.mkdirs()
                }
                val publicFile = File(publicDownloadDir, fileName)
                try {
                    publicFile.writeText(fileContent)
                    publicFilePath = publicFile.absolutePath
                    copySuccess = true
                } catch (e: Exception) {
                    // Swallow: Scoped storage restrictions may block direct write on some SDK versions
                }
            }

            val finalPath = if (copySuccess) publicFilePath else appFile.absolutePath
            val displayMessage = if (copySuccess) {
                "Document successfully exported to public storage:\n\n$finalPath"
            } else {
                "Saved locally in application sandbox storage:\n\n$finalPath"
            }

            // Create file URI via FileProvider for sharing
            val authority = "${context.packageName}.fileprovider"
            val fileUri = try {
                FileProvider.getUriForFile(context, authority, appFile)
            } catch (e: Exception) {
                Uri.fromFile(appFile)
            }

            return ExportResult(
                isSuccess = true,
                filePath = finalPath,
                message = displayMessage,
                fileUri = fileUri
            )
        } catch (e: Exception) {
            return ExportResult(
                isSuccess = false,
                filePath = "",
                message = "Failed to export document: ${e.localizedMessage}"
            )
        }
    }

    private fun sanitizeFileName(title: String, type: String): String {
        val clean = title.replace(Regex("[\\\\/:*?\"<>|]"), "_")
        val ext = when (type) {
            "WORD" -> if (clean.endsWith(".txt", ignoreCase = true) || clean.endsWith(".docx", ignoreCase = true)) "" else ".txt"
            "SHEET" -> if (clean.endsWith(".csv", ignoreCase = true) || clean.endsWith(".xlsx", ignoreCase = true)) "" else ".csv"
            "SLIDE" -> if (clean.endsWith(".txt", ignoreCase = true) || clean.endsWith(".pptx", ignoreCase = true)) "" else ".txt"
            "PDF" -> if (clean.endsWith(".pdf", ignoreCase = true)) "" else ".pdf"
            else -> ".txt"
        }
        return clean + ext
    }

    private fun getFormattedContent(doc: DocumentEntity): String {
        return when (doc.type) {
            "WORD" -> doc.content // Standard rich/plain text content
            "SHEET" -> {
                // Return Tabular CSV Content
                try {
                    val cells = SheetSerializer.deserializeMap(doc.content)
                    formatSheetToCsv(cells)
                } catch (e: Exception) {
                    doc.content
                }
            }
            "SLIDE" -> {
                // Return custom structural presentation outlines
                try {
                    val slides = SlideSerializer.deserializeList(doc.content)
                    slides.joinToString("\n\n" + "=".repeat(40) + "\n\n") { slide ->
                        "【 SLIDE: ${slide.title} 】\n" +
                        "Theme Schema: ${slide.bgTheme} | Slide Layout: ${slide.layout}\n" +
                        "-".repeat(20) + "\n" +
                        "Subtitle: ${slide.subtitle}\n" +
                        "-".repeat(20) + "\n" +
                        "Body Outline:\n${slide.body}"
                    }
                } catch (e: Exception) {
                    doc.content
                }
            }
            else -> {
                // PDF details export
                "PDF TITLE: ${doc.title}\n" + "Contains offline electronic form markup.\n\n" + doc.content
            }
        }
    }

    private fun formatSheetToCsv(cells: Map<String, SheetCell>): String {
        val maxRow = 15
        val maxColChar = 'G'
        val sb = StringBuilder()
        
        for (r in 1..maxRow) {
            val rowCells = mutableListOf<String>()
            for (c in 'A'..maxColChar) {
                val key = "$c$r"
                val cell = cells[key]
                val valStr = cell?.value ?: ""
                val safeVal = if (valStr.contains(",") || valStr.contains("\"") || valStr.contains("\n")) {
                    "\"" + valStr.replace("\"", "\"\"") + "\""
                } else {
                    valStr
                }
                rowCells.add(safeVal)
            }
            sb.append(rowCells.joinToString(",")).append("\n")
        }
        return sb.toString()
    }

    fun triggerShare(context: Context, res: ExportResult, docTitle: String) {
        val uri = res.fileUri ?: return
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Export: $docTitle")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share or Save File"))
    }
}
