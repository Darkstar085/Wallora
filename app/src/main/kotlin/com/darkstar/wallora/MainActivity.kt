package com.darkstar.wallora

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.remember
import com.darkstar.wallora.data.PreferencesStore
import com.darkstar.wallora.ui.theme.WalloraTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val preferences = remember { PreferencesStore(applicationContext) }
            WalloraTheme(
                themeMode = preferences.themeMode,
                dynamicColors = preferences.dynamicColors,
            ) {
                WalloraApp(preferences)
            }
        }
    }
}
