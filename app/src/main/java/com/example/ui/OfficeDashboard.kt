package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.DocumentEntity
import com.example.data.DocumentViewModel
import com.example.data.Screen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfficeDashboard(
    viewModel: DocumentViewModel,
    modifier: Modifier = Modifier
) {
    val documents by viewModel.filteredDocuments.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val focusManager = LocalFocusManager.current
    var showCreateDialog by remember { mutableStateOf(false) }
    var docTypeToCreate by remember { mutableStateOf("WORD") }
    var newDocTitle by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(
                    onClick = {
                        docTypeToCreate = "WORD"
                        newDocTitle = "New Document"
                        showCreateDialog = true
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = "New file",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 4.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Filled.Description, contentDescription = "Files List") },
                    label = { Text("Documents", fontWeight = FontWeight.Bold) }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Filled.Person, contentDescription = "Author") },
                    label = { Text("About Dev", fontWeight = FontWeight.Bold) }
                )
            }
        },
        modifier = modifier
    ) { innerPadding ->
        if (selectedTab == 1) {
            AboutScreen(modifier = Modifier.padding(innerPadding))
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(Color(0xFFF3F6FA)) // Soft slate/blue-gray theme background
            ) {
            // 1. Material 3 Search Bar Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "OmniOffice",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF0F172A), // Slate 900
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            text = "Professional Document Suite",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF64748B) // Slate 500
                        )
                    }

                    // JD user profile badge
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFF2563EB), CircleShape), // Blue-600
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "JD",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Modern Search Pill Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)) // Border slate-100/200
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = "Search icon",
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        TextField(
                            value = searchQuery,
                            onValueChange = { viewModel.searchQuery.value = it },
                            placeholder = { 
                                Text(
                                    "Search docs, sheets, slides, PDFs...", 
                                    color = Color(0xFF94A3B8),
                                    fontSize = 13.sp
                                ) 
                            },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                                Icon(
                                    imageVector = Icons.Filled.Clear, 
                                    contentDescription = "Clear search",
                                    tint = Color(0xFF64748B),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 2. Bento Style Quick Access Grid
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val formats = listOf(
                        Triple("WORD", "Docs", Color(0xFFEFF6FF) to Color(0xFF2563EB)),     // Blue
                        Triple("SHEET", "Sheets", Color(0xFFECFDF5) to Color(0xFF059669)),   // Green
                        Triple("SLIDE", "Slides", Color(0xFFFFF7ED) to Color(0xFFEA580C)),   // Orange
                        Triple("PDF", "PDF", Color(0xFFFEF2F2) to Color(0xFFDC2626))         // Red
                    )

                    formats.forEach { (typeKey, name, colors) ->
                        val (bgColor, textColor) = colors
                        val isSelected = selectedCategory == typeKey

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    // Toggle filter mode
                                    if (selectedCategory == typeKey) {
                                        viewModel.selectedCategory.value = "ALL"
                                    } else {
                                        viewModel.selectedCategory.value = typeKey
                                    }
                                }
                                .padding(vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(
                                        color = if (isSelected) textColor else bgColor, 
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) Color.White else Color.Transparent,
                                        shape = RoundedCornerShape(16.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (typeKey == "PDF") "PDF" else name.first().toString(),
                                    color = if (isSelected) Color.White else textColor,
                                    fontWeight = FontWeight.Black,
                                    fontSize = if (typeKey == "PDF") 11.sp else 16.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = name,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) textColor else Color(0xFF475569) // Slate 600
                            )
                        }
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                // 3. Custom Work Desk Hero Banner
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .clip(RoundedCornerShape(16.dp))
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_office_banner),
                            contentDescription = "Workstation Desk",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            Color.Black.copy(alpha = 0.75f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Welcome to OmniOffice",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Fully offline office processing suite.",
                                color = Color.LightGray,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // 3. Document Templates Section
                item {
                    Text(
                        text = "Start from a Template",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
                        color = Color(0xFF2D3748)
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            TemplateCard(
                                title = "Blank Word",
                                subtitle = "Clean doc",
                                color = Color(0xFF1E3A8A),
                                onClick = {
                                    viewModel.createNewDocument("Blank Word.docx", "WORD")
                                }
                            )
                        }
                        item {
                            TemplateCard(
                                title = "Professional Resume",
                                subtitle = "Word Template",
                                color = Color(0xFF1D4ED8),
                                onClick = {
                                    viewModel.createNewDocument("Professional Resume.docx", "WORD", "Resume")
                                }
                            )
                        }
                        item {
                            TemplateCard(
                                title = "Blank Sheet",
                                subtitle = "Clean grid",
                                color = Color(0xFF047857),
                                onClick = {
                                    viewModel.createNewDocument("Blank Sheet.xlsx", "SHEET")
                                }
                            )
                        }
                        item {
                            TemplateCard(
                                title = "Monthly Budget",
                                subtitle = "Spreadsheet Template",
                                color = Color(0xFF065F46),
                                onClick = {
                                    viewModel.createNewDocument("Monthly Budget.xlsx", "SHEET", "Budget")
                                }
                            )
                        }
                        item {
                            TemplateCard(
                                title = "Blank Deck",
                                subtitle = "Clean slide",
                                color = Color(0xFFD97706),
                                onClick = {
                                    viewModel.createNewDocument("Blank Slides.pptx", "SLIDE")
                                }
                            )
                        }
                        item {
                            TemplateCard(
                                title = "Investor Pitch",
                                subtitle = "Presentation Template",
                                color = Color(0xFFB45309),
                                onClick = {
                                    viewModel.createNewDocument("Investor Pitch.pptx", "SLIDE", "Pitch")
                                }
                            )
                        }
                    }
                }

                // 4. Quick Category Switcher
                item {
                    Text(
                        text = "File Formats",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 6.dp),
                        color = Color(0xFF2D3748)
                    )
                    CategoryRow(
                        selected = selectedCategory,
                        onSelected = { viewModel.selectedCategory.value = it }
                    )
                }

                // 5. Existing Local Files
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "My Documents (${documents.size})",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1D2D50)
                        )
                    }
                }

                if (documents.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Inbox,
                                contentDescription = null,
                                modifier = Modifier.size(50.dp),
                                tint = Color.LightGray
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("No files match filters.", color = Color.Gray, fontSize = 13.sp)
                            Text("Tap the floating button to create one!", color = Color.Gray, fontSize = 11.sp)
                        }
                    }
                } else {
                    items(documents, key = { it.id }) { doc ->
                        DocumentItem(
                            document = doc,
                            onToggleStar = { viewModel.toggleStarred(doc.id) },
                            onDelete = { viewModel.deleteDocument(doc.id) },
                            onClick = {
                                when (doc.type) {
                                    "WORD" -> viewModel.navigateTo(Screen.WordEditor(doc.id))
                                    "SHEET" -> viewModel.navigateTo(Screen.SheetEditor(doc.id))
                                    "SLIDE" -> viewModel.navigateTo(Screen.SlideEditor(doc.id))
                                    "PDF" -> viewModel.navigateTo(Screen.PdfReader(doc.id))
                                }
                            }
                        )
                    }
                }

                // Beautiful Storage & Sandbox Status Card matching the design theme HTML
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2563EB)), // Vibrant M3 Blue
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column {
                                    Text(
                                        text = "LOCAL SANDBOX ENGINE",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White.copy(alpha = 0.8f),
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "100% Offline Integrity",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Storage,
                                        contentDescription = "Shield Offline Storage Logo",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Sleek horizontal health progression meter
                            LinearProgressIndicator(
                                progress = { 1.0f },
                                color = Color.White,
                                trackColor = Color.White.copy(alpha = 0.25f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(CircleShape)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "0.2 MB used of 512 MB locally allocated",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                                Text(
                                    text = "Compliant",
                                    color = Color(0xFF34D399), // Emerald 400
                                    fontWeight = FontWeight.Black,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }

        // Create Dialog Popup
        if (showCreateDialog) {
            AlertDialog(
                onDismissRequest = { showCreateDialog = false },
                title = { Text("Create New Document", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = newDocTitle,
                            onValueChange = { newDocTitle = it },
                            label = { Text("File Name") },
                            placeholder = { Text("Enter document name...") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Format:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val types = listOf(
                                Pair("WORD", "Word"),
                                Pair("SHEET", "Sheet"),
                                Pair("SLIDE", "Slides")
                            )
                            types.forEach { t ->
                                FilterChip(
                                    selected = docTypeToCreate == t.first,
                                    onClick = { 
                                        docTypeToCreate = t.first
                                        newDocTitle = "New " + t.second + when (t.first) {
                                            "WORD" -> ".docx"
                                            "SHEET" -> ".xlsx"
                                            else -> ".pptx"
                                        }
                                    },
                                    label = { Text(t.second) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newDocTitle.isNotBlank()) {
                                viewModel.createNewDocument(newDocTitle.trim(), docTypeToCreate)
                                showCreateDialog = false
                            }
                        }
                    ) {
                        Text("Create & Open")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun TemplateCard(
    title: String,
    subtitle: String,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(130.dp)
            .height(95.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .background(Color.White.copy(alpha = 0.25f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
            Column {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = subtitle,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 9.sp,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun CategoryRow(selected: String, onSelected: (String) -> Unit) {
    val categories = listOf(
        Pair("ALL", "All Files"),
        Pair("WORD", "Word .docx"),
        Pair("SHEET", "Excel .xlsx"),
        Pair("SLIDE", "Slides .pptx"),
        Pair("PDF", "PDF Reader")
    )

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        items(categories) { cat ->
            val isSelected = selected == cat.first
            val color = when (cat.first) {
                "WORD" -> Color(0xFF1E3A8A)
                "SHEET" -> Color(0xFF047857)
                "SLIDE" -> Color(0xFFD97706)
                "PDF" -> Color(0xFFC53030)
                else -> MaterialTheme.colorScheme.primary
            }

            FilterChip(
                selected = isSelected,
                onClick = { onSelected(cat.first) },
                label = { Text(cat.second, fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = color.copy(alpha = 0.15f),
                    selectedLabelColor = color,
                    selectedLeadingIconColor = color
                )
            )
        }
    }
}

@Composable
fun DocumentItem(
    document: DocumentEntity,
    onToggleStar: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    val iconColor = when (document.type) {
        "WORD" -> Color(0xFF1E3A8A)
        "SHEET" -> Color(0xFF047857)
        "SLIDE" -> Color(0xFFD97706)
        "PDF" -> Color(0xFFD32F2F)
        else -> Color.Gray
    }

    val iconVector = when (document.type) {
        "WORD" -> Icons.Filled.Description
        "SHEET" -> Icons.Filled.GridOn
        "SLIDE" -> Icons.Filled.Slideshow
        "PDF" -> Icons.Filled.PictureAsPdf
        else -> Icons.Filled.InsertDriveFile
    }

    val dateStr = try {
        val df = SimpleDateFormat("MMM dd, h:mm a", Locale.US)
        df.format(Date(document.lastModified))
    } catch (e: Exception) {
        ""
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(iconColor.copy(alpha = 0.12f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = document.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2D3748),
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = document.type + " • " + document.fileSize,
                        fontSize = 10.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = dateStr, fontSize = 10.sp, color = Color.LightGray)
                }
            }
            
            // Star Icon Action
            IconButton(onClick = onToggleStar) {
                Icon(
                    imageVector = if (document.isStarred) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    contentDescription = "Star",
                    tint = if (document.isStarred) Color(0xFFFBBF24) else Color.LightGray
                )
            }

            // Simple Delete Icon Action
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Delete",
                    tint = Color(0xFFE53E3E).copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
