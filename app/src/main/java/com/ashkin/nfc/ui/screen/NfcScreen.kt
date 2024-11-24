package com.ashkin.nfc.ui.screen

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.util.Consumer
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.ashkin.nfc.NfcHelper
import com.ashkin.nfc.extension.asComponentActivity

@Composable
fun NfcScreen(
    nfcHelper: NfcHelper,
    modifier: Modifier = Modifier,
) {
    val activity = LocalContext.current.asComponentActivity()

    val nfcTagContent = remember { mutableStateOf("") }

    DisposableEffect(Unit) {
        val listener = Consumer<Intent> { intent ->
            nfcHelper.handleNfcIntent(intent) {
                nfcTagContent.value = it
            }
        }
        activity.addOnNewIntentListener(listener)
        onDispose { activity.removeOnNewIntentListener(listener) }
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        nfcHelper.enableNfcIntent(activity, activity::class.java)
    }

    LifecycleResumeEffect(Unit) {
        onPauseOrDispose {
            nfcHelper.disableNfcIntent(activity)
        }
    }

    Scaffold(
        modifier = modifier,
    ) { innerPadding ->
        Greeting(
            nfcTagContent = nfcTagContent,
            modifier = Modifier.padding(innerPadding)
        )
    }
}


@Composable
fun Greeting(
    nfcTagContent: MutableState<String>,
    modifier: Modifier = Modifier,
) {
    Column {
        if (nfcTagContent.value.isNotEmpty()) {
            Text(
                text = "NFC retrieve, tag content: ${nfcTagContent.value}",
                modifier = modifier
            )
        } else {
            Text(
                text = "NFC not retrieve",
                modifier = modifier
            )
        }
    }
}
