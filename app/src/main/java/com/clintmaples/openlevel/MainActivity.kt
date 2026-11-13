package com.clintmaples.openlevel

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.clintmaples.openlevel.ui.LevelRoute
import com.clintmaples.openlevel.ui.LevelViewModel
import com.clintmaples.openlevel.ui.theme.OpenLevelTheme

class MainActivity : ComponentActivity() {
    private val viewModel: LevelViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(0xFF090C11.toInt()),
            navigationBarStyle = SystemBarStyle.dark(0xFF090C11.toInt()),
        )
        setContent {
            OpenLevelTheme {
                LevelRoute(viewModel)
            }
        }
    }
}
