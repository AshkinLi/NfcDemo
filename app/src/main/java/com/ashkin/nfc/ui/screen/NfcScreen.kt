package com.ashkin.nfc.ui.screen

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.util.Consumer
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.ashkin.nfc.NfcHelper
import com.ashkin.nfc.PackageName
import com.ashkin.nfc.extension.asComponentActivity
import com.ashkin.nfc.extension.isApplicationAvailable

@Composable
fun NfcScreen(
    nfcHelper: NfcHelper,
    nfcTagContent: MutableState<String>,
    modifier: Modifier = Modifier,
) {
    val activity = LocalContext.current.asComponentActivity()

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
        NfcContent(
            nfcTagContent = nfcTagContent,
            modifier = Modifier.padding(innerPadding)
        )
    }
}


@Composable
fun NfcContent(
    nfcTagContent: MutableState<String>,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    val shouldShowNfcDialog = remember { mutableStateOf(false) }

    shouldShowNfcDialog.value = nfcTagContent.value.isNotEmpty()

    Column(
        modifier = modifier
            .padding(16.dp),
    ) {
        NfcAlertDialog(
            shouldShowDialog = shouldShowNfcDialog,
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Price: HKD 200.00"
        )

        val isAlipayInstalled = context.isApplicationAvailable(PackageName.ALIPAY)

        Button(
            onClick = {
                if (isAlipayInstalled) {
                    context.startActivity(
                        context.packageManager.getLaunchIntentForPackage(
                            PackageName.ALIPAY
                        )
                    )
                }
            },
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            enabled = isAlipayInstalled,
        ) {
            Text(text = "Pay via Alipay")
        }

        val isDaaSCInstalled = context.isApplicationAvailable(PackageName.DAASC)

        Button(
            onClick = {
                if (isDaaSCInstalled) {
                    context.startActivity(
                        context.packageManager.getLaunchIntentForPackage(
                            PackageName.DAASC
                        )
                    )
                }
            },
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            enabled = isDaaSCInstalled,
        ) {
            Text(text = "Pay via DaaSC")
        }
    }
}

@Composable
fun NfcAlertDialog(
    shouldShowDialog: MutableState<Boolean>,
) {
    if (shouldShowDialog.value) {
        AlertDialog(
            onDismissRequest = {
                shouldShowDialog.value = false
            },
            title = { Text(text = "Title") },
            text = {
                Text(
                    text = """
                        {
                            "payload": {
                                "price": 200,
                                "uid": "123"
                            }
                        }
                    """.trimIndent()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        shouldShowDialog.value = false
                    }
                ) {
                    Text(
                        text = "Confirm",
                        color = Color.White
                    )
                }
            }
        )
    }
}
