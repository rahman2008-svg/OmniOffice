package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DocumentViewModel
import com.example.data.SheetCell
import com.example.data.SheetSerializer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SheetEditorScreen(
    viewModel: DocumentViewModel,
    docId: Int,
    modifier: Modifier = Modifier
) {
    val documentFlow = remember(docId) { viewModel.observeDocumentById(docId) }
    val document by documentFlow.collectAsState(initial = null)

    var cellMap by remember { mutableStateOf<Map<String, SheetCell>>(emptyMap()) }
    var titleText by remember { mutableStateOf("") }
    var isInitialized by remember { mutableStateOf(false) }

    // Grid coordinates
    val columns = listOf("A", "B", "C", "D", "E")
    val rows = (1..12).toList()

    // Selection state
    var selectedCellKey by remember { mutableStateOf("A1") }
    var formulaValueInput by remember { mutableStateOf("") }
    
    var showRenameDialog by remember { mutableStateOf(false) }
    var renameValue by remember { mutableStateOf("") }

    LaunchedEffect(document) {
        val doc = document
        if (doc != null && !isInitialized) {
            cellMap = SheetSerializer.deserializeMap(doc.content)
            titleText = doc.title
            isInitialized = true
        }
    }

    // Load active cell text into formula bar on click
    LaunchedEffect(selectedCellKey, cellMap) {
        val cell = cellMap[selectedCellKey] ?: SheetCell()
        formulaValueInput = cell.value
    }

    // Save and update Room
    fun persistCells(updatedMap: Map<String, SheetCell>) {
        cellMap = updatedMap
        val serializedContent = SheetSerializer.serializeMap(updatedMap)
        viewModel.updateDocumentContent(docId, serializedContent)
    }

    fun updateSelectedCellValue(newVal: String) {
        formulaValueInput = newVal
        val currentCell = cellMap[selectedCellKey] ?: SheetCell()
        val updated = currentCell.copy(value = newVal)
        val newMap = cellMap.toMutableMap()
        newMap[selectedCellKey] = updated
        persistCells(newMap)
    }

    fun toggleSelectedCellBold() {
        val currentCell = cellMap[selectedCellKey] ?: SheetCell()
        val updated = currentCell.copy(isBold = !currentCell.isBold)
        val newMap = cellMap.toMutableMap()
        newMap[selectedCellKey] = updated
        persistCells(newMap)
    }

    fun updateSelectedCellBg(hexColor: String) {
        val currentCell = cellMap[selectedCellKey] ?: SheetCell()
        val updated = currentCell.copy(bgHex = hexColor)
        val newMap = cellMap.toMutableMap()
        newMap[selectedCellKey] = updated
        persistCells(newMap)
    }

    fun updateSelectedCellTextColor(hexColor: String) {
        val currentCell = cellMap[selectedCellKey] ?: SheetCell()
        val updated = currentCell.copy(textColorHex = hexColor)
        val newMap = cellMap.toMutableMap()
        newMap[selectedCellKey] = updated
        persistCells(newMap)
    }

    fun parseColor(hex: String, default: Color): Color {
        return try {
            Color(android.graphics.Color.parseColor(hex))
        } catch (e: Exception) {
            default
        }
    }

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
                            text = if (titleText.isEmpty()) "Loading Sheet..." else titleText,
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
                            val res = com.example.util.StorageExporter.exportDocument(context, doc.copy(content = SheetSerializer.serializeMap(cellMap)))
                            if (res.isSuccess) {
                                android.widget.Toast.makeText(context, "Exported successfully!\n${res.filePath}", android.widget.Toast.LENGTH_LONG).show()
                                com.example.util.StorageExporter.triggerShare(context, res, doc.title)
                            } else {
                                android.widget.Toast.makeText(context, res.message, android.widget.Toast.LENGTH_LONG).show()
                            }
                        }) {
                            Icon(Icons.Filled.Download, contentDescription = "Download CSV", tint = Color(0xFF047857))
                        }
                    }
                    Text(
                        text = "Sheet Editor",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF047857),
                        modifier = Modifier.padding(end = 16.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            // Spreadsheet bottom context & style controls
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(0.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEDF2F7))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Cell style bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Bold Toggle icon
                        val selectedCell = cellMap[selectedCellKey] ?: SheetCell()
                        IconButton(
                            onClick = { toggleSelectedCellBold() },
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    if (selectedCell.isBold) Color(0xFFE2E8F0) else Color.Transparent,
                                    RoundedCornerShape(6.dp)
                                )
                        ) {
                            Icon(Icons.Filled.FormatBold, contentDescription = "Bold Toggle", modifier = Modifier.size(20.dp))
                        }

                        // Presets of Cell backgrounds
                        Text("Fill:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.Gray)
                        val bgPresets = listOf(
                            Pair("#FFFFFF", Color.White),
                            Pair("#E0EAFC", Color(0xFFE0EAFC)),
                            Pair("#E6FFFA", Color(0xFFE6FFFA)),
                            Pair("#FEEBC8", Color(0xFFFEEBC8)),
                            Pair("#FED7D7", Color(0xFFFED7D7))
                        )
                        bgPresets.forEach { p ->
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .border(
                                        BorderStroke(
                                            if (selectedCell.bgHex.uppercase() == p.first.uppercase()) 2.dp else 1.dp,
                                            if (selectedCell.bgHex.uppercase() == p.first.uppercase()) Color(0xFF047857) else Color.LightGray
                                        ),
                                        CircleShape
                                    )
                                    .background(p.second, CircleShape)
                                    .clickable { updateSelectedCellBg(p.first) }
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        // Clear cell action
                        TextButton(
                            onClick = { updateSelectedCellValue("") },
                            colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFC53030))
                        ) {
                            Icon(Icons.Filled.ClearAll, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Clear", fontSize = 11.sp)
                        }
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
                .background(Color(0xFFF7F9FC))
        ) {
            // 1. Sleek Address and Formula Input Bar
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Cell Address Marker
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFE6FFFA), RoundedCornerShape(6.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = selectedCellKey,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF047857),
                            letterSpacing = 0.5.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "fx",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.LightGray
                    )
                    Spacer(modifier = Modifier.width(8.dp))

                    // TextField to enter numeric values or cell formulas
                    OutlinedTextField(
                        value = formulaValueInput,
                        onValueChange = { updateSelectedCellValue(it) },
                        placeholder = { Text("Enter a value, number or formula like =SUM(B3:B6)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF047857),
                            unfocusedBorderColor = Color(0xFFE2E8F0)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        textStyle = LocalTextStyle.current.copy(fontSize = 12.sp)
                    )
                }
            }

            // 2. Spreadsheet Grid Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .horizontalScroll(rememberScrollState())
                    .verticalScroll(rememberScrollState())
                    .padding(8.dp)
            ) {
                Column {
                    // Columns headers row A, B, C, D...
                    Row {
                        // Empty corner cell
                        Box(
                            modifier = Modifier
                                .size(width = 36.dp, height = 26.dp)
                                .background(Color(0xFFEDF2F7))
                                .border(1.dp, Color(0xFFCBD5E0))
                        )
                        columns.forEach { col ->
                            Box(
                                modifier = Modifier
                                    .size(width = 95.dp, height = 26.dp)
                                    .background(Color(0xFFEDF2F7))
                                    .border(1.dp, Color(0xFFCBD5E0)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = col,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.DarkGray
                                )
                            }
                        }
                    }

                    // Grid cell rows
                    rows.forEach { row ->
                        Row {
                            // Row number labels
                            Box(
                                modifier = Modifier
                                    .size(width = 36.dp, height = 36.dp)
                                    .background(Color(0xFFEDF2F7))
                                    .border(1.dp, Color(0xFFCBD5E0)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = row.toString(),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.DarkGray
                                )
                            }

                            // Dynamic cell values
                            columns.forEach { col ->
                                val cellKey = "$col$row"
                                val isSelected = selectedCellKey == cellKey
                                val cell = cellMap[cellKey] ?: SheetCell()
                                
                                // Call evaluator helper if prefix is '='
                                val displayValue = remember(cell.value, cellMap) {
                                    SpreadsheetEvaluator.evaluate(cell.value, cellMap)
                                }

                                val cellBgColor = parseColor(cell.bgHex, Color.White)
                                val cellTextColor = parseColor(cell.textColorHex, Color.Black)

                                Box(
                                    modifier = Modifier
                                        .size(width = 95.dp, height = 36.dp)
                                        .background(cellBgColor)
                                        .border(
                                            if (isSelected) BorderStroke(2.dp, Color(0xFF047857)) 
                                            else BorderStroke(0.5.dp, Color(0xFFCBD5E0))
                                        )
                                        .clickable {
                                            selectedCellKey = cellKey
                                        }
                                        .padding(4.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Text(
                                        text = displayValue,
                                        fontSize = 11.sp,
                                        fontWeight = if (cell.isBold) FontWeight.Bold else FontWeight.Normal,
                                        color = cellTextColor,
                                        maxLines = 1,
                                        textAlign = if (displayValue.toDoubleOrNull() != null) TextAlign.Right else TextAlign.Left,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Helpful formula tips
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE6FFFA))
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Functions, contentDescription = null, tint = Color(0xFF047857), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Supports Formulas: =SUM(A1:A5), =PRODUCT(B2:C2), =AVG(B3:B6), =SUBTRACT(C3:B3)",
                        fontSize = 9.sp,
                        color = Color(0xFF065F46),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Rename dialog popup
        if (showRenameDialog) {
            AlertDialog(
                onDismissRequest = { showRenameDialog = false },
                title = { Text("Rename Spreadsheet", fontWeight = FontWeight.Bold) },
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
