package com.gondwana.prime

import android.os.Bundle
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
import java.net.HttpURLConnection
import java.net.URL

private const val SUPABASE_URL = "https://gadpntuncgwkxrpqtxym.supabase.co"
private const val SUPABASE_KEY = "sb_publishable_eQY9fZ6zLnQNzWBj2jTl0A_U5CqP6F-"
private val Gold = Color(0xFFEBC36E)
private val Ink = Color(0xFF0D0B12)
private val CardBg = Color(0xFF1B1623)

private data class AuthUser(val id: String, val token: String, val label: String)

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
        AuthUser(user.getString("id"), json.getString("access_token"), identifier)
    }
    suspend fun signUp(identifier: String, password: String, name: String, city: String): AuthUser = withContext(Dispatchers.IO) {
        val key = if (identifier.contains("@")) "email" else "phone"
        val metadata = JSONObject().put("name", name).put("mobile", if (key == "phone") identifier else "").put("city", city)
        val json = request("/auth/v1/signup", "POST", JSONObject().put(key, identifier).put("password", password).put("data", metadata).toString())
        val user = json.optJSONObject("user") ?: throw IllegalStateException("Email/phone confirmation required in Supabase")
        val token = json.optString("access_token")
        if (token.isBlank()) throw IllegalStateException("Registration complete. Please confirm your account, then login.")
        AuthUser(user.getString("id"), token, identifier)
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContent { GondwanaPrimeApp() } }
}

@Composable
private fun GondwanaPrimeApp() {
    var user by remember { mutableStateOf<AuthUser?>(null) }
    MaterialTheme(colorScheme = darkColorScheme(primary = Gold, background = Ink, surface = CardBg)) {
        if (user == null) LoginScreen(onSuccess = { user = it }) else MainShell(user!!, onLogout = { user = null })
    }
}

@Composable
private fun LoginScreen(onSuccess: (AuthUser) -> Unit) {
    var register by remember { mutableStateOf(false) }
    var identifier by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    Column(Modifier.fillMaxSize().background(Ink).verticalScroll(rememberScrollState()).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(48.dp))
        Text("GP", color = Gold, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
        Text("GONDWANA PRIME", color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(if (register) "नया खाता बनाएँ" else "अपने खाते में लॉगिन करें", color = Color.LightGray, modifier = Modifier.padding(top = 8.dp, bottom = 26.dp))
        if (register) {
            OutlinedTextField(name, { name = it }, label = { Text("नाम") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(city, { city = it }, label = { Text("शहर") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Spacer(Modifier.height(10.dp))
        }
        OutlinedTextField(identifier, { identifier = it }, label = { Text("मोबाइल या ईमेल") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(password, { password = it }, label = { Text("पासवर्ड") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        if (message.isNotBlank()) Text(message, color = Color(0xFFFFB4AB), modifier = Modifier.padding(top = 12.dp))
        Button(enabled = !busy && identifier.isNotBlank() && password.length >= 6, onClick = {
            busy = true; message = ""
            scope.launch(Dispatchers.Main) {
                try { onSuccess(if (register) SupabaseApi.signUp(identifier.trim(), password, name.trim(), city.trim()) else SupabaseApi.signIn(identifier.trim(), password)) }
                catch (e: Exception) { message = e.message ?: "कृपया फिर कोशिश करें" }
                finally { busy = false }
            }
        }, colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Ink), modifier = Modifier.fillMaxWidth().padding(top = 20.dp)) { Text(if (busy) "कृपया प्रतीक्षा करें…" else if (register) "रजिस्टर करें" else "लॉगिन करें") }
        TextButton(onClick = { register = !register; message = "" }) { Text(if (register) "पहले से खाता है? लॉगिन" else "नया खाता बनाएँ", color = Gold) }
        Text("बिना OTP: मोबाइल/ईमेल और पासवर्ड से प्रवेश", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun MainShell(user: AuthUser, onLogout: () -> Unit) {
    var selected by remember { mutableStateOf("होम") }
    Scaffold(containerColor = Ink, bottomBar = { NavigationBar(containerColor = CardBg) { listOf("होम", "फिल्में", "इवेंट", "मेरा खाता").forEach { item -> NavigationBarItem(selected = selected == item, onClick = { selected = item }, icon = { Text(if (item == "होम") "⌂" else "•") }, label = { Text(item) }) } } }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
            Header()
            if (selected == "होम") { HeroBanner(); Section("लेटेस्ट इवेंट", listOf("सिंगिंग ऑडिशन", "डांस चैंपियनशिप", "ग्रैंड फिनाले")); Section("फिल्में", listOf("जंगल सत्याग्रह", "वन योद्धा", "नई कहानी")); Section("वेब सीरीज़", listOf("एपिसोड 1", "एपिसोड 2", "आने वाला एपिसोड")) }
            else if (selected == "मेरा खाता") { Text("मेरा खाता", color = Color.White, style = MaterialTheme.typography.headlineLarge, modifier = Modifier.padding(20.dp)); Text(user.label, color = Color.LightGray, modifier = Modifier.padding(horizontal = 20.dp)); TextButton(onClick = onLogout) { Text("लॉगआउट", color = Gold) } }
            else { Text(selected, color = Color.White, style = MaterialTheme.typography.headlineLarge, modifier = Modifier.padding(20.dp)); Text("यह सेक्शन जल्द अपडेट किया जाएगा।", color = Color.LightGray, modifier = Modifier.padding(horizontal = 20.dp)) }
        }
    }
}

@Composable private fun Header() { Row(Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(48.dp).background(Color(0xFF38253D), RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) { Text("GP", color = Gold, fontWeight = FontWeight.Bold) }; Spacer(Modifier.width(12.dp)); Column { Text("GONDWANA PRIME", color = Color.White, fontWeight = FontWeight.Bold); Text("OTT · EVENTS & CINEMA", color = Gold, style = MaterialTheme.typography.labelSmall) }; Spacer(Modifier.weight(1f)); Text("⌕", color = Color.White, style = MaterialTheme.typography.headlineMedium) } }
@Composable private fun HeroBanner() { Card(Modifier.fillMaxWidth().padding(horizontal = 20.dp), shape = RoundedCornerShape(22.dp)) { Box(Modifier.height(210.dp).fillMaxWidth().background(Brush.linearGradient(listOf(Color(0xFF452449), Color(0xFF16121D))))) { Column(Modifier.align(Alignment.BottomStart).padding(20.dp)) { Text("GONDWANA ORIGINAL", color = Gold, style = MaterialTheme.typography.labelMedium); Text("जंगल सत्याग्रह", color = Color.White, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold); Text("The Untold History · Coming Soon", color = Color.LightGray); Spacer(Modifier.height(10.dp)); Button(onClick = {}, colors = ButtonDefaults.buttonColors(containerColor = Gold)) { Text("अभी देखें", color = Ink) } } } } }
@Composable private fun Section(title: String, itemsList: List<String>) { Column(Modifier.padding(top = 24.dp)) { Text(title, color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 20.dp)); Spacer(Modifier.height(10.dp)); LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) { items(itemsList) { name -> Card(shape = RoundedCornerShape(14.dp), modifier = Modifier.width(150.dp).height(105.dp)) { Column(Modifier.fillMaxSize().background(CardBg).padding(12.dp), verticalArrangement = Arrangement.Bottom) { Text(name, color = Color.White, fontWeight = FontWeight.SemiBold); Text("▶ Watch", color = Gold, style = MaterialTheme.typography.labelSmall) } } } } } }
