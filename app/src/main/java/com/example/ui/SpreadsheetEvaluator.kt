package com.example.ui

import com.example.data.SheetCell
import java.util.Locale

object SpreadsheetEvaluator {

    fun evaluate(cellValue: String, cells: Map<String, SheetCell>): String {
        if (!cellValue.startsWith("=")) return cellValue
        
        val formula = cellValue.substring(1).trim().uppercase(Locale.US)
        
        return try {
            when {
                formula.startsWith("SUM(") -> {
                    val range = extractRange(formula, "SUM(")
                    val values = getValuesFromRange(range, cells)
                    val sum = values.sum()
                    formatResult(sum)
                }
                formula.startsWith("PRODUCT(") -> {
                    val range = extractRange(formula, "PRODUCT(")
                    val values = getValuesFromRange(range, cells)
                    if (values.isEmpty()) "0" else {
                        val product = values.fold(1.0) { acc, d -> acc * d }
                        formatResult(product)
                    }
                }
                formula.startsWith("AVG(") -> {
                    val range = extractRange(formula, "AVG(")
                    val values = getValuesFromRange(range, cells)
                    if (values.isEmpty()) "0" else formatResult(values.average())
                }
                formula.startsWith("AVERAGE(") -> {
                    val range = extractRange(formula, "AVERAGE(")
                    val values = getValuesFromRange(range, cells)
                    if (values.isEmpty()) "0" else formatResult(values.average())
                }
                formula.startsWith("SUBTRACT(") -> {
                    val range = extractRange(formula, "SUBTRACT(")
                    val values = getValuesFromRange(range, cells)
                    if (values.size >= 2) {
                        formatResult(values[0] - values[1])
                    } else if (values.size == 1) {
                        formatResult(values[0])
                    } else "0"
                }
                else -> {
                    // Try to evaluate direct cell value references, e.g. =A1
                    if (formula.matches(Regex("^[A-Z][0-9]+$"))) {
                        cells[formula]?.value?.let { evaluate(it, cells) } ?: "0"
                    } else {
                        "Err: Formula"
                    }
                }
            }
        } catch (e: Exception) {
            "Err: Math"
        }
    }

    private fun extractRange(formula: String, prefix: String): String {
        val start = prefix.length
        val end = formula.lastIndexOf(')')
        if (end == -1 || end <= start) return ""
        return formula.substring(start, end).trim()
    }

    private fun getValuesFromRange(range: String, cells: Map<String, SheetCell>): List<Double> {
        if (range.isBlank()) return emptyList()
        val list = mutableListOf<Double>()
        
        val parts = range.split(":")
        if (parts.size == 1) {
            // Single cell evaluation, or comma-separated cells
            val subParts = range.split(",")
            for (p in subParts) {
                val cleanKey = p.trim()
                val valueStr = cells[cleanKey]?.value ?: ""
                // Recursively evaluate if the referenced cell is also a formula
                val evaluated = if (valueStr.startsWith("=")) evaluate(valueStr, cells) else valueStr
                evaluated.toDoubleOrNull()?.let { list.add(it) }
            }
        } else if (parts.size == 2) {
            // Col-Row range e.g. B3:B6
            val startCell = parts[0].trim()
            val endCell = parts[1].trim()
            
            if (startCell.length >= 2 && endCell.length >= 2) {
                val startCol = startCell[0]
                val startRowStr = startCell.substring(1)
                val endCol = endCell[0]
                val endRowStr = endCell.substring(1)
                
                val startRow = startRowStr.toIntOrNull() ?: 1
                val endRow = endRowStr.toIntOrNull() ?: 1
                
                val colStart = minOf(startCol, endCol)
                val colEnd = maxOf(startCol, endCol)
                val rowStart = minOf(startRow, endRow)
                val rowEnd = maxOf(startRow, endRow)
                
                for (c in colStart..colEnd) {
                    for (r in rowStart..rowEnd) {
                        val key = "$c$r"
                        val valueStr = cells[key]?.value ?: ""
                        val evaluated = if (valueStr.startsWith("=")) evaluate(valueStr, cells) else valueStr
                        evaluated.toDoubleOrNull()?.let { list.add(it) }
                    }
                }
            }
        }
        
        return list
    }

    private fun formatResult(num: Double): String {
        return if (num % 1.0 == 0.0) {
            num.toLong().toString()
        } else {
            String.format(Locale.US, "%.2f", num)
        }
    }
}
