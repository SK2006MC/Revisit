package com.sk.revisit.components

import android.content.Context
import android.widget.Toast
import com.sk.revisit.activities.BaseActivity

open class Component(protected val activity: BaseActivity) {
    protected val tag: String = this::class.java.simpleName
    protected val context: Context = activity

    fun alert(msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
    }
}