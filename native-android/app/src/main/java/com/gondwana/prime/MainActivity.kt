package com.gondwana.prime

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
    val background = Color(0xFF0D0B12)
    MaterialTheme(colorScheme = darkColorScheme(primary = gold, background = background)) {
        Scaffold(
            containerColor = background,
            bottomBar = {
                NavigationBar(containerColor = Color(0xFF1A1621)) {
                    listOf("होम", "फिल्में", "इवेंट", "मेरा खाता").forEach { item ->
                        NavigationBarItem(
                            selected = selected == item,
                            onClick = { selected = item },
                            icon = { Text(if (item == "होम") "⌂" else "•") },
                            label = { Text(item) }
                        )
                    }
                }
            }
        ) { padding ->
            Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
                Header(gold)
                if (selected == "होम") {
                    HeroBanner(gold)
                    Section("लेटेस्ट इवेंट", listOf("सिंगिंग ऑडिशन", "डांस चैंपियनशिप", "ग्रैंड फिनाले"), gold)
                    Section("फिल्में", listOf("जंगल सत्याग्रह", "वन योद्धा", "नई कहानी"), gold)
                    Section("वेब सीरीज़", listOf("एपिसोड 1", "एपिसोड 2", "आने वाला एपिसोड"), gold)
                } else {
                    Text(selected, style = MaterialTheme.typography.headlineLarge, color = Color.White, modifier = Modifier.padding(20.dp))
                    Text("यह सेक्शन जल्द अपडेट किया जाएगा।", color = Color.LightGray, modifier = Modifier.padding(horizontal = 20.dp))
                }
            }
        }
    }
}

@Composable
private fun Header(gold: Color) {
    Row(Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(48.dp).background(Color(0xFF38253D), RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
            Text("GP", color = gold, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text("GONDWANA PRIME", color = Color.White, fontWeight = FontWeight.Bold)
            Text("OTT · EVENTS & CINEMA", color = gold, style = MaterialTheme.typography.labelSmall)
        }
        Spacer(Modifier.weight(1f))
        Text("⌕", color = Color.White, style = MaterialTheme.typography.headlineMedium)
    }
}

@Composable
private fun HeroBanner(gold: Color) {
    Card(Modifier.fillMaxWidth().padding(horizontal = 20.dp), shape = RoundedCornerShape(22.dp)) {
        Box(Modifier.height(210.dp).fillMaxWidth().background(Brush.linearGradient(listOf(Color(0xFF452449), Color(0xFF16121D))))) {
            Column(Modifier.align(Alignment.BottomStart).padding(20.dp)) {
                Text("GONDWANA ORIGINAL", color = gold, style = MaterialTheme.typography.labelMedium)
                Text("जंगल सत्याग्रह", color = Color.White, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text("The Untold History · Coming Soon", color = Color.LightGray)
                Spacer(Modifier.height(10.dp))
                Button(onClick = {}, colors = ButtonDefaults.buttonColors(containerColor = gold)) { Text("अभी देखें", color = Color(0xFF201821)) }
            }
        }
    }
}

@Composable
private fun Section(title: String, itemsList: List<String>, gold: Color) {
    Column(Modifier.padding(top = 24.dp)) {
        Text(title, color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 20.dp))
        Spacer(Modifier.height(10.dp))
        LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(itemsList) { name ->
                Card(shape = RoundedCornerShape(14.dp), modifier = Modifier.width(150.dp).height(105.dp)) {
                    Column(Modifier.fillMaxSize().background(Color(0xFF211A29)).padding(12.dp), verticalArrangement = Arrangement.Bottom) {
                        Text(name, color = Color.White, fontWeight = FontWeight.SemiBold)
                        Text("▶  Watch", color = gold, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}
