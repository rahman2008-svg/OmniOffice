package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DocumentViewModel
import com.example.data.Slide
import com.example.data.SlideSerializer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlideEditorScreen(
    viewModel: DocumentViewModel,
    docId: Int,
    modifier: Modifier = Modifier
) {
    val documentFlow = remember(docId) { viewModel.observeDocumentById(docId) }
    val document by documentFlow.collectAsState(initial = null)

    var slides by remember { mutableStateOf<List<Slide>>(emptyList()) }
    var titleText by remember { mutableStateOf("") }
    var isInitialized by remember { mutableStateOf(false) }

    var selectedSlideIndex by remember { mutableStateOf(0) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var renameValue by remember { mutableStateOf("") }
    
    // Presentation player state
    var isPresentingMode by remember { mutableStateOf(false) }
    var presentSlideIndex by remember { mutableStateOf(0) }
    var isLaserActive by remember { mutableStateOf(false) }
    var laserCoordinates by remember { mutableStateOf(Offset.Unspecified) }

    LaunchedEffect(document) {
        val doc = document
        if (doc != null && !isInitialized) {
            slides = SlideSerializer.deserializeList(doc.content)
            titleText = doc.title
            isInitialized = true
        }
    }

    // Save and update Room
    fun persistSlides(updatedList: List<Slide>) {
        slides = updatedList
        val serializedContent = SlideSerializer.serializeList(updatedList)
        viewModel.updateDocumentContent(docId, serializedContent)
    }

    // Slide manipulations
    fun updateActiveSlide(updatedSlide: Slide) {
        val currentList = slides.toMutableList()
        if (selectedSlideIndex in currentList.indices) {
            currentList[selectedSlideIndex] = updatedSlide
            persistSlides(currentList)
        }
    }

    fun addNewSlide() {
        val currentList = slides.toMutableList()
        currentList.add(Slide("New Slide Title", "Double tap to change", "• Point A\n• Point B", "Ocean Mint", "CONTENT"))
        persistSlides(currentList)
        selectedSlideIndex = currentList.size - 1
    }

    fun deleteSlide(index: Int) {
        val currentList = slides.toMutableList()
        if (currentList.size > 1 && index in currentList.indices) {
            currentList.removeAt(index)
            persistSlides(currentList)
            selectedSlideIndex = maxOf(0, index - 1)
        }
    }

    val activeSlide = slides.getOrNull(selectedSlideIndex) ?: Slide()

    if (isPresentingMode) {
        // Full screen presenter overlay
        val density = LocalDensity.current.density
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            val slidesCount = slides.size
            val pSlide = slides.getOrNull(presentSlideIndex) ?: Slide()
            val gradientBrush = getSlideGradient(pSlide.bgTheme)

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(gradientBrush)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                if (isLaserActive) laserCoordinates = offset
                            },
                            onDrag = { change, dragAmount ->
                                if (isLaserActive) {
                                    change.consume()
                                    laserCoordinates = change.position
                                }
                            },
                            onDragEnd = {
                                laserCoordinates = Offset.Unspecified
                            }
                        )
                    }
                    .padding(40.dp)
            ) {
                // Render presentation layouts cleanly
                RenderSlideLayout(slide = pSlide, isPresenter = true)

                // Laser glow particle
                if (isLaserActive && laserCoordinates != Offset.Unspecified) {
                    Box(
                        modifier = Modifier
                            .offset(
                                x = (laserCoordinates.x / density).dp - 10.dp,
                                y = (laserCoordinates.y / density).dp - 10.dp
                            )
                            .size(20.dp)
                            .background(Color.Red.copy(alpha = 0.8f), CircleShape)
                            .border(2.dp, Color.White, CircleShape)
                    )
                }
            }

            // Controls floating bottom bar
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(24.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.DarkGray.copy(alpha = 0.85f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { if (presentSlideIndex > 0) presentSlideIndex-- }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Prev", tint = Color.White)
                    }
                    Text(
                        text = "Slide ${presentSlideIndex + 1} of $slidesCount",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { if (presentSlideIndex < slidesCount - 1) presentSlideIndex++ }) {
                        Icon(Icons.Filled.ArrowForward, contentDescription = "Next", tint = Color.White)
                    }

                    VerticalDividerInPresentation()

                    // Laser pointer toggle button
                    IconButton(
                        onClick = { isLaserActive = !isLaserActive },
                        modifier = Modifier.background(if (isLaserActive) Color.Red else Color.Transparent, CircleShape)
                    ) {
                        Icon(
                            Icons.Filled.FilterTiltShift,
                            contentDescription = "Laser pointer",
                            tint = if (isLaserActive) Color.White else Color.Tomato()
                        )
                    }

                    VerticalDividerInPresentation()

                    IconButton(onClick = { isPresentingMode = false }) {
                        Icon(Icons.Filled.Close, contentDescription = "Exit Presenting", tint = Color.White)
                    }
                }
            }
        }
    } else {
        // Normal slide editor screen
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            modifier = Modifier.clickable {
                                renameValue = titleText
                                showRenameDialog = true
                            },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (titleText.isEmpty()) "Loading presentation..." else titleText,
                                maxLines = 1,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                Icons.Filled.Edit,
                                contentDescription = "Rename",
                                modifier = Modifier.size(14.dp),
                                tint = Color.Gray
                            )
                        }
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
                                val res = com.example.util.StorageExporter.exportDocument(context, doc.copy(content = SlideSerializer.serializeList(slides)))
                                if (res.isSuccess) {
                                    android.widget.Toast.makeText(context, "Exported successfully!\n${res.filePath}", android.widget.Toast.LENGTH_LONG).show()
                                    com.example.util.StorageExporter.triggerShare(context, res, doc.title)
                                } else {
                                    android.widget.Toast.makeText(context, res.message, android.widget.Toast.LENGTH_LONG).show()
                                }
                            }) {
                                Icon(Icons.Filled.Download, contentDescription = "Export Outline", tint = Color(0xFFD97706))
                            }
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Button(
                            onClick = {
                                if (slides.isNotEmpty()) {
                                    presentSlideIndex = selectedSlideIndex
                                    isLaserActive = false
                                    isPresentingMode = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Present", fontSize = 12.sp)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
            },
            modifier = modifier
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(Color(0xFFF7F9FC))
            ) {
                // 1. Interactive Slides Row Carousel (Left list replica for clean vertical/mobile usage)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Slide Sheets (${slides.size})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                            IconButton(onClick = { addNewSlide() }) {
                                Icon(Icons.Filled.AddCircle, contentDescription = "Add Slide", tint = Color(0xFFD97706))
                            }
                        }
                        
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            itemsIndexed(slides) { idx, sl ->
                                val isSelected = selectedSlideIndex == idx
                                Box(
                                    modifier = Modifier
                                        .size(width = 90.dp, height = 55.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(getSlideGradient(sl.bgTheme))
                                        .border(
                                            if (isSelected) 2.5.dp else 1.dp,
                                            if (isSelected) Color(0xFFD97706) else Color.LightGray,
                                            RoundedCornerShape(6.dp)
                                        )
                                        .clickable { selectedSlideIndex = idx }
                                        .padding(4.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxSize()) {
                                        Text(
                                            text = sl.title,
                                            fontSize = 7.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (sl.bgTheme == "Sleek Dark") Color.White else Color.Black,
                                            maxLines = 1
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Page ${idx + 1}",
                                                fontSize = 6.sp,
                                                color = if (sl.bgTheme == "Sleek Dark") Color.LightGray else Color.DarkGray
                                            )
                                            if (slides.size > 1) {
                                                Icon(
                                                    Icons.Filled.Delete,
                                                    contentDescription = null,
                                                    tint = Color.Red.copy(alpha = 0.5f),
                                                    modifier = Modifier
                                                        .size(10.dp)
                                                        .clickable { deleteSlide(idx) }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 2. Focused Slide Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(12.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(getSlideGradient(activeSlide.bgTheme))
                                .padding(24.dp)
                        ) {
                            RenderSlideLayout(slide = activeSlide, isPresenter = false)
                        }
                    }
                }

                // 3. Slide Metadata and Context Layout Editor Toolbar
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Edit Slide Details", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = activeSlide.title,
                                onValueChange = { updateActiveSlide(activeSlide.copy(title = it)) },
                                label = { Text("Title") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                textStyle = LocalTextStyle.current.copy(fontSize = 11.sp)
                            )
                            OutlinedTextField(
                                value = activeSlide.subtitle,
                                onValueChange = { updateActiveSlide(activeSlide.copy(subtitle = it)) },
                                label = { Text("Subtitle") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                textStyle = LocalTextStyle.current.copy(fontSize = 11.sp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        OutlinedTextField(
                            value = activeSlide.body,
                            onValueChange = { updateActiveSlide(activeSlide.copy(body = it)) },
                            label = { Text("Body / Bullet Points") },
                            modifier = Modifier.fillMaxWidth().height(80.dp),
                            textStyle = LocalTextStyle.current.copy(fontSize = 11.sp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Controls: Theme gradients & Layout selections
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Layout select
                            Column {
                                Text("Slide Grid Style", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    val layouts = listOf("TITLE", "CONTENT", "SPLIT")
                                    layouts.forEach { ly ->
                                        FilterChip(
                                            selected = activeSlide.layout == ly,
                                            onClick = { updateActiveSlide(activeSlide.copy(layout = ly)) },
                                            label = { Text(ly, fontSize = 9.sp) }
                                        )
                                    }
                                }
                            }

                            // Backdrop select
                            Column {
                                Text("Canvas Theme", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    val themePresets = listOf("Ocean Mint", "Sleek Dark", "Lavender Rose", "Sunset Glow")
                                    val bgColors = listOf(Color(0xFFD4FC79), Color(0xFF1A202C), Color(0xFFECC4FF), Color(0xFFFFB347))
                                    themePresets.forEachIndexed { idx, t ->
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(bgColors[idx])
                                                .border(
                                                    if (activeSlide.bgTheme == t) 2.dp else 0.5.dp,
                                                    if (activeSlide.bgTheme == t) Color(0xFFD97706) else Color.LightGray,
                                                    CircleShape
                                                )
                                                .clickable { updateActiveSlide(activeSlide.copy(bgTheme = t)) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Rename dialog popup
            if (showRenameDialog) {
                AlertDialog(
                    onDismissRequest = { showRenameDialog = false },
                    title = { Text("Rename Presentation", fontWeight = FontWeight.Bold) },
                    text = {
                        OutlinedTextField(
                            value = renameValue,
                            onValueChange = { renameValue = it },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (renameValue.isNotBlank()) {
                                    titleText = renameValue
                                    viewModel.updateDocumentTitle(docId, renameValue)
                                    showRenameDialog = false
                                }
                            }
                        ) {
                            Text("Rename")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showRenameDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}

// Color and Theme Utilities
fun getSlideGradient(theme: String): Brush {
    return when (theme) {
        "Ocean Mint" -> Brush.linearGradient(colors = listOf(Color(0xFFE0EAFC), Color(0xFFCFDEF3)))
        "Sleek Dark" -> Brush.linearGradient(colors = listOf(Color(0xFF1E293B), Color(0xFF0F172A)))
        "Lavender Rose" -> Brush.linearGradient(colors = listOf(Color(0xFFF7F0F5), Color(0xFFDEC5E3)))
        "Sunset Glow" -> Brush.linearGradient(colors = listOf(Color(0xFFFFF1EB), Color(0xFFACE0F9)))
        else -> Brush.linearGradient(colors = listOf(Color.White, Color.LightGray))
    }
}

fun Color.Companion.Tomato(): Color = Color(0xFFFF6347)

@Composable
fun VerticalDividerInPresentation() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(20.dp)
            .background(Color.Gray)
    )
}

@Composable
fun RenderSlideLayout(slide: Slide, isPresenter: Boolean) {
    val titleSize = if (isPresenter) 36.sp else 22.sp
    val subSize = if (isPresenter) 18.sp else 12.sp
    val bodySize = if (isPresenter) 18.sp else 11.sp
    
    val textColor = if (slide.bgTheme == "Sleek Dark") Color.White else Color(0xFF2D3748)
    val captionColor = if (slide.bgTheme == "Sleek Dark") Color.LightGray else Color.Gray

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = if (slide.layout == "TITLE") Arrangement.Center else Arrangement.Top
    ) {
        if (slide.layout == "TITLE") {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = slide.title,
                    fontSize = titleSize,
                    fontWeight = FontWeight.ExtraBold,
                    color = textColor,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = slide.subtitle,
                    fontSize = subSize,
                    color = captionColor,
                    textAlign = TextAlign.Center
                )
            }
        } else if (slide.layout == "SPLIT") {
            Text(
                text = slide.title,
                fontSize = titleSize,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            Text(
                text = slide.subtitle,
                fontSize = subSize,
                color = captionColor
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                // Split 1
                Column(modifier = Modifier.weight(1f)) {
                    Text("Aims & Targets", fontSize = subSize, fontWeight = FontWeight.Bold, color = textColor)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = slide.body,
                        fontSize = bodySize,
                        color = textColor.copy(alpha = 0.85f),
                        lineHeight = 16.sp
                    )
                }
                // Split 2 (Render a clean geometric card vector representing local stats)
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(110.dp),
                    colors = CardDefaults.cardColors(containerColor = textColor.copy(alpha = 0.05f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("OFFLINE PERFORMANCE", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = captionColor)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("100% SECURE", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = textColor)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("< 20ms execution state", fontSize = 8.sp, color = captionColor)
                    }
                }
            }
        } else {
            // "CONTENT" standard layout
            Text(
                text = slide.title,
                fontSize = titleSize,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            Text(
                text = slide.subtitle,
                fontSize = subSize,
                color = captionColor
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = slide.body,
                fontSize = bodySize,
                color = textColor.copy(alpha = 0.9f),
                lineHeight = 20.sp,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
