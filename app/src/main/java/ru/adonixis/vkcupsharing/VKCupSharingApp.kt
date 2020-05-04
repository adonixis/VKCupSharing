package ru.adonixis.vkcupsharing

import android.app.Application
import com.vk.api.sdk.VK
import com.vk.api.sdk.VKTokenExpiredHandler
import ru.adonixis.vkcupsharing.activity.WelcomeActivity

class VKCupSharingApp: Application() {
    override fun onCreate() {
        super.onCreate()
        VK.addTokenExpiredHandler(tokenTracker)
    }

    private val tokenTracker = object: VKTokenExpiredHandler {
        override fun onTokenExpired() {
            WelcomeActivity.startFrom(this@VKCupSharingApp)
        }
    }
}