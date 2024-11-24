package com.ashkin.nfc.extension

import android.content.Context
import android.content.ContextWrapper
import androidx.activity.ComponentActivity

fun Context.asComponentActivity(): ComponentActivity = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.asComponentActivity()
    else -> throw IllegalStateException("No activity found for context: $this")
}
