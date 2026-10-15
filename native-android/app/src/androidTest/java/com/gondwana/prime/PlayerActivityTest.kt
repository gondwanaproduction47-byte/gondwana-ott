package com.gondwana.prime

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PlayerActivityTest {
    @Test
    fun demoPlayerActivityLaunches() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val intent = Intent(context, PlayerActivity::class.java).apply {
            putExtra(PlayerActivity.EXTRA_VIDEO_URL, PlayerActivity.DEMO_URL)
        }
        ActivityScenario.launch<PlayerActivity>(intent).use { scenario ->
            scenario.onActivity { activity ->
                assertNotNull(activity.findViewById<android.view.View>(android.R.id.content))
            }
        }
    }
}
