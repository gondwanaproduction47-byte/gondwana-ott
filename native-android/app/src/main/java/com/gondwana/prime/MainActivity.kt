package com.gondwana.prime

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Ink = Color(0xFF0D0B12); private val CardBg = Color(0xFF1B1623); private val Gold = Color(0xFFEBC36E); private val Muted = Color(0xFFBDB0C7)
private const val WEB_ADMIN = "https://gondwana-ott.gondwanaproduction47.chatgpt.site/"

class MainActivity : ComponentActivity() { override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContent { GondwanaPrimeApp { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(WEB_ADMIN))) } } } }

@Composable fun GondwanaPrimeApp(openAdmin: () -> Unit) {
    var selectedTab by remember { mutableIntStateOf(0) }
    MaterialTheme(colorScheme = darkColorScheme(background = Ink, surface = CardBg, primary = Gold)) {
        Scaffold(containerColor = Ink, bottomBar = { NavigationBar(containerColor = CardBg) {
            listOf("होम" to Icons.Default.Home, "फिल्में" to Icons.Default.Movie, "इवेंट" to Icons.Default.EmojiEvents, "मेरा खाता" to Icons.Default.Person).forEachIndexed { i, item ->
                NavigationBarItem(selected = selectedTab == i, onClick = { selectedTab = i }, icon = { Icon(item.second, null) }, label = { Text(item.first) }, colors = NavigationBarItemDefaults.colors(selectedIconColor = Ink, indicatorColor = Gold, unselectedIconColor = Muted, unselectedTextColor = Muted, selectedTextColor = Gold))
            }
        } }) { pad -> when (selectedTab) { 0 -> HomeScreen(Modifier.padding(pad)); 1 -> CatalogScreen(Modifier.padding(pad)); 2 -> EventScreen(Modifier.padding(pad)); else -> AccountScreen(Modifier.padding(pad), openAdmin) } }
    }
}

@Composable private fun Header() { Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) { Column { Text("GONDWANA", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp); Text("PRIME", color = Gold, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 3.sp) }; Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) { Icon(Icons.Default.Search, "खोजें", tint = Color.White); Icon(Icons.Default.AccountCircle, "खाता", tint = Color.White) } } }

@Composable private fun HomeScreen(modifier: Modifier = Modifier) { Column(modifier.verticalScroll(rememberScrollState())) { Header(); HeroCard(); Text("लेटेस्ट फिल्में", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(18.dp, 10.dp)); Shelf(listOf("धरती के रक्षक", "गोंडवाना की महारानी", "वीरांगना दुर्गावती"), "ऐतिहासिक"); Text("वेब सीरीज़", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(18.dp, 16.dp, 18.dp, 10.dp)); Shelf(listOf("जंगल की पुकार", "नई कहानी"), "एपिसोड जल्द"); Spacer(Modifier.height(24.dp)) } }
@Composable private fun HeroCard() { Box(Modifier.padding(horizontal = 18.dp).fillMaxWidth().height(220.dp).clip(RoundedCornerShape(22.dp)).background(Brush.linearGradient(listOf(Color(0xFF422E4A), Color(0xFF17111F))))) { Column(Modifier.align(Alignment.CenterStart).padding(22.dp)) { Text("GONDWANA ORIGINAL", color = Gold, fontSize = 12.sp, letterSpacing = 2.sp); Text("जंगल सत्याग्रह (1930)", color = Color.White, fontSize = 27.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp)); Text("THE UNTOLD HISTORY", color = Muted, fontSize = 12.sp, letterSpacing = 1.sp); Button(onClick = {}, colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Ink), modifier = Modifier.padding(top = 18.dp)) { Icon(Icons.Default.PlayArrow, null); Text("अभी देखें") } } } }
@Composable private fun Shelf(items: List<String>, meta: String) { Row(Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 18.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) { items.forEach { title -> Column(Modifier.width(145.dp).clickable {}) { Box(Modifier.fillMaxWidth().height(190.dp).clip(RoundedCornerShape(10.dp)).background(Brush.verticalGradient(listOf(Color(0xFF5B3A63), Color(0xFF211929)))), contentAlignment = Alignment.Center) { Icon(Icons.Default.PlayCircle, null, tint = Gold, modifier = Modifier.size(42.dp)) }; Text(title, color = Color.White, fontSize = 15.sp, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 8.dp)); Text(meta, color = Muted, fontSize = 12.sp) } } } }
@Composable private fun CatalogScreen(modifier: Modifier = Modifier) { Column(modifier.verticalScroll(rememberScrollState())) { Header(); Text("फिल्में", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 18.dp)); Row(Modifier.horizontalScroll(rememberScrollState()).padding(18.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) { listOf("सभी", "एक्शन", "ड्रामा", "कॉमेडी", "ऐतिहासिक", "रोमांस").forEach { Text(it, color = if (it == "सभी") Ink else Color.White, modifier = Modifier.clip(RoundedCornerShape(22.dp)).background(if (it == "सभी") Gold else CardBg).padding(horizontal = 16.dp, vertical = 10.dp)) } }; Shelf(listOf("धरती के रक्षक", "जंगल की पुकार", "आदिवासी वीरगाथा", "माटी के लाल"), "फिल्म"); Spacer(Modifier.height(30.dp)) } }
@Composable private fun EventScreen(modifier: Modifier = Modifier) { Column(modifier.padding(18.dp)) { Header(); Text("Active Event", color = Gold, fontSize = 13.sp, letterSpacing = 2.sp); Text("लेटेस्ट इवेंट", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp)); Card(colors = CardDefaults.cardColors(containerColor = CardBg), modifier = Modifier.padding(top = 18.dp).fillMaxWidth()) { Column(Modifier.padding(18.dp)) { Text("सिंगिंग स्टार 2026", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold); Text("परफ़ॉर्मेंस देखें और Admin ON करने पर वोट करें", color = Muted, modifier = Modifier.padding(top = 6.dp)); Button(onClick = {}, modifier = Modifier.padding(top = 18.dp)) { Text("इवेंट खोलें") } } } } }

@Composable private fun AccountScreen(modifier: Modifier = Modifier, openAdmin: () -> Unit) { Column(modifier.verticalScroll(rememberScrollState()).padding(18.dp)) { Header(); Text("मेरा खाता", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold); Card(colors = CardDefaults.cardColors(containerColor = CardBg), modifier = Modifier.padding(top = 18.dp).fillMaxWidth()) { Column(Modifier.padding(18.dp)) { Text("लॉगिन करके अपनी Watch History, My List और Payment देखें।", color = Muted); Button(onClick = {}, modifier = Modifier.padding(top = 18.dp)) { Text("लॉगिन करें") } } }; AdminUploadCard(openAdmin) } }

@Composable private fun AdminUploadCard(openAdmin: () -> Unit) {
    var videoName by remember { mutableStateOf("") }; var posterName by remember { mutableStateOf("") }; var thumbName by remember { mutableStateOf("") }
    val videoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { videoName = it?.lastPathSegment ?: "वीडियो चुनी गई" }
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> if (uri != null) posterName = uri.lastPathSegment ?: "इमेज चुनी गई" }
    val thumbPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> if (uri != null) thumbName = uri.lastPathSegment ?: "इमेज चुनी गई" }
    Card(colors = CardDefaults.cardColors(containerColor = CardBg), modifier = Modifier.padding(top = 18.dp).fillMaxWidth()) { Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Admin File Upload", color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.Bold)
        Text("Video, poster और thumbnail को सुरक्षित R2 uploader में भेजें। Upload केवल Admin web panel से publish होगा।", color = Muted, fontSize = 13.sp)
        OutlinedButton(onClick = { videoPicker.launch("video/*") }, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.VideoFile, null); Spacer(Modifier.width(8.dp)); Text(if (videoName.isBlank()) "Video file चुनें" else videoName) }
        OutlinedButton(onClick = { imagePicker.launch("image/*") }, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.Image, null); Spacer(Modifier.width(8.dp)); Text(if (posterName.isBlank()) "Poster चुनें" else posterName) }
        OutlinedButton(onClick = { thumbPicker.launch("image/*") }, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.PhotoLibrary, null); Spacer(Modifier.width(8.dp)); Text(if (thumbName.isBlank()) "Thumbnail चुनें" else thumbName) }
        Button(onClick = openAdmin, colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Ink), modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.CloudUpload, null); Spacer(Modifier.width(8.dp)); Text("Secure Web Admin में Publish करें") }
        Text("R2 secret key APK में नहीं रखी गई है।", color = Muted, fontSize = 12.sp)
    } }
}
