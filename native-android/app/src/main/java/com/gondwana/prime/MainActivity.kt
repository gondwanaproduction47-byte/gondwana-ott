package com.gondwana.prime

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { GondwanaPrimeApp() }
    }
}

@Composable
private fun GondwanaPrimeApp() {
    var selected by remember { mutableStateOf("होम") }
    val gold = Color(0xFFEBC36E)
    MaterialTheme(colorScheme = darkColorScheme(primary = gold)) {
        Scaffold(containerColor = Color(0xFF0D0B12), bottomBar = {
            NavigationBar(containerColor = Color(0xFF1B1722)) {
                listOf("होम", "फिल्में", "इवेंट", "मेरा खाता").forEach { item ->
                    NavigationBarItem(selected = selected == item, onClick = { selected = item }, icon = {}, label = { Text(item) })
                }
            }
        }) { padding ->
            Column(Modifier.fillMaxSize().padding(padding).padding(20.dp), horizontalAlignment = Alignment.Start) {
                Text("GONDWANA PRIME", style = MaterialTheme.typography.headlineMedium, color = gold)
                Spacer(Modifier.height(24.dp))
                Text(selected, style = MaterialTheme.typography.headlineLarge, color = Color.White)
                Spacer(Modifier.height(12.dp))
                Text("फिल्में, वेब सीरीज़ और लेटेस्ट इवेंट एक ही ऐप में", color = Color.LightGray)
            }
        }
    }
}
