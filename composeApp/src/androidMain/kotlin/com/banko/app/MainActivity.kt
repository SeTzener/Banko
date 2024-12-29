package com.banko.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.remember
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.retainedComponent
import com.banko.app.ui.screens.navigation.RootComponent

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalDecomposeApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = retainedComponent {
            RootComponent(
                componentContext = it
            )
        }
        setContent {
            App(root = root, prefs = remember { createDataStore(applicationContext) })
        }
    }
}

//@Preview
//@Composable
//fun AppAndroidPreview() {
//    App()
//}