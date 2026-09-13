package com.naymyo.warforge

import android.app.Application
import com.naymyo.warforge.data.Catalog
import com.naymyo.warforge.data.ModelStore
import com.naymyo.warforge.data.Progress
import com.naymyo.warforge.ui.Music

class WarforgeApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ModelStore.init(this)
        Music.enabled = Progress(this).musicEnabled
        // Build the catalogue off the main thread so the hangar can draw immediately.
        // Only the flat silhouettes are built here; 3D solids stay lazy until a vehicle
        // is opened in the museum.
        Thread({ Catalog.campaign }, "warforge-catalog").apply { isDaemon = true }.start()
    }
}
