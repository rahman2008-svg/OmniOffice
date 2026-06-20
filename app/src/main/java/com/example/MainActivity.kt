package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.data.DocumentViewModel
import com.example.data.Screen
import com.example.ui.OfficeDashboard
import com.example.ui.PdfReaderScreen
import com.example.ui.SheetEditorScreen
import com.example.ui.SlideEditorScreen
import com.example.ui.WordEditorScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        val viewModel: DocumentViewModel = viewModel()
        val currentScreen by viewModel.currentScreen.collectAsState()

        // Handle physical back button/gesture intercept
        if (currentScreen != Screen.Dashboard) {
          BackHandler {
            viewModel.goBack()
          }
        }

        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          when (val screen = currentScreen) {
            is Screen.Dashboard -> {
              OfficeDashboard(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
              )
            }
            is Screen.WordEditor -> {
              WordEditorScreen(
                viewModel = viewModel,
                docId = screen.docId,
                modifier = Modifier.padding(innerPadding)
              )
            }
            is Screen.SheetEditor -> {
              SheetEditorScreen(
                viewModel = viewModel,
                docId = screen.docId,
                modifier = Modifier.padding(innerPadding)
              )
            }
            is Screen.SlideEditor -> {
              SlideEditorScreen(
                viewModel = viewModel,
                docId = screen.docId,
                modifier = Modifier.padding(innerPadding)
              )
            }
            is Screen.PdfReader -> {
              PdfReaderScreen(
                viewModel = viewModel,
                docId = screen.docId,
                modifier = Modifier.padding(innerPadding)
              )
            }
          }
        }
      }
    }
  }
}

