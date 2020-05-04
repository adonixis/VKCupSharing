package ru.adonixis.vkcupsharing.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.vk.api.sdk.VK
import com.vk.api.sdk.auth.VKAccessToken
import com.vk.api.sdk.auth.VKAuthCallback
import com.vk.api.sdk.auth.VKScope
import kotlinx.android.synthetic.main.activity_welcome.*
import ru.adonixis.vkcupsharing.R

class WelcomeActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (VK.isLoggedIn()) {
            SharingActivity.startFrom(this)
            finish()
            return
        }
        setContentView(R.layout.activity_welcome)

        btnVKLogin.setOnClickListener {
            VK.login(this, arrayListOf(VKScope.WALL, VKScope.PHOTOS))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val callback = object: VKAuthCallback {
            override fun onLogin(token: VKAccessToken) {
                SharingActivity.startFrom(this@WelcomeActivity)
                finish()
            }

            override fun onLoginFailed(errorCode: Int) {
            }
        }
        if (!VK.onActivityResult(requestCode, resultCode, data, callback)) {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    companion object {
        fun startFrom(context: Context) {
            val intent = Intent(context, WelcomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            context.startActivity(intent)
        }
    }
}