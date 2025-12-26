package com.sk.revisit.activities

import android.content.Intent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.sk.revisit.Revisit

open class BaseActivity : AppCompatActivity() {

    protected val TAG: String = this::class.java.simpleName

    val revisitApp: Revisit
        get() = application as Revisit

    fun alert(msg: String?) {
        msg?.let { Toast.makeText(this, it, Toast.LENGTH_LONG).show() }
    }

    /**
     * Modern navigation: Resets the back press timer and starts activity
     */
    inline fun <reified T : AppCompatActivity> startMyActivity(fini: Boolean = false) {
        // Reset the time-based back press logic
        MainActivity.lastBackPressTime = 0L

        startActivity(Intent(this, T::class.java))
        if (fini) finish()
    }

    // Overload for Java compatibility if needed
    fun startMyActivity(activityClass: Class<*>, fini: Boolean = false) {
        MainActivity.lastBackPressTime = 0L
        startActivity(Intent(this, activityClass))
        if (fini) finish()
    }
}