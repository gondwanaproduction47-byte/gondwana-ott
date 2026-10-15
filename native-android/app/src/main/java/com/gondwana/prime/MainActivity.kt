package com.gondwana.prime

import android.os.Bundle
import android.net.Uri
import android.widget.VideoView
import androidx.compose.ui.viewinterop.AndroidView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

private const val SUPABASE_URL = "https://gadpntuncgwkxrpqtxym.supabase.co"
private const val SUPABASE_KEY = "sb_publishable_eQY9fZ6zLnQNzWBj2jTl0A_U5CqP6F-"
private val Gold = Color(0xFFEBC36E)
private val Ink = Color(0xFF0D0B12)
private val CardBg = Color(0xFF1B1623)

private data class AuthUser(val id: String, val token: String, val label: String, val role: String = "user")

private object SupabaseApi {
    private fun request(path: String, method: String, body: String? = null, token: String? = null): JSONObject {
        val connection = (URL(SUPABASE_URL + path).openConnection() as HttpURLConnection)
        connection.requestMethod = method
        connection.setRequestProperty("apikey", SUPABASE_KEY)
        connection.setRequestProperty("Content-Type", "application/json")
        if (token != null) connection.setRequestProperty("Authorization", "Bearer $token")
        connection.doInput = true
        if (body != null) { connection.doOutput = true; connection.outputStream.use { it.write(body.toByteArray()) } }
        val stream = if (connection.responseCode in 200..299) connection.inputStream else connection.errorStream
        val text = stream?.bufferedReader()?.use { it.readText() } ?: "{}"
        if (connection.responseCode !in 200..299) throw IllegalStateException(JSONObject(text).optString("msg", JSONObject(text).optString("message", "Login failed")))
        return JSONObject(text)
    }
    suspend fun signIn(identifier: String, password: String): AuthUser = withContext(Dispatchers.IO) {
        val key = if (identifier.contains("@")) "email" else "phone"
        val json = request("/auth/v1/token?grant_type=password", "POST", JSONObject().put(key, identifier).put("password", password).toString())
        val user = json.getJSONObject("user")
        AuthUser(user.getString("id"), json.getString("access_token"), identifier, profileRole(json.getString("access_token"), user.getString("id")))
    }
    private fun profileRole(token: String, id: String): String {
        return try {
            val c = (URL(SUPABASE_URL + "/rest/v1/profiles?id=eq." + id + "&select=role").openConnection() as HttpURLConnection)
            c.requestMethod = "GET"; c.setRequestProperty("apikey", SUPABASE_KEY); c.setRequestProperty("Authorization", "Bearer $token")
            val text = c.inputStream.bufferedReader().use { it.readText() }
            JSONArray(text).optJSONObject(0)?.optString("role", "user") ?: "user"
        } catch (_: Exception) { "user" }
    }
    suspend fun saveContent(token: String, ownerId: String, title: String, type: String, videoUrl: String, posterUrl: String): Unit = withContext(Dispatchers.IO) {
        request("/rest/v1/content", "POST", JSONObject().put("title", title).put("content_type", type).put("video_url", videoUrl).put("poster_url", posterUrl).put("status", "published").put("owner_id", ownerId).toString(), token)
    }
    suspend fun saveAd(token: String, ownerId: String, title: String, mediaType: String, mediaUrl: String, targetUrl: String): Unit = withContext(Dispatchers.IO) {
        request("/rest/v1/ads", "POST", JSONObject().put("title", title).put("media_type", mediaType).put("media_url", mediaUrl).put("target_url", targetUrl).put("placement", "home_below_hero").put("is_active", true).put("created_by", ownerId).toString(), token)
    }
    suspend fun signUp(identifier: String, password: String, name: String, city: String): AuthUser = withContext(Dispatchers.IO) {
        val key = if (identifier.contains("@")) "email" else "phone"
        val metadata = JSONObject().put("name", name).put("mobile", if (key == "phone") identifier else "").put("city", city)
        val json = request("/auth/v1/signup", "POST", JSONObject().put(key, identifier).put("password", password).put("data", metadata).toString())
        val user = json.optJSONObject("user") ?: throw IllegalStateException("Email/phone confirmation required in Supabase")
        val token = json.optString("access_token")
        if (token.isBlank()) throw IllegalStateException("Registration complete. Please confirm your account, then login.")
        AuthUser(user.getString("id"), token, identifier, "user")
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContent { GondwanaPrimeApp() } }
}

@Composable
private fun GondwanaPrimeApp() {
    var user by remember { mutableStateOf<AuthUser?>(null) }
    var showLogin by remember { mutableStateOf(false) }
    MaterialTheme(colorScheme = darkColorScheme(primary = Gold, background = Ink, surface = CardBg)) {
        if (showLogin) LoginScreen(onSuccess = { user = it; showLogin = false }, onCancel = { showLogin = false })
        else MainShell(user = user, onLogin = { showLogin = true }, onLogout = { user = null }, onWatch = { if (user == null) showLogin = true })
    }
}

@Composable
private fun LoginScreen(onSuccess: (AuthUser) -> Unit, onCancel: () -> Unit = {}) {
    var register by remember { mutableStateOf(false) }; var identifier by remember { mutableStateOf("") }; var password by remember { mutableStateOf("") }; var name by remember { mutableStateOf("") }; var city by remember { mutableStateOf("") }; var message by remember { mutableStateOf("") }; var busy by remember { mutableStateOf(false) }; val scope = rememberCoroutineScope()
    Column(Modifier.fillMaxSize().background(Ink).verticalScroll(rememberScrollState()).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(36.dp)); Text("GP", color = Gold, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold); Text("GONDWANA PRIME", color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold); Text(if (register) "नया खाता बनाएँ" else "फिल्म देखने के लिए लॉगिन करें", color = Color.LightGray, modifier = Modifier.padding(top = 8.dp, bottom = 22.dp))
        if (register) { OutlinedTextField(name, { name = it }, label = { Text("नाम") }, modifier = Modifier.fillMaxWidth(), singleLine = true); Spacer(Modifier.height(10.dp)); OutlinedTextField(city, { city = it }, label = { Text("शहर") }, modifier = Modifier.fillMaxWidth(), singleLine = true); Spacer(Modifier.height(10.dp)) }
        OutlinedTextField(identifier, { identifier = it }, label = { Text("Gmail या मोबाइल") }, modifier = Modifier.fillMaxWidth(), singleLine = true); Spacer(Modifier.height(10.dp)); OutlinedTextField(password, { password = it }, label = { Text("पासवर्ड") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        if (message.isNotBlank()) Text(message, color = Color(0xFFFFB4AB), modifier = Modifier.padding(top = 12.dp))
        Button(enabled = !busy && identifier.isNotBlank() && password.length >= 6, onClick = { busy = true; message = ""; scope.launch(Dispatchers.Main) { try { onSuccess(if (register) SupabaseApi.signUp(identifier.trim(), password, name.trim(), city.trim()) else SupabaseApi.signIn(identifier.trim(), password)) } catch (e: Exception) { message = e.message ?: "कृपया फिर कोशिश करें" } finally { busy = false } } }, colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Ink), modifier = Modifier.fillMaxWidth().padding(top = 20.dp)) { Text(if (busy) "कृपया प्रतीक्षा करें…" else if (register) "रजिस्टर करें" else "लॉगिन करें") }
        TextButton(onClick = { register = !register; message = "" }) { Text(if (register) "पहले से खाता है? लॉगिन" else "नया खाता बनाएँ", color = Gold) }; TextButton(onClick = onCancel) { Text("बाद में", color = Color.LightGray) }
    }
}

@Composable
private fun MainShell(user: AuthUser?, onLogin: () -> Unit, onLogout: () -> Unit, onWatch: () -> Unit) {
    var selected by remember { mutableStateOf("होम") }
    Scaffold(containerColor = Ink, bottomBar = { NavigationBar(containerColor = CardBg) { listOf("होम", "फिल्में", "इवेंट", "मेरा खाता").forEach { item -> NavigationBarItem(selected = selected == item, onClick = { selected = item }, icon = { Text(if (item == "होम") "⌂" else "•") }, label = { Text(item) }) } } }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
            Header()
            if (selected == "होम") { HeroBanner(onWatch); SponsorAdSlot(); Section("लेटेस्ट इवेंट", listOf("सिंगिंग ऑडिशन", "डांस चैंपियनशिप", "ग्रैंड फिनाले"), onWatch); Section("फिल्में", listOf("जंगल सत्याग्रह", "वन योद्धा", "नई कहानी"), onWatch); Section("वेब सीरीज़", listOf("एपिसोड 1", "एपिसोड 2", "आने वाला एपिसोड"), onWatch) }
            else if (selected == "मेरा खाता") { if (user?.role == "admin") AdminScreen(user) else AccountView(user, onLogin, onLogout) }
            else { Text(selected, color = Color.White, style = MaterialTheme.typography.headlineLarge, modifier = Modifier.padding(20.dp)); Text("वीडियो देखने के लिए किसी title पर tap करें।", color = Color.LightGray, modifier = Modifier.padding(horizontal = 20.dp)) }
        }
    }
}

@Composable private fun AccountView(user: AuthUser?, onLogin: () -> Unit, onLogout: () -> Unit) { Text("मेरा खाता", color = Color.White, style = MaterialTheme.typography.headlineLarge, modifier = Modifier.padding(20.dp)); if (user == null) { Text("Movie देखने पर ही login जरूरी है।", color = Color.LightGray, modifier = Modifier.padding(horizontal = 20.dp)); Button(onClick = onLogin, modifier = Modifier.padding(20.dp)) { Text("लॉगिन करें") } } else { Text(user.label, color = Color.LightGray, modifier = Modifier.padding(horizontal = 20.dp)); TextButton(onClick = onLogout) { Text("लॉगआउट", color = Gold) } } }

@Composable private fun AdminScreen(user: AuthUser) { var title by remember { mutableStateOf("") }; var type by remember { mutableStateOf("movie") }; var videoUrl by remember { mutableStateOf("") }; var posterUrl by remember { mutableStateOf("") }; var adTitle by remember { mutableStateOf("") }; var adType by remember { mutableStateOf("image") }; var adUrl by remember { mutableStateOf("") }; var adTarget by remember { mutableStateOf("") }; var message by remember { mutableStateOf("") }; var playUrl by remember { mutableStateOf("") }; val scope = rememberCoroutineScope(); Column(Modifier.padding(20.dp).verticalScroll(rememberScrollState())) { Text("Admin Panel", color = Gold, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold); Text("वीडियो और Sponsor Ads manage करें", color = Color.LightGray); Spacer(Modifier.height(12.dp)); OutlinedTextField(title, { title = it }, label = { Text("वीडियो नाम") }, modifier = Modifier.fillMaxWidth(), singleLine = true); Spacer(Modifier.height(8.dp)); OutlinedTextField(type, { type = it }, label = { Text("Type: movie / web_series / event") }, modifier = Modifier.fillMaxWidth(), singleLine = true); Spacer(Modifier.height(8.dp)); OutlinedTextField(videoUrl, { videoUrl = it }, label = { Text("Direct MP4 / R2 video URL") }, modifier = Modifier.fillMaxWidth(), singleLine = true); Spacer(Modifier.height(8.dp)); OutlinedTextField(posterUrl, { posterUrl = it }, label = { Text("Poster URL (optional)") }, modifier = Modifier.fillMaxWidth(), singleLine = true); Button(onClick = { scope.launch { try { SupabaseApi.saveContent(user.token, user.id, title, type, videoUrl, posterUrl); message = "Video published"; playUrl = videoUrl } catch (e: Exception) { message = e.message ?: "Upload failed" } } }, enabled = title.isNotBlank() && videoUrl.startsWith("http"), modifier = Modifier.fillMaxWidth().padding(top = 12.dp), colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Ink)) { Text("Publish Video") }; Spacer(Modifier.height(28.dp)); Text("Sponsor Ads", color = Gold, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold); OutlinedTextField(adTitle, { adTitle = it }, label = { Text("Ad title") }, modifier = Modifier.fillMaxWidth(), singleLine = true); Spacer(Modifier.height(8.dp)); OutlinedTextField(adType, { adType = it }, label = { Text("image या video") }, modifier = Modifier.fillMaxWidth(), singleLine = true); Spacer(Modifier.height(8.dp)); OutlinedTextField(adUrl, { adUrl = it }, label = { Text("Ad image/video URL") }, modifier = Modifier.fillMaxWidth(), singleLine = true); Spacer(Modifier.height(8.dp)); OutlinedTextField(adTarget, { adTarget = it }, label = { Text("Sponsor link (optional)") }, modifier = Modifier.fillMaxWidth(), singleLine = true); Button(onClick = { scope.launch { try { SupabaseApi.saveAd(user.token, user.id, adTitle, adType, adUrl, adTarget); message = "Sponsor Ad ON" } catch (e: Exception) { message = e.message ?: "Ad save failed" } } }, enabled = adTitle.isNotBlank() && adUrl.startsWith("http"), modifier = Modifier.fillMaxWidth().padding(top = 8.dp), colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Ink)) { Text("Add / ON Sponsor Ad") }; if (message.isNotBlank()) Text(message, color = Gold, modifier = Modifier.padding(top = 10.dp)); if (playUrl.isNotBlank()) { Text("Video Preview", color = Color.White, modifier = Modifier.padding(top = 18.dp)); AndroidView(factory = { context -> VideoView(context) }, update = { view -> view.setVideoURI(Uri.parse(playUrl)); view.setOnPreparedListener { it.isLooping = false; view.start() } }, modifier = Modifier.fillMaxWidth().height(220.dp)) } } }

@Composable private fun SponsorAdSlot() { Card(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF241C2B)), shape = RoundedCornerShape(16.dp)) { Column(Modifier.padding(16.dp)) { Text("SPONSOR AD", color = Gold, style = MaterialTheme.typography.labelMedium); Text("आपके sponsor का ad यहाँ दिखेगा", color = Color.White, fontWeight = FontWeight.SemiBold); Text("Admin Panel से Ad ON करने पर यह slot live होगा।", color = Color.LightGray, style = MaterialTheme.typography.bodySmall) } } }

@Composable private fun Header() { Row(Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(48.dp).background(Color(0xFF38253D), RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) { Text("GP", color = Gold, fontWeight = FontWeight.Bold) }; Spacer(Modifier.width(12.dp)); Column { Text("GONDWANA PRIME", color = Color.White, fontWeight = FontWeight.Bold); Text("OTT · EVENTS & CINEMA", color = Gold, style = MaterialTheme.typography.labelSmall) }; Spacer(Modifier.weight(1f)); Text("⌕", color = Color.White, style = MaterialTheme.typography.headlineMedium) } }
@Composable private fun HeroBanner(onWatch: () -> Unit) { Card(Modifier.fillMaxWidth().padding(horizontal = 20.dp), shape = RoundedCornerShape(22.dp)) { Box(Modifier.height(210.dp).fillMaxWidth().background(Brush.linearGradient(listOf(Color(0xFF452449), Color(0xFF16121D))))) { Column(Modifier.align(Alignment.BottomStart).padding(20.dp)) { Text("GONDWANA ORIGINAL", color = Gold, style = MaterialTheme.typography.labelMedium); Text("जंगल सत्याग्रह", color = Color.White, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold); Text("The Untold History · Coming Soon", color = Color.LightGray); Spacer(Modifier.height(10.dp)); Button(onClick = onWatch, colors = ButtonDefaults.buttonColors(containerColor = Gold)) { Text("अभी देखें", color = Ink) } } } } }
@Composable private fun Section(title: String, itemsList: List<String>, onWatch: () -> Unit) { Column(Modifier.padding(top = 24.dp)) { Text(title, color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 20.dp)); Spacer(Modifier.height(10.dp)); LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) { items(itemsList) { name -> Card(shape = RoundedCornerShape(14.dp), modifier = Modifier.width(150.dp).height(105.dp)) { Column(Modifier.fillMaxSize().background(CardBg).padding(12.dp), verticalArrangement = Arrangement.Bottom) { Text(name, color = Color.White, fontWeight = FontWeight.SemiBold); TextButton(onClick = onWatch, contentPadding = PaddingValues(0.dp)) { Text("▶ Watch", color = Gold, style = MaterialTheme.typography.labelSmall) } } } } } } }
