package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.gps.GpsTracker
import com.example.localization.AppLanguage
import com.example.localization.LocalizationManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read app name string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("SafeRide", appName)
    }

    @Test
    fun `verify localization dictionary supports all 11 languages`() {
        AppLanguage.values().forEach { lang ->
            val strings = LocalizationManager.getStrings(lang)
            assertEquals("SafeRide", strings.appName)
            assertTrue(strings.busOnTheWay.isNotEmpty())
            assertTrue(strings.eta.isNotEmpty())
        }
    }

    @Test
    fun `verify distance calculation logic`() {
        val dist = GpsTracker.calculateDistanceKm(19.1628, 77.3175, 19.1555, 77.3265)
        assertTrue(dist > 0.5 && dist < 3.0)
    }

    @Test
    fun `verify bus online freshness threshold`() {
        val now = System.currentTimeMillis()
        assertTrue(GpsTracker.isVehicleOnline(now - 10_000))
        assertTrue(!GpsTracker.isVehicleOnline(now - 120_000))
    }
}
