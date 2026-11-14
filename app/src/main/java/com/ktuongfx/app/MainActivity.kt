package com.ktuongfx.app

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { KTuongApp() }
    }
}

@Composable
fun KTuongApp() {
    MaterialTheme(
        colorScheme = darkColorScheme(primary = AppGold, background = AppBlack, surface = AppBlack)
    ) {
        val context = LocalContext.current
        var tab by rememberSaveable { mutableIntStateOf(0) }
        val tabs = listOf(
            "Trang chủ" to Icons.Filled.Home,
            "Lịch chiếu" to Icons.Filled.DateRange,
            "Tài khoản" to Icons.Filled.Person
        )
        Scaffold(
            containerColor = AppBlack,
            bottomBar = {
                NavigationBar(containerColor = AppBlack) {
                    tabs.forEachIndexed { i, (label, icon) ->
                        NavigationBarItem(
                            selected = tab == i,
                            onClick = { tab = i },
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = AppGold,
                                selectedTextColor = AppGold,
                                indicatorColor = AppCard,
                                unselectedIconColor = Color(0xFF8A8A99),
                                unselectedTextColor = Color(0xFF8A8A99)
                            )
                        )
                    }
                }
            }
        ) { padding ->
            Box(Modifier.padding(bottom = padding.calculateBottomPadding())) {
                if (tab == 0) {
                    HomeScreen(onMovieClick = {
                        Toast.makeText(context, it.name, Toast.LENGTH_SHORT).show()
                    })
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Sắp có", color = Color(0xFFA0A0B0))
                    }
                }
            }
        }
    }
}
