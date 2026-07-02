package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.GameScreen
import com.example.ui.GameViewModel
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val gameViewModel: GameViewModel = viewModel()
                    val currentScreen by gameViewModel.currentScreen.collectAsState()

                    when (currentScreen) {
                        GameScreen.MENU -> MenuScreen(viewModel = gameViewModel)
                        GameScreen.GARAGE -> GarageScreen(viewModel = gameViewModel)
                        GameScreen.IGNITION -> IgnitionScreen(viewModel = gameViewModel)
                        GameScreen.RACE -> RaceScreen(viewModel = gameViewModel)
                        GameScreen.FINISH -> FinishScreen(viewModel = gameViewModel)
                    }
                }
            }
        }
    }
}
