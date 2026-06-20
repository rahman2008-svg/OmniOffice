package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DocumentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordEditorScreen(
    viewModel: DocumentViewModel,
    docId: Int,
    modifier: Modifier = Modifier
) {
    val documentFlow = remember(docId) { viewModel.observeDocumentById(docId) }
    val document by documentFlow.collectAsState(initial = null)

    var textContent by remember { mutableStateOf("") }
    var titleText by remember { mutableStateOf("") }
    var isInitialized by remember { mutableStateOf(false) }

    // Text formatting mock styles - fully responsive and stateful in editor
    var isBold by remember { mutableStateOf(false) }
    var isItalic by remember { mutableStateOf(false) }
    var isUnderline by remember { mutableStateOf(false) }
    var fontSize by remember { mutableStateOf(14) }
    var textAlign by remember { mutableStateOf(TextAlign.Left) }
    var highlightColor by remember { mutableStateOf(Color.White) }
    
    var showRenameDialog by remember { mutableStateOf(false) }
    var renameValue by remember { mutableStateOf("") }

    LaunchedEffect(document) {
        val doc = document
        if (doc != null && !isInitialized) {
            textContent = doc.content
            titleText = doc.title
            isInitialized = true
        }
    }

    // Auto-save changes back to Room
    fun saveContent(newText: String) {
        textContent = newText
        viewModel.updateDocumentContent(docId, newText)
    }

    // Calculations
    val wordCount = remember(textContent) {
        if (textContent.isBlank()) 0 else textContent.split(Regex("\\s+")).filter { it.isNotBlank() }.size
    }
    val charCount = textContent.length

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
                            text = if (titleText.isEmpty()) "Loading..." else titleText,
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
                    IconButton(onClick = { saveContent(textContent) }) {
                        Icon(Icons.Filled.Save, contentDescription = "Save", tint = MaterialTheme.colorScheme.primary)
                    }
                    document?.let { doc ->
                        IconButton(onClick = {
                            val res = com.example.util.StorageExporter.exportDocument(context, doc.copy(content = textContent))
                            if (res.isSuccess) {
                                android.widget.Toast.makeText(context, "Exported successfully!\n${res.filePath}", android.widget.Toast.LENGTH_LONG).show()
                                com.example.util.StorageExporter.triggerShare(context, res, doc.title)
                            } else {
                                android.widget.Toast.makeText(context, res.message, android.widget.Toast.LENGTH_LONG).show()
                            }
                        }) {
                            Icon(Icons.Filled.Download, contentDescription = "Download/Export to storage", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            // Status bar
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(0.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEDF2F7))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Words: $wordCount   |   Chars: $charCount",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Gray
                    )
                    Text(
                        text = "Offline Mode Active",
                        fontSize = 10.sp,
                        color = Color(0xFF38A169),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFE2E8F0)) // Modern sleek gray desk background
        ) {
            // Document toolbar (B, I, U, Size, Alignment)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Bold Toggle
                    IconToggleButton(checked = isBold, onCheckedChange = { isBold = it }) {
                        Icon(
                            Icons.Filled.FormatBold,
                            contentDescription = "Bold",
                            tint = if (isBold) MaterialTheme.colorScheme.primary else Color.Gray,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Italic Toggle
                    IconToggleButton(checked = isItalic, onCheckedChange = { isItalic = it }) {
                        Icon(
                            Icons.Filled.FormatItalic,
                            contentDescription = "Italic",
                            tint = if (isItalic) MaterialTheme.colorScheme.primary else Color.Gray,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Underline Toggle
                    IconToggleButton(checked = isUnderline, onCheckedChange = { isUnderline = it }) {
                        Icon(
                            Icons.Filled.FormatUnderlined,
                            contentDescription = "Underline",
                            tint = if (isUnderline) MaterialTheme.colorScheme.primary else Color.Gray,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    VerticalDivider()

                    // Font Size Minus / Plus
                    IconButton(onClick = { if (fontSize > 10) fontSize -= 2 }) {
                        Icon(Icons.Filled.Remove, contentDescription = "Font smaller", modifier = Modifier.size(18.dp))
                    }
                    Text(
                        text = "$fontSize pt",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray
                    )
                    IconButton(onClick = { if (fontSize < 28) fontSize += 2 }) {
                        Icon(Icons.Filled.Add, contentDescription = "Font larger", modifier = Modifier.size(18.dp))
                    }

                    VerticalDivider()

                    // Align options
                    IconButton(onClick = { textAlign = TextAlign.Left }) {
                        Icon(
                            Icons.Filled.FormatAlignLeft,
                            contentDescription = "Align Left",
                            tint = if (textAlign == TextAlign.Left) MaterialTheme.colorScheme.primary else Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = { textAlign = TextAlign.Center }) {
                        Icon(
                            Icons.Filled.FormatAlignCenter,
                            contentDescription = "Align Center",
                            tint = if (textAlign == TextAlign.Center) MaterialTheme.colorScheme.primary else Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = { textAlign = TextAlign.Right }) {
                        Icon(
                            Icons.Filled.FormatAlignRight,
                            contentDescription = "Align Right",
                            tint = if (textAlign == TextAlign.Right) MaterialTheme.colorScheme.primary else Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    VerticalDivider()

                    // Highlights selector (visual helper)
                    Box {
                        var expanded by remember { mutableStateOf(false) }
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .shadow(1.dp, RoundedCornerShape(4.dp))
                                .background(highlightColor, RoundedCornerShape(4.dp))
                                .clickable { expanded = true }
                        )
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            val colors = listOf(
                                Pair(Color.White, "Plain"),
                                Pair(Color(0xFFFEFCBF), "Chalk Yellow"),
                                Pair(Color(0xFFEBF8FF), "Soft Ice Blue"),
                                Pair(Color(0xFFE6FFFA), "Mint Green"),
                                Pair(Color(0xFFFED7D7), "Pastel Red")
                            )
                            colors.forEach { c ->
                                DropdownMenuItem(
                                    text = { Text(c.second, fontSize = 12.sp) },
                                    onClick = {
                                        highlightColor = c.first
                                        expanded = false
                                    },
                                    leadingIcon = {
                                        Box(
                                            modifier = Modifier
                                                .size(16.dp)
                                                .background(c.first, RoundedCornerShape(3.dp))
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Document body - white elegant sheet design resting on table
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .shadow(4.dp, RoundedCornerShape(4.dp)),
                    colors = CardDefaults.cardColors(containerColor = highlightColor),
                    shape = RoundedCornerShape(2.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Word Processor Text Field
                        TextField(
                            value = textContent,
                            onValueChange = { saveContent(it) },
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                                .verticalScroll(rememberScrollState()),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            placeholder = { Text("Start typing your document content here...", fontSize = 14.sp) },
                            textStyle = LocalTextStyle.current.copy(
                                fontSize = fontSize.sp,
                                fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
                                fontStyle = if (isItalic) FontStyle.Italic else FontStyle.Normal,
                                textDecoration = if (isUnderline) TextDecoration.Underline else TextDecoration.None,
                                textAlign = textAlign,
                                color = Color(0xFF1A202C)
                            )
                        )
                    }
                }
            }
        }

        // Rename dialog popup
        if (showRenameDialog) {
            AlertDialog(
                onDismissRequest = { showRenameDialog = false },
                title = { Text("Rename File", fontWeight = FontWeight.Bold) },
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

@Composable
fun VerticalDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(24.dp)
            .background(Color.LightGray)
    )
}
