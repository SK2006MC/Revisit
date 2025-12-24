package com.sk.revisit.activities

import android.content.Intent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.sk.revisit.Revisit

open class BaseActivity : AppCompatActivity() {
    @JvmField
    var TAG: String = this.javaClass.simpleName

    fun alert(msg: String?) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }

    @JvmOverloads
    fun startMyActivity(activityClass: Class<*>, fini: Boolean = false) {
        MainActivity.bpn = 0
        startActivity(Intent(this, activityClass))
        if (fini) finish()
    }

    fun getRevisitApp(): Revisit {
        return application as Revisit
    }
}
