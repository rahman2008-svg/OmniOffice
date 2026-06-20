package com.example.data

// ==========================================
// 1. Spreadsheet (Sheets) Data Model
// ==========================================
data class SheetCell(
    val value: String = "",
    val isBold: Boolean = false,
    val textColorHex: String = "#1A1A1A",
    val bgHex: String = "#FFFFFF"
) {
    fun serialize(): String {
        return "$value|$isBold|$textColorHex|$bgHex"
    }

    companion object {
        fun deserialize(serialized: String): SheetCell {
            val parts = serialized.split("|")
            if (parts.size < 4) return SheetCell()
            return SheetCell(
                value = parts[0],
                isBold = parts[1].toBoolean(),
                textColorHex = parts[2],
                bgHex = parts[3]
            )
        }
    }
}

// Helper to serialize high-performance Map<String, SheetCell> keys: "A1", "C4"
object SheetSerializer {
    fun serializeMap(map: Map<String, SheetCell>): String {
        return map.entries.joinToString(";") { "${it.key}>>${it.value.serialize()}" }
    }

    fun deserializeMap(serialized: String): Map<String, SheetCell> {
        if (serialized.isBlank()) return emptyMap()
        val result = mutableMapOf<String, SheetCell>()
        try {
            val rows = serialized.split(";")
            for (row in rows) {
                if (row.isBlank()) continue
                val parts = row.split(">>")
                if (parts.size == 2) {
                    result[parts[0]] = SheetCell.deserialize(parts[1])
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }
}

// ==========================================
// 2. Presentation (Slides) Data Model
// ==========================================
data class Slide(
    val title: String = "Untitled Slide",
    val subtitle: String = "Slide subtitle details here...",
    val body: String = "Bullet point 1\nBullet point 2\nBullet point 3",
    val bgTheme: String = "Ocean Mint", // "Ocean Mint", "Sleek Dark", "Lavender Rose", "Sunset Glow"
    val layout: String = "TITLE" // "TITLE", "CONTENT", "SPLIT", "STATISTICS"
) {
    fun serialize(): String {
        // Use double-caret ^ to avoid splitting on common punctuation in titles or text bodies
        val titleEsc = title.replace("^", "").replace("~", "")
        val subtitleEsc = subtitle.replace("^", "").replace("~", "")
        val bodyEsc = body.replace("^", "").replace("~", "")
        return "$titleEsc^$subtitleEsc^$bodyEsc^$bgTheme^$layout"
    }

    companion object {
        fun deserialize(serialized: String): Slide {
            val parts = serialized.split("^")
            if (parts.size < 5) return Slide()
            return Slide(
                title = parts[0],
                subtitle = parts[1],
                body = parts[2],
                bgTheme = parts[3],
                layout = parts[4]
            )
        }
    }
}

object SlideSerializer {
    fun serializeList(slides: List<Slide>): String {
        return slides.joinToString("~") { it.serialize() }
    }

    fun deserializeList(serialized: String): List<Slide> {
        if (serialized.isBlank()) return listOf(Slide("Welcome to Slides", "Double tap to present", "• Modern Jetpack Layouts\n• Offline capabilities\n• Professional rendering", "Ocean Mint", "TITLE"))
        return try {
            serialized.split("~").filter { it.isNotBlank() }.map { Slide.deserialize(it) }
        } catch (e: Exception) {
            listOf(Slide())
        }
    }
}

// ==========================================
// 3. PDF Annotations Data Model
// ==========================================
data class PdfAnnotation(
    val pageIndex: Int,
    val type: String, // "HIGHLIGHT", "NOTE", "SIGN", "STAMP"
    val colorHex: String,
    val xPercent: Float,
    val yPercent: Float,
    val extraText: String = ""
) {
    fun serialize(): String {
        val extraTextEsc = extraText.replace("^", "")
        return "$pageIndex^$type^$colorHex^$xPercent^$yPercent^$extraTextEsc"
    }

    companion object {
        fun deserialize(serialized: String): PdfAnnotation {
            val parts = serialized.split("^")
            if (parts.size < 6) return PdfAnnotation(0, "HIGHLIGHT", "#FFFF00", 0.0f, 0.0f)
            return PdfAnnotation(
                pageIndex = parts[0].toIntOrNull() ?: 0,
                type = parts[1],
                colorHex = parts[2],
                xPercent = parts[3].toFloatOrNull() ?: 0f,
                yPercent = parts[4].toFloatOrNull() ?: 0f,
                extraText = parts[5]
            )
        }
    }
}

object PdfAnnotationSerializer {
    fun serializeList(list: List<PdfAnnotation>): String {
        return list.joinToString("~") { it.serialize() }
    }

    fun deserializeList(serialized: String): List<PdfAnnotation> {
        if (serialized.isBlank()) return emptyList()
        return try {
            serialized.split("~").filter { it.isNotBlank() }.map { PdfAnnotation.deserialize(it) }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
