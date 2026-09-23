package com.darkstar.wallora

import android.app.Application
import com.darkstar.wallora.data.UpdateManager
import com.darkstar.wallora.data.UpdateNotificationHelper

class WalloraApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        UpdateNotificationHelper.createChannel(this)
        UpdateManager.enqueuePeriodicCheck(this)
    }
}
