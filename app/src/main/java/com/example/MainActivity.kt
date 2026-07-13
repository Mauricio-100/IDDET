package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.AppDatabase
import com.example.data.IddetRepository
import com.example.ui.IddetViewModel
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.MainScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val db = AppDatabase.getDatabase(this)
        val prefs = getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
        val repository = IddetRepository(db.userDao(), db.actfileDao(), db.messageDao(), db.followDao(), db.commentDao(), db.notificationDao(), prefs)
        
        val factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return IddetViewModel(repository) as T
            }
        }

        setContent {
            MyApplicationTheme {
                var showSplash by remember { mutableStateOf(true) }
                
                if (showSplash) {
                    SplashScreen(onTimeout = { showSplash = false })
                } else {
                    val viewModel: IddetViewModel = viewModel(factory = factory)
                    val currentUser by viewModel.currentUser.collectAsState()
                    
                    if (currentUser == null) {
                        AuthScreen(viewModel)
                    } else {
                        MainScreen(viewModel)
                    }
                }
            }
        }
    }
}
