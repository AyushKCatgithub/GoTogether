package com.quantax.gotogether

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.quantax.gotogether.navigation.GoTogetherNavGraph
import com.quantax.gotogether.ui.theme.GoTogetherTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GoTogetherTheme {
                val navController = rememberNavController()
                GoTogetherNavGraph(
                    navController = navController,
                    context = this@MainActivity
                )
            }
        }
    }
}
