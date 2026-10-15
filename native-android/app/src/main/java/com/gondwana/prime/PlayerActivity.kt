package com.gondwana.prime

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

/** Native Media3 player. Backend should provide an authorized HTTPS URL for real catalog items. */
class PlayerActivity : ComponentActivity() {
    companion object {
        const val EXTRA_VIDEO_URL = "video_url"
        // Demo only: third-party public sample, not an OTT catalog item.
        const val DEMO_URL = "https://storage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val url = intent.getStringExtra(EXTRA_VIDEO_URL)
            ?.takeIf { it.startsWith("https://", ignoreCase = true) } ?: DEMO_URL
        setContent { NativeVideoPlayer(url) }
    }
}

@Composable
private fun NativeVideoPlayer(url: String) {
    val context = LocalContext.current
    var status by remember(url) { mutableStateOf("वीडियो लोड हो रहा है…") }
    val player = remember(url) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(url))
            volume = 1f
            playWhenReady = true
        }
    }
    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                status = when (playbackState) {
                    Player.STATE_BUFFERING -> "वीडियो बफर हो रहा है…"
                    Player.STATE_READY -> "वीडियो तैयार है • आवाज़ के लिए फोन का मीडिया वॉल्यूम जाँचें"
                    Player.STATE_ENDED -> "वीडियो समाप्त"
                    else -> "वीडियो लोड हो रहा है…"
                }
            }
            override fun onPlayerError(error: PlaybackException) {
                status = "Playback Error: ${error.errorCodeName}"
            }
        }
        player.addListener(listener)
        player.prepare()
        onDispose {
            player.removeListener(listener)
            player.release()
        }
    }
    Column(Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            factory = { PlayerView(it).apply { this.player = player; useController = true } }
        )
        Text(
            text = status,
            color = Color.White,
            modifier = Modifier.fillMaxWidth().padding(12.dp)
        )
    }
}
