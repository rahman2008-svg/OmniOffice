package com.example.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed class Screen {
    object Dashboard : Screen()
    data class WordEditor(val docId: Int) : Screen()
    data class SheetEditor(val docId: Int) : Screen()
    data class SlideEditor(val docId: Int) : Screen()
    data class PdfReader(val docId: Int) : Screen()
}

class DocumentViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: DocumentRepository
    
    // Screen backstack navigation
    private val _screenStack = MutableStateFlow<List<Screen>>(listOf(Screen.Dashboard))
    val currentScreen: StateFlow<Screen> = _screenStack
        .map { it.lastOrNull() ?: Screen.Dashboard }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Screen.Dashboard)

    // Filters & Selections
    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow("ALL") // "ALL", "WORD", "SHEET", "SLIDE", "PDF"

    init {
        val database = AppDatabase.getDatabase(application)
        repository = DocumentRepository(database.documentDao())

        // Populate beautiful default templates on initial database creation
        viewModelScope.launch {
            repository.allDocuments.first().let { list ->
                if (list.isEmpty()) {
                    populateDefaultTemplates()
                }
            }
        }
    }

    // Observe documents and perform reactive searching + category filtering in Kotlin Flow
    val filteredDocuments: StateFlow<List<DocumentEntity>> = combine(
        repository.allDocuments,
        searchQuery,
        selectedCategory
    ) { allDocs, query, cat ->
        allDocs.filter { doc ->
            val matchesCategory = if (cat == "ALL") true else doc.type == cat
            val matchesSearch = doc.title.contains(query, ignoreCase = true) || 
                                doc.type.contains(query, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Backstack management
    fun navigateTo(screen: Screen) {
        val current = _screenStack.value.toMutableList()
        current.add(screen)
        _screenStack.value = current
    }

    fun goBack() {
        val current = _screenStack.value.toMutableList()
        if (current.size > 1) {
            current.removeAt(current.size - 1)
            _screenStack.value = current
        }
    }

    fun observeDocumentById(id: Int): Flow<DocumentEntity?> {
        return repository.getDocumentById(id)
    }

    // CRUD Ops
    fun createNewDocument(title: String, type: String, templateName: String? = null) {
        viewModelScope.launch {
            val df = SimpleDateFormat("MMM dd, yyyy", Locale.US)
            val desc = "Created on ${df.format(Date())}"
            
            val initialContent = when (type) {
                "WORD" -> {
                    if (templateName == "Resume") {
                        "**JOHN DOE**\nSoftware Engineer | San Francisco, CA\n\n*SUMMARY*\nDynamic developer with expertise in Android Jetpack Compose and offline system architecture.\n\n*EXPERIENCE*\nSenior Developer - Omni Corp (2024 - Present)\n• Designed premium modular offline databases saving 30% storage footprint.\n• Implemented Material 3 grid visualizers.\n\n*EDUCATION*\nB.S. Computer Science - Stanford University"
                    } else if (templateName == "Memo") {
                        "**MEMORANDUM**\n\nTO: All Staff Members\nFROM: Management\nDATE: June 20, 2026\nSUBJECT: OmniOffice Cloudless Deployment Release\n\nThis memo details our complete transition into a secure, offline-first workflow system. Please verify local files before sign-off."
                    } else {
                        "Welcome to your new document.\n\nStart typing here. Utilize the format options toolbar above to customize typography, text borders, alignment and headings."
                    }
                }
                "SHEET" -> {
                    if (templateName == "Budget") {
                        val cells = mapOf(
                            "A1" to SheetCell("Monthly Budget", isBold = true, textColorHex = "#1D2D50", bgHex = "#E0EAFC"),
                            "A2" to SheetCell("Category", isBold = true, bgHex = "#F4F6F9"),
                            "B2" to SheetCell("Allocated", isBold = true, bgHex = "#F4F6F9"),
                            "C2" to SheetCell("Actual", isBold = true, bgHex = "#F4F6F9"),
                            "A3" to SheetCell("Rent / Living"), "B3" to SheetCell("1500"), "C3" to SheetCell("1500"),
                            "A4" to SheetCell("Groceries"), "B4" to SheetCell("400"), "C4" to SheetCell("385"),
                            "A5" to SheetCell("Utilities"), "B5" to SheetCell("250"), "C5" to SheetCell("280"),
                            "A6" to SheetCell("Entertainment"), "B6" to SheetCell("200"), "C6" to SheetCell("180"),
                            "A7" to SheetCell("Total Sum", isBold = true, bgHex = "#E8F0FE"),
                            "B7" to SheetCell("=SUM(B3:B6)", isBold = true, bgHex = "#E8F0FE"),
                            "C7" to SheetCell("=SUM(C3:C6)", isBold = true, bgHex = "#E8F0FE")
                        )
                        SheetSerializer.serializeMap(cells)
                    } else {
                        val cells = mapOf(
                            "A1" to SheetCell("Item", isBold = true, bgHex = "#F0F4F8"),
                            "B1" to SheetCell("Qty", isBold = true, bgHex = "#F0F4F8"),
                            "C1" to SheetCell("Rate", isBold = true, bgHex = "#F0F4F8"),
                            "D1" to SheetCell("Total", isBold = true, bgHex = "#F0F4F8"),
                            "A2" to SheetCell("Tablet Cases"), "B2" to SheetCell("10"), "C2" to SheetCell("25"), "D2" to SheetCell("=PRODUCT(B2:C2)"),
                            "A3" to SheetCell("Styli Pens"), "B3" to SheetCell("5"), "C3" to SheetCell("40"), "D3" to SheetCell("=PRODUCT(B3:C3)"),
                            "A4" to SheetCell("Keyboard Attachments"), "B4" to SheetCell("2"), "C4" to SheetCell("120"), "D4" to SheetCell("=PRODUCT(B4:C4)"),
                            "A5" to SheetCell("Grand Total", isBold = true, bgHex = "#E6FFFA"), "D5" to SheetCell("=SUM(D2:D4)", isBold = true, bgHex = "#E6FFFA")
                        )
                        SheetSerializer.serializeMap(cells)
                    }
                }
                "SLIDE" -> {
                    if (templateName == "Pitch") {
                        val slides = listOf(
                            Slide("OmniSync Pitch", "Reinventing Offline Productivity", "Presenter: executive team\nConfidential internal review Only", "Ocean Mint", "TITLE"),
                            Slide("The Vision", "Why Edge Computing Wins", "• Absolute Privacy: No servers, no leaks\n• Sub-millisecond latency edits\n• 100% field readiness anywhere", "Sleek Dark", "CONTENT"),
                            Slide("Q1 Product Pipeline", "Core Modules", "• docx editor engine\n• xlsx cells math compiler\n• pptx custom presenter layers", "Lavender Rose", "SPLIT")
                        )
                        SlideSerializer.serializeList(slides)
                    } else {
                        val slides = listOf(
                            Slide("Untitled Presentation", "Build something amazing", "• Click '+' below to insert slides\n• Layout toggle options available\n• Slide presentation rendering mode enabled", "Sunset Glow", "TITLE")
                        )
                        SlideSerializer.serializeList(slides)
                    }
                }
                "PDF" -> {
                    // Pre-annotated lists
                    val annotations = listOf(
                        PdfAnnotation(0, "HIGHLIGHT", "#FFFF00", 0.15f, 0.22f, "Highlighting key security clauses"),
                        PdfAnnotation(0, "NOTE", "#438A5E", 0.82f, 0.18f, "Verified: compliance matches Q1 local safety bar.")
                    )
                    PdfAnnotationSerializer.serializeList(annotations)
                }
                else -> ""
            }

            val doc = DocumentEntity(
                title = title,
                type = type,
                content = initialContent,
                templateName = templateName,
                lastModified = System.currentTimeMillis(),
                fileSize = when (type) {
                    "WORD" -> "15 KB"
                    "SHEET" -> "8 KB"
                    "SLIDE" -> "32 KB"
                    else -> "144 KB"
                }
            )
            val newId = repository.insertDocument(doc)
            navigateTo(
                when (type) {
                    "WORD" -> Screen.WordEditor(newId.toInt())
                    "SHEET" -> Screen.SheetEditor(newId.toInt())
                    "SLIDE" -> Screen.SlideEditor(newId.toInt())
                    else -> Screen.PdfReader(newId.toInt())
                }
            )
        }
    }

    fun updateDocumentContent(id: Int, newContent: String) {
        viewModelScope.launch {
            val doc = repository.getDocumentByIdSuspended(id)
            if (doc != null) {
                val updated = doc.copy(
                    content = newContent,
                    lastModified = System.currentTimeMillis()
                )
                repository.updateDocument(updated)
            }
        }
    }

    fun updateDocumentTitle(id: Int, newTitle: String) {
        viewModelScope.launch {
            val doc = repository.getDocumentByIdSuspended(id)
            if (doc != null) {
                val updated = doc.copy(
                    title = newTitle,
                    lastModified = System.currentTimeMillis()
                )
                repository.updateDocument(updated)
            }
        }
    }

    fun toggleStarred(id: Int) {
        viewModelScope.launch {
            val doc = repository.getDocumentByIdSuspended(id)
            if (doc != null) {
                val updated = doc.copy(isStarred = !doc.isStarred)
                repository.updateDocument(updated)
            }
        }
    }

    fun deleteDocument(id: Int) {
        viewModelScope.launch {
            repository.deleteDocumentById(id)
        }
    }

    private suspend fun populateDefaultTemplates() {
        val pdfContent1 = PdfAnnotationSerializer.serializeList(listOf(
            PdfAnnotation(0, "HIGHLIGHT", "#FFEB3B", 0.12f, 0.35f, "Employee standard welcome"),
            PdfAnnotation(0, "NOTE", "#2196F3", 0.75f, 0.40f, "Please return standard form B within 5 business days")
        ))
        val pdfContent2 = PdfAnnotationSerializer.serializeList(listOf(
            PdfAnnotation(0, "HIGHLIGHT", "#8BC34A", 0.12f, 0.20f, "Overview of compiler engine pipeline"),
            PdfAnnotation(1, "HIGHLIGHT", "#FF9800", 0.30f, 0.55f, "Required Gradle memory sizing optimizations")
        ))

        val templates = listOf(
            DocumentEntity(
                title = "Resume Candidate Profile.docx",
                type = "WORD",
                content = "**ALEX SMITH**\nProduct Manager | Alex@omnioffice.io\n\n*PROFESSIONAL BACKGROUND*\nExperienced team captain specializing in zero-latency offline synchronization protocols.\n\n*NOTABLE PROJECTS*\nOmniWord Release v2\n• Scaled docx text parsing capabilities to millions of local actions per minute.\n• Created zero-latency state controllers.",
                templateName = "Resume",
                fileSize = "24 KB"
            ),
            DocumentEntity(
                title = "Project Launch Memo.docx",
                type = "WORD",
                content = "**OMNIOFFICE MEMORANDUM**\n\nTO: All Department Heads\nFROM: Technical Solutions Architect\nDATE: June 20, 2026\n\nWe have achieved stable code compilations! The local Room databases run beautifully, supporting dynamic insets and edge-to-edge rendering templates. Let's maintain this high level of architectural excellence.",
                templateName = "Memo",
                fileSize = "18 KB"
            ),
            DocumentEntity(
                title = "Corporate Budget Q2.xlsx",
                type = "SHEET",
                content = SheetSerializer.serializeMap(mapOf(
                    "A1" to SheetCell("Department Q2 Balances", isBold = true, bgHex = "#ECEFF1"),
                    "A2" to SheetCell("Division"), "B2" to SheetCell("Q1 Spending"), "C2" to SheetCell("Q2 Cap"), "D2" to SheetCell("Remaining"),
                    "A3" to SheetCell("Engineering"), "B3" to SheetCell("85000"), "C3" to SheetCell("120000"), "D3" to SheetCell("=SUBTRACT(C3:B3)"),
                    "A4" to SheetCell("Design Lab"), "B4" to SheetCell("32000"), "C4" to SheetCell("45000"), "D4" to SheetCell("=SUBTRACT(C4:B4)"),
                    "A5" to SheetCell("Customer Growth"), "B5" to SheetCell("18000"), "C5" to SheetCell("25000"), "D5" to SheetCell("=SUBTRACT(C5:B5)"),
                    "A6" to SheetCell("Total Budget Sum", isBold = true, bgHex = "#DCEDC8"),
                    "B6" to SheetCell("=SUM(B3:B5)", isBold = true, bgHex = "#DCEDC8"),
                    "C6" to SheetCell("=SUM(C3:C5)", isBold = true, bgHex = "#DCEDC8"),
                    "D6" to SheetCell("=SUM(D3:D5)", isBold = true, bgHex = "#DCEDC8")
                )),
                templateName = "Budget",
                fileSize = "11 KB"
            ),
            DocumentEntity(
                title = "OmniSync Investors deck.pptx",
                type = "SLIDE",
                content = SlideSerializer.serializeList(listOf(
                    Slide("OmniOffice 2026", "Building the Future of Local Productivity", "• Presenters: Senior Leadership Team\n• Location: San Francisco, CA\n• Mission: Elevate local edge computing with Material 3 styling.", "Ocean Mint", "TITLE"),
                    Slide("Problem Statement", "The Internet is Unreliable", "• Server outages disrupt important enterprise documents\n• Data leaks compromise private confidential profiles\n• Sync conflicts destroy hours of progress", "Sleek Dark", "CONTENT"),
                    Slide("The OmniOffice Solution", "100% Offline Resilience", "• Instant launch times (<20ms)\n• Full-range local editing features\n• Cryptographically secure offline sandbox storage", "Lavender Rose", "CONTENT")
                )),
                templateName = "Pitch",
                fileSize = "38 KB"
            ),
            DocumentEntity(
                title = "Employee Handbook.pdf",
                type = "PDF",
                content = pdfContent1,
                fileSize = "1.2 MB"
            ),
            DocumentEntity(
                title = "OmniOffice Tech Specs.pdf",
                type = "PDF",
                content = pdfContent2,
                fileSize = "840 KB"
            )
        )

        for (temp in templates) {
            repository.insertDocument(temp)
        }
    }
}
