package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DocumentViewModel
import com.example.data.PdfAnnotation
import com.example.data.PdfAnnotationSerializer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfReaderScreen(
    viewModel: DocumentViewModel,
    docId: Int,
    modifier: Modifier = Modifier
) {
    val documentFlow = remember(docId) { viewModel.observeDocumentById(docId) }
    val document by documentFlow.collectAsState(initial = null)

    var annotations by remember { mutableStateOf<List<PdfAnnotation>>(emptyList()) }
    var titleText by remember { mutableStateOf("") }
    var isInitialized by remember { mutableStateOf(false) }

    // Page state
    var activePage by remember { mutableStateOf(0) }
    val totalPages = 2

    // Annotation Toolbar Selections
    var currentTool by remember { mutableStateOf("NONE") } // "NONE", "HIGHLIGHT", "NOTE", "SIGN", "STAMP"
    var activeBrushColorHex by remember { mutableStateOf("#0F172A") }
    var activeStampText by remember { mutableStateOf("APPROVED") }
    var activeStampColorHex by remember { mutableStateOf("#10B981") }
    
    // Notes/Comments editing popups
    var showCommentDialog by remember { mutableStateOf(false) }
    var notePositionClicked by remember { mutableStateOf(Offset(0f, 0f)) }
    var commentTextValue by remember { mutableStateOf("") }
    var activeCommentToView by remember { mutableStateOf<PdfAnnotation?>(null) }

    // Freehand drawing stroke coordinates (temporary before saving)
    val drawingStrokes = remember { mutableStateListOf<Offset>() }

    LaunchedEffect(document) {
        val doc = document
        if (doc != null && !isInitialized) {
            annotations = PdfAnnotationSerializer.deserializeList(doc.content)
            titleText = doc.title
            isInitialized = true
        }
    }

    // Persist to Room
    fun saveAnnotations(updatedList: List<PdfAnnotation>) {
        annotations = updatedList
        val serializedContent = PdfAnnotationSerializer.serializeList(updatedList)
        viewModel.updateDocumentContent(docId, serializedContent)
    }

    // Tool actions
    fun addAnnotationAtCoordinate(xPercent: Float, yPercent: Float) {
        when (currentTool) {
            "HIGHLIGHT" -> {
                val newHighlight = PdfAnnotation(activePage, "HIGHLIGHT", "#FFEB3B", xPercent, yPercent, "Section Highlighted")
                saveAnnotations(annotations + newHighlight)
                currentTool = "NONE"
            }
            "STAMP" -> {
                val newStamp = PdfAnnotation(activePage, "STAMP", activeStampColorHex, xPercent, yPercent, activeStampText)
                saveAnnotations(annotations + newStamp)
                currentTool = "NONE"
            }
            "NOTE" -> {
                notePositionClicked = Offset(xPercent, yPercent)
                commentTextValue = ""
                showCommentDialog = true
            }
        }
    }

    fun clearAnnotationsOnCurrentPage() {
        val filtered = annotations.filter { it.pageIndex != activePage }
        saveAnnotations(filtered)
        drawingStrokes.clear()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = titleText.ifEmpty { "PDF Viewer" },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.goBack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    document?.let { doc ->
                        IconButton(onClick = {
                            val res = com.example.util.StorageExporter.exportDocument(context, doc.copy(content = PdfAnnotationSerializer.serializeList(annotations)))
                            if (res.isSuccess) {
                                android.widget.Toast.makeText(context, "Saved & Exported successfully!\n${res.filePath}", android.widget.Toast.LENGTH_LONG).show()
                                com.example.util.StorageExporter.triggerShare(context, res, doc.title)
                            } else {
                                android.widget.Toast.makeText(context, res.message, android.widget.Toast.LENGTH_LONG).show()
                            }
                        }) {
                            Icon(Icons.Filled.Download, contentDescription = "Export PDF Markup", tint = Color(0xFF1E3A8A))
                        }
                    }
                    IconButton(onClick = { clearAnnotationsOnCurrentPage() }) {
                        Icon(Icons.Filled.DeleteSweep, contentDescription = "Clear Page Markup", tint = Color(0xFFC53030))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            // PDF Page step and status
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(0.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEDF2F7))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { if (activePage > 0) activePage-- }, enabled = activePage > 0) {
                        Icon(Icons.Filled.KeyboardArrowLeft, contentDescription = "Prev page")
                    }
                    Text(
                        text = "Page ${activePage + 1} of $totalPages",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray
                    )
                    IconButton(onClick = { if (activePage < totalPages - 1) activePage++ }, enabled = activePage < totalPages - 1) {
                        Icon(Icons.Filled.KeyboardArrowRight, contentDescription = "Next page")
                    }
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFCBD5E1)) // Slate grey draft desk background
        ) {
            // 1. Sleek Annotation Markup Toolbar
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(6.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Freehand Highlight Stroke Select
                    IconButton(
                        onClick = { 
                            currentTool = if (currentTool == "HIGHLIGHT_STROKE") "NONE" else "HIGHLIGHT_STROKE"
                            activeBrushColorHex = "#50FBBF24" // default Yellow Marker
                        },
                        modifier = Modifier.background(
                            if (currentTool == "HIGHLIGHT_STROKE") Color(0xFFFEFCBF) else Color.Transparent, 
                            RoundedCornerShape(6.dp)
                        )
                    ) {
                        Icon(Icons.Filled.BorderColor, contentDescription = "Highlight Marker", tint = Color(0xFFD97706))
                    }

                    // Doodle Ink Pen Select
                    IconButton(
                        onClick = { 
                            currentTool = if (currentTool == "SIGN") "NONE" else "SIGN"
                            activeBrushColorHex = "#0F172A" // default Dark Gray Ink
                        },
                        modifier = Modifier.background(
                            if (currentTool == "SIGN") Color(0xFFE2E8F0) else Color.Transparent, 
                            RoundedCornerShape(6.dp)
                        )
                    ) {
                        Icon(Icons.Filled.Create, contentDescription = "Signature Doodle Path", tint = Color(0xFF334155))
                    }

                    // Comments note select
                    IconButton(
                        onClick = { currentTool = if (currentTool == "NOTE") "NONE" else "NOTE" },
                        modifier = Modifier.background(
                            if (currentTool == "NOTE") Color(0xFFEBF8FF) else Color.Transparent, 
                            RoundedCornerShape(6.dp)
                        )
                    ) {
                        Icon(Icons.Filled.Comment, contentDescription = "Add Sticky Note", tint = Color(0xFF2B6CB0))
                    }

                    // Stamp verification select
                    IconButton(
                        onClick = { currentTool = if (currentTool == "STAMP") "NONE" else "STAMP" },
                        modifier = Modifier.background(
                            if (currentTool == "STAMP") Color(0xFFFED7D7) else Color.Transparent, 
                            RoundedCornerShape(6.dp)
                        )
                    ) {
                        Icon(Icons.Filled.Verified, contentDescription = "Stamp Official Approved", tint = Color(0xFFC53030))
                    }
                }
            }

            if (currentTool == "SIGN") {
                // Color selector for solid pen
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Doodle Pen Ink: ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Spacer(modifier = Modifier.width(4.dp))
                        listOf(
                            Pair("#0F172A", "Dark Slate"),
                            Pair("#EF4444", "Crimson Red"),
                            Pair("#2563EB", "Royal Blue")
                        ).forEach { colorPair ->
                            val isSel = activeBrushColorHex == colorPair.first
                            AssistChip(
                                onClick = { activeBrushColorHex = colorPair.first },
                                label = { Text(colorPair.second, fontSize = 10.sp) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = if (isSel) Color(android.graphics.Color.parseColor(colorPair.first)).copy(alpha = 0.15f) else Color.White,
                                    labelColor = if (isSel) Color(android.graphics.Color.parseColor(colorPair.first)) else Color.Gray
                                ),
                                modifier = Modifier.padding(horizontal = 2.dp)
                            )
                        }
                    }
                }
            } else if (currentTool == "HIGHLIGHT_STROKE") {
                // Color selector for highlighter
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Marker Color: ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Spacer(modifier = Modifier.width(4.dp))
                        listOf(
                            Pair("#50FBBF24", "Yellow"),
                            Pair("#5010B981", "Green"),
                            Pair("#5006B6D4", "Cyan")
                        ).forEach { colorPair ->
                            val isSel = activeBrushColorHex == colorPair.first
                            AssistChip(
                                onClick = { activeBrushColorHex = colorPair.first },
                                label = { Text(colorPair.second, fontSize = 10.sp) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = if (isSel) Color(android.graphics.Color.parseColor(colorPair.first.replace("#50", "#FF"))).copy(alpha = 0.3f) else Color.White,
                                    labelColor = if (isSel) Color(android.graphics.Color.parseColor(colorPair.first.replace("#50", "#FF"))).copy(alpha = 1.0f) else Color.Gray
                                ),
                                modifier = Modifier.padding(horizontal = 2.dp)
                            )
                        }
                    }
                }
            } else if (currentTool == "STAMP") {
                // Stamp label selector
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Stamp Style: ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Spacer(modifier = Modifier.width(4.dp))
                        listOf(
                            Triple("APPROVED", "#10B981", "Approved"),
                            Triple("CONFIDENTIAL", "#F59E0B", "Confidential"),
                            Triple("REJECTED", "#EF4444", "Rejected")
                        ).forEach { mStamp ->
                            val isSel = activeStampText == mStamp.first
                            AssistChip(
                                onClick = {
                                    activeStampText = mStamp.first
                                    activeStampColorHex = mStamp.second
                                },
                                label = { Text(mStamp.first, fontSize = 10.sp) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = if (isSel) Color(android.graphics.Color.parseColor(mStamp.second)).copy(alpha = 0.15f) else Color.White,
                                    labelColor = if (isSel) Color(android.graphics.Color.parseColor(mStamp.second)) else Color.Gray
                                ),
                                modifier = Modifier.padding(horizontal = 2.dp)
                            )
                        }
                    }
                }
            }

            if (currentTool != "NONE") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                        .background(Color(0xFFFEF3C7), RoundedCornerShape(6.dp))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (currentTool) {
                            "HIGHLIGHT_STROKE" -> "Drag your finger over the text to highlight key sentences instantly."
                            "NOTE" -> "Tap anywhere on the document to pin a sticky annotation note."
                            "SIGN" -> "Draw freehand signatures or inline drawings directly over your document."
                            "STAMP" -> "Tap on the document page to drop your selected certification stamp."
                            else -> ""
                        },
                        fontSize = 11.sp,
                        color = Color(0xFF92400E),
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // 2. Realistic Print-Format Document Pages
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .aspectRatio(0.72f) // Standard letter aspect ratio
                        .pointerInput(currentTool, activePage) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    if (currentTool == "SIGN" || currentTool == "HIGHLIGHT_STROKE") {
                                        drawingStrokes.add(offset)
                                    }
                                },
                                onDrag = { change, dragAmount ->
                                    if (currentTool == "SIGN" || currentTool == "HIGHLIGHT_STROKE") {
                                        change.consume()
                                        drawingStrokes.add(change.position)
                                    }
                                },
                                onDragEnd = {
                                    if ((currentTool == "SIGN" || currentTool == "HIGHLIGHT_STROKE") && drawingStrokes.isNotEmpty()) {
                                        val first = drawingStrokes.first()
                                        val xP = first.x / size.width
                                        val yP = first.y / size.height
                                        val pathData = drawingStrokes.joinToString(";") { "${it.x},${it.y}" }
                                        val newSign = PdfAnnotation(activePage, currentTool, activeBrushColorHex, xP, yP, pathData)
                                        saveAnnotations(annotations + newSign)
                                        drawingStrokes.clear()
                                        currentTool = "NONE"
                                    }
                                }
                            )
                        }
                        .pointerInput(currentTool, activePage) {
                            // Touch listener for tapping coordinates
                            detectDragGestures(
                                onDragStart = { offset ->
                                    if (currentTool != "SIGN" && currentTool != "HIGHLIGHT_STROKE" && currentTool != "NONE") {
                                        val xPct = offset.x / size.width
                                        val yPct = offset.y / size.height
                                        addAnnotationAtCoordinate(xPct, yPct)
                                    }
                                },
                                onDrag = { _, _ -> },
                                onDragEnd = {}
                            )
                        },
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(2.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                        val w = maxWidth
                        val h = maxHeight

                        // Simulated PDF Print Layouts
                        if (activePage == 0) {
                            // Page 1 Layout: Cover Page with Corporate Identity
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp)
                            ) {
                                // Corporate border design
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp)
                                        .background(Color(0xFF1E3A8A))
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "OFFICIAL DOCUMENT",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF1E3A8A)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = titleText.replace(".pdf", "").uppercase(),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF0F172A),
                                    lineHeight = 24.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Standard Operating Handbook",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                
                                // Executive summary paragraphs
                                Text(
                                    text = "Section 1: General Core Guidelines",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.DarkGray
                                )
                                Divider(modifier = Modifier.padding(vertical = 6.dp))
                                Text(
                                    text = "This regulatory document details complete offline computational safety directives for OmniOffice local applications. All files, templates, and spreadsheets are sandboxed directly on the device partitions via Room Database nodes, satisfying high compliance metrics.",
                                    fontSize = 9.sp,
                                    color = Color.DarkGray,
                                    lineHeight = 12.sp
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Users of this off-grid software processing environment are strictly authorized to modify, expand, format, and present documents securely without leaks or cloud dependency cycles.\n\nAll cryptographic signatures added locally are saved as secure vector nodes, certified on-device.",
                                    fontSize = 9.sp,
                                    color = Color.Gray,
                                    lineHeight = 12.sp
                                )
                            }
                        } else {
                            // Page 2 Layout: Financial Graphs & Safety Standards
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp)
                            ) {
                                Text(
                                    text = "Section 2: Performance Metrics & Sizing",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E3A8A)
                                )
                                Divider(modifier = Modifier.padding(vertical = 6.dp))
                                
                                // Simulated visual chart
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(85.dp)
                                        .background(Color(0xFFF8FAFC), RoundedCornerShape(6.dp))
                                        .border(0.5.dp, Color.LightGray, RoundedCornerShape(6.dp))
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    val barHeights = listOf(25.dp, 45.dp, 70.dp, 55.dp, 80.dp)
                                    val barLabels = listOf("Jan", "Feb", "Mar", "Apr", "May")
                                    barHeights.forEachIndexed { idx, bh ->
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Box(
                                                modifier = Modifier
                                                    .width(18.dp)
                                                    .height(bh)
                                                    .background(Color(0xFF1E3A8A), RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(barLabels[idx], fontSize = 6.sp, color = Color.Gray)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Analytical Observations:",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.DarkGray
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "As shown in the graph, local processing optimization metrics rose 144% from standard layouts. Speedups from Kotlin Room Dao compiled queries surpassed cloud-bound delays by averages of fifteen times. This establishes complete offline operational superiority.",
                                    fontSize = 9.sp,
                                    color = Color.DarkGray,
                                    lineHeight = 12.sp
                                )
                            }
                        }

                        // Drawing Stroke Vector Layer
                        if (drawingStrokes.isNotEmpty()) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val path = Path()
                                path.moveTo(drawingStrokes.first().x, drawingStrokes.first().y)
                                for (i in 1 until drawingStrokes.size) {
                                    path.lineTo(drawingStrokes[i].x, drawingStrokes[i].y)
                                }
                                val colorInt = try {
                                    android.graphics.Color.parseColor(activeBrushColorHex)
                                } catch (e: Exception) {
                                    android.graphics.Color.BLACK
                                }
                                val strokeWidth = if (currentTool == "HIGHLIGHT_STROKE") 36f else 8f
                                drawPath(path, Color(colorInt), style = Stroke(width = strokeWidth))
                            }
                        }

                        // Render Active Annotations overlay
                        val pageAnns = annotations.filter { it.pageIndex == activePage }
                        pageAnns.forEach { ann ->
                            val xPos = (ann.xPercent * w.value).dp
                            val yPos = (ann.yPercent * h.value).dp

                            when (ann.type) {
                                "HIGHLIGHT" -> {
                                    Box(
                                        modifier = Modifier
                                            .offset(x = xPos - 20.dp, y = yPos - 12.dp)
                                            .width(120.dp)
                                            .height(25.dp)
                                            .background(Color(0xFFFBBF24).copy(alpha = 0.35f))
                                    )
                                }
                                "STAMP" -> {
                                    val stampCol = try {
                                        Color(android.graphics.Color.parseColor(ann.colorHex))
                                    } catch (e: Exception) {
                                        Color(0xFF38A169)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .offset(x = xPos - 40.dp, y = yPos - 15.dp)
                                            .shadow(2.dp, RoundedCornerShape(4.dp))
                                            .background(Color.White)
                                            .border(2.dp, stampCol, RoundedCornerShape(4.dp))
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = ann.extraText,
                                            color = stampCol,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = 1.sp
                                        )
                                    }
                                }
                                "NOTE" -> {
                                    Box(
                                        modifier = Modifier
                                            .offset(x = xPos - 10.dp, y = yPos - 10.dp)
                                            .size(20.dp)
                                            .shadow(2.dp, CircleShape)
                                            .background(Color(0xFF2B6CB0), CircleShape)
                                            .border(1.5.dp, Color.White, CircleShape)
                                            .clickable {
                                                activeCommentToView = ann
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Filled.Comment,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(10.dp)
                                        )
                                    }
                                }
                                "SIGN", "HIGHLIGHT_STROKE" -> {
                                    // Parse list of points
                                    val points = ann.extraText.split(";").mapNotNull { p ->
                                        val parts = p.split(",")
                                        if (parts.size == 2) {
                                            val px = parts[0].toFloatOrNull()
                                            val py = parts[1].toFloatOrNull()
                                            if (px != null && py != null) Offset(px, py) else null
                                        } else null
                                    }
                                    if (points.isNotEmpty()) {
                                        val strokeCol = try {
                                            Color(android.graphics.Color.parseColor(ann.colorHex))
                                        } catch (e: Exception) {
                                            if (ann.type == "HIGHLIGHT_STROKE") Color.Yellow.copy(alpha = 0.4f) else Color(0xFF2D3748)
                                        }
                                        val strokeWidth = if (ann.type == "HIGHLIGHT_STROKE") 36f else 8f
                                        Canvas(modifier = Modifier.fillMaxSize()) {
                                            val path = Path()
                                            path.moveTo(points.first().x, points.first().y)
                                            for (pt in points) {
                                                path.lineTo(pt.x, pt.y)
                                            }
                                            drawPath(path, strokeCol, style = Stroke(width = strokeWidth))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Add note comment dialog popup
        if (showCommentDialog) {
            AlertDialog(
                onDismissRequest = { showCommentDialog = false },
                title = { Text("Add Commentary Note", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text("Add a secure note directly tied to this document section:", fontSize = 11.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = commentTextValue,
                            onValueChange = { commentTextValue = it },
                            placeholder = { Text("E.g., Approved, compliance checks out...") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (commentTextValue.isNotBlank()) {
                                val newNote = PdfAnnotation(
                                    pageIndex = activePage,
                                    type = "NOTE",
                                    colorHex = "#2196F3",
                                    xPercent = notePositionClicked.x,
                                    yPercent = notePositionClicked.y,
                                    extraText = commentTextValue.trim()
                                )
                                saveAnnotations(annotations + newNote)
                                showCommentDialog = false
                                currentTool = "NONE"
                            }
                        }
                    ) {
                        Text("Pin Note")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCommentDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // View sticky comment dialog popup
        if (activeCommentToView != null) {
            AlertDialog(
                onDismissRequest = { activeCommentToView = null },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Comment, contentDescription = null, tint = Color(0xFF2B6CB0))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Secured Sticky Note", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                },
                text = {
                    Text(
                        text = activeCommentToView?.extraText ?: "",
                        fontSize = 13.sp,
                        color = Color.DarkGray
                    )
                },
                confirmButton = {
                    Button(onClick = { activeCommentToView = null }) {
                        Text("Close")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            val target = activeCommentToView
                            if (target != null) {
                                val list = annotations.toMutableList()
                                list.remove(target)
                                saveAnnotations(list)
                                activeCommentToView = null
                            }
                        }
                    ) {
                        Text("Delete Note", color = Color.Red)
                    }
                }
            )
        }
    }
}
